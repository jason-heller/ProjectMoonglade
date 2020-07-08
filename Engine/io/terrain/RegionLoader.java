package io.terrain;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.zip.DataFormatException;

import map.Chunk;
import map.Enviroment;
import map.Material;
import map.Terrain;
import map.prop.Props;
import map.prop.StaticPropProperties;
import map.tile.BuildData;
import map.tile.BuildSector;
import map.tile.Tile;
import scene.entity.Entity;
import scene.entity.EntityData;
import scene.entity.EntityHandler;
import scene.overworld.Overworld;
import util.RunLengthInputStream;
import util.ZLibUtil;  

// stores 16x16x16 chunk region
public class RegionLoader implements Runnable {
	
	final static int CHUNKS_PER_AXIS = 16;
	final static int CHUNKS_PER_REGION = CHUNKS_PER_AXIS * 1 * CHUNKS_PER_AXIS;
	private final static int LOOKUPTBL_SIZE_BYTES = CHUNKS_PER_REGION * 4;
	private final static int LOOKUPTBL_OFFSET_BYTES = 4;
	private final static int SECTOR_SIZE = 4096;
	private static final long ENTITY_EXPIRATION_TIME = 300000; // 5 minutes
	public static Terrain terrainPtr;
	
	private String filename;
	private Map<Integer, Chunk> map;
	private ChunkCallbackInterface c;
	boolean ignoreTimeDifference = false;
	//private Enviroment enviroment;
	
	public RegionLoader(ChunkCallbackInterface c, String filename, Map<Integer, Chunk> map, Enviroment enviroment) {
		this.filename = filename;
		this.map = map;
		this.c = c;
		//this.enviroment = enviroment;
	}

	@Override
	public void run() {
		load(filename, map, ignoreTimeDifference);
		this.c.loadCallback();
	}

	private void readChunk(Chunk chunk, RunLengthInputStream data, boolean ignoreTimeDifference) {
		float[][] heights = chunk.heightmap;
		Props[][] items = chunk.chunkProps.getPropMap();
		StaticPropProperties[][] props = chunk.chunkProps.getEntityPropertyMap();
		
		byte editFlags = data.readByte();
		long timestamp = data.readLong();
		long timeDifference = System.currentTimeMillis() - timestamp;
		
		if (ignoreTimeDifference) {
			timeDifference = 0;
		}
		
		chunk.editFlags = editFlags;
		
		if ((editFlags & 0x04) != 0) {
			for (int z = 0; z < heights.length; z++) {
				for (int x = 0; x < heights.length; x++) {
					heights[z][x] = data.readFloat();
				}
			}
		}
		
		if ((editFlags & 0x02) != 0) {
			for (int x = 0; x < items.length; x++) {
				for (int z = 0; z < items.length; z++) {
					int id = data.readShort();
					if (id != Short.MAX_VALUE) {
						items[x][z] = Props.values()[id];
						
						if (data.readByte() != 0) {
							float dx = data.readFloat();
							float dz = data.readFloat();
							float scale = data.readFloat();
							StaticPropProperties prop = new StaticPropProperties(dx, dz, scale);
							prop.damage = data.readByte();
							props[x][z] = prop;
						}
					}
				}
			}
		}
		
		if ((editFlags & 0x01) != 0) {
			BuildData building = chunk.getBuilding();
			int numSectors = data.readShort();
			for(int i = 0; i < numSectors; i++) {
				int sectorX = data.readByte();
				int sectorZ = sectorX >> 4;
				sectorX &= 15;
				int sectorY = data.readByte();
				BuildSector sector = building.addSector(sectorX, sectorY, sectorZ);
				
				for(int x = 0; x < BuildSector.SIZE; x++) {
					for(int z = 0; z < BuildSector.SIZE; z++) {
						for(int y = 0; y < BuildSector.SIZE; y++) {
							byte slope = data.readByte();
							
							boolean noMats = true;
							Material[] mats = new Material[Tile.NUM_MATS];
							byte[] flags = new byte[Tile.NUM_MATS];
							for (int j = 0; j < Tile.NUM_MATS ; j++) {
								mats[j] = Material.values()[data.readByte()];
								flags[j] = data.readByte();
								
								if (mats[j] != Material.NONE) noMats = false;
							}
							
							if (!noMats || slope != 0) {
								sector.addTile(new Tile(mats, slope, flags), x, y, z);
							}
						}
					}
				}
			}
		}
		
		int id;
		while((id = data.readShort()) != 0) {
			byte persistency = data.readByte();
			Entity e = EntityData.instantiate(id);
			e.load(data);
			
			if (persistency != 1 || timeDifference < ENTITY_EXPIRATION_TIME) {
				EntityHandler.addEntity(e);
			}
			
			
		}
		
		//synchronized(terrainPtr) {
			chunk.setState(Chunk.BUILDING);
		//}
	}

	private void load(String filename, Map<Integer, Chunk> chunks, boolean ignoreTimeDifference) {
		File file = new File(filename);
		if (!file.exists()) {
			//BiomeVoronoi biomeVoronoi = enviroment.getBiomeVoronoi();
			//final int wid = (Chunk.VERTEX_COUNT-1);
			
			for(Chunk chunk : chunks.values()) {
				chunk.setState(Chunk.GENERATING);
				//GenTerrain.buildTerrain(chunk, chunk.dataX*wid, 0, chunk.dataZ*wid, Chunk.VERTEX_COUNT, Chunk.POLYGON_SIZE, biomeVoronoi);
			}
			
			/*for(Chunk chunk : chunks.values()) {
				this.c.loadCallback(chunk);
			}*/
			for(Chunk chunk : chunks.values()) {
				chunk.setState(Chunk.GENERATING);
			}
			return;
		}
	
		//synchronized(ChunkStreamer.class) {
			
			try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
				
				int[] header = new int[CHUNKS_PER_REGION /*HEADER_SIZE_BYTES / 4*/];
				raf.seek(LOOKUPTBL_OFFSET_BYTES);	// Freespace int, not used in game, only for appending
				try {
					for(int i = 0; i < header.length; i++) {
						header[i] = raf.readInt();
						//header[i] = raf.read() << 24 + raf.read() << 16 + raf.read() << 8 + raf.read();
					}
				} catch (Exception e) {
					System.err.println(raf.getFilePointer());
					System.err.println(file);
				}
				
				load(header, raf, chunks, ignoreTimeDifference);
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		//}
	}
	
	private void load(int[] header, RandomAccessFile raf, Map<Integer, Chunk> chunks, boolean ignoreTimeDifference) throws IOException, DataFormatException {
		// Assumed chunks are in sequential offset order
		//synchronized(ChunkStreamer.class) {
		
			for(int tablePosition : chunks.keySet()) {
				Chunk chunk = chunks.get(tablePosition);
				//int tablePosition = getOffset(chunk.x, 0, chunk.z);
				int dataPosition = (header[tablePosition] >> 8) * SECTOR_SIZE;
				int dataLength = (header[tablePosition] & 0xFF) * SECTOR_SIZE;
				if(header[tablePosition] == 0) {
					chunk.setState(Chunk.GENERATING);
					continue;
				}
				
				// Seek to chunk position
				raf.seek(LOOKUPTBL_OFFSET_BYTES + (LOOKUPTBL_SIZE_BYTES) + dataPosition);
				// Read chunk data
				byte[] compressedData = new byte[dataLength];
				
				final int endPosition = dataPosition + dataLength;
				final int startPosition = dataPosition;
				int bytePosition = startPosition;	// Excluding header
				
				while(bytePosition != endPosition) {
					bytePosition += raf.read(compressedData, bytePosition - startPosition, endPosition - bytePosition);
				}
				
				// Parse data
				byte[] decompressedData = ZLibUtil.decompress(compressedData);
				readChunk(chunk, new RunLengthInputStream(decompressedData), ignoreTimeDifference);
			}
		//}
	}

	public static String getFilename(int regionX, int regionY, int regionZ) {
		return "saves/" + Overworld.worldFileName + "/r." + regionX + "." + regionY + "." + regionZ + ".rgn";
	}
	
	public static int getOffset(int regionX, int regionY, int regionZ) {
		int rx = Math.floorMod(regionX, CHUNKS_PER_AXIS);
		int ry = 0;//Math.floorMod(regionY, CHUNKS_PER_AXIS);
		int rz = Math.floorMod(regionZ, CHUNKS_PER_AXIS);

		return ((rx) + (rz * CHUNKS_PER_AXIS)
				+ (ry * (CHUNKS_PER_AXIS * CHUNKS_PER_AXIS)));
	}
}
