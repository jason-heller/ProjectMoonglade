package io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import dev.Console;
import map.Chunk;
import map.Material;
import map.Terrain;
import map.building.Building;
import map.building.BuildingTile;
import map.tile.TileProperties;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.EntityData;
import scene.overworld.Overworld;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;
import util.ZLibUtil;  

// stores 16x16x16 chunk region
public class RegionIO {
	
	final static int CHUNKS_PER_AXIS = 16;
	final static int CHUNKS_PER_REGION = CHUNKS_PER_AXIS * 1 * CHUNKS_PER_AXIS;
	private final static int LOOKUPTBL_SIZE_BYTES = CHUNKS_PER_REGION * 4;
	private final static int LOOKUPTBL_OFFSET_BYTES = 4;
	private final static int SECTOR_SIZE = 4096;
	private static final long ENTITY_EXPIRATION_TIME = 300000; // 5 minutes
	public static Terrain terrainPtr;

	private static byte[] writeChunk(Chunk chunk, RunLengthOutputStream data) {
		data.reset();
		
		byte flags = chunk.editFlags;
		data.writeByte(flags);	// uuuuuHuB
		data.writeLong(System.currentTimeMillis());
		
		final float[][] heights = chunk.heightmap;
		if ((flags & 0x04) != 0) {
			for (int z = 0; z < heights.length; z++) {
				for (int x = 0; x < heights.length; x++) {
					data.writeFloat(heights[z][x]);
				}
			}
		}
		
		final int[][] items = chunk.items.getTilemap();
		final TileProperties[][] tileProps = chunk.items.getTilePropertyMap();
		if ((flags & 0x02) != 0) {
			for (int x = 0; x < items.length; x++) {
				for (int z = 0; z < items.length; z++) {
					data.writeShort(items[x][z]);
					
					if (tileProps[x][z] == null) {
						data.writeByte(0);
					} else {
						data.writeByte(1);
						data.writeFloat(tileProps[x][z].dx);
						data.writeFloat(tileProps[x][z].dz);
						data.writeFloat(tileProps[x][z].scale);
						data.writeByte(tileProps[x][z].damage);
					}
				}
			}
		}
		
		Building building = chunk.getBuilding();
		BuildingTile[][][] tiles = building.getTilemap();

		if ((flags & 0x01) != 0) {
			for (int x = 0; x < tiles.length; x++) {
				for (int z = 0; z < tiles.length; z++) {
					for (int y = 0; y < tiles[0].length; y++) {
						
						BuildingTile tile = tiles[x][y][z];
						if (tile == null) {
							for(int i = 0; i < 8; i++) {
								data.writeByte(0);
							}
						} else {
							data.writeByte(tile.getWalls());
							data.writeByte(tile.getFlags());
							
							for (int i = 0; i < 6 ; i++) {
								data.writeByte(tile.materials[i].ordinal());
							}
						}
					}
				}
			}
		}
		
		List<Entity> chunkEntities = EntityHandler.getAllEntitiesInChunk(chunk);
		if (chunkEntities != null) {
			for(Entity entity : chunkEntities) {
				if (entity.getPersistency() == 1 || entity.getPersistency() == 2) {
					data.writeShort(EntityData.getId(entity.getClass()));
					data.writeByte(entity.getPersistency());
					entity.save(data);
				}
			}
		}
		data.writeShort(0);
		
		data.close();
		byte[] uncompressedData = data.toByteArray();
		try {
			return ZLibUtil.compress(uncompressedData);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	private static void readChunk(Chunk chunk, RunLengthInputStream data, boolean ignoreTimeDifference) {
		BuildingTile[][][] tiles = chunk.getBuilding().getTilemap();
		float[][] heights = chunk.heightmap;
		int[][] items = chunk.items.getTilemap();
		TileProperties[][] props = chunk.items.getTilePropertyMap();
		
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
					items[x][z] = data.readShort();
	
					if (data.readByte() != 0) {
						float dx = data.readFloat();
						float dz = data.readFloat();
						float scale = data.readFloat();
						TileProperties prop = new TileProperties(dx, dz, scale);
						prop.damage = data.readByte();
						props[x][z] = prop;
					}
				}
			}
		}
		
		if ((editFlags & 0x01) != 0) {
			for(int x = 0; x < tiles.length; x++) {
				for(int z = 0; z < tiles.length; z++) {
					for(int y = 0; y < tiles[0].length; y++) {
						byte walls = data.readByte();
						byte flags = data.readByte();
						
						Material[] mats = new Material[6];
						for (int j = 0; j < 6 ; j++) {
							mats[j] = Material.values()[data.readByte()];
						}
						
						if (walls != 0) {
							tiles[x][y][z] = new BuildingTile(chunk, mats, walls, flags);
						}
					}
				}
			}
		}
		
		
		//TODO: THIS IS FUCKING BROKEN AAAA
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

	static void save(String filename, Map<Integer, Chunk> map) {
		File file = new File(filename);
		//Map<Integer, Chunk> map = new HashMap<Integer, Chunk>(m);\

		if (file.exists()) {
			append(file, map);
			return;
		}
		
		RunLengthOutputStream buf = new RunLengthOutputStream();
		
		Map<Integer, byte[]> chunkData = new HashMap<Integer, byte[]>();

		int freespace = 0;
		for(int key : map.keySet()) {
			Chunk chunk = map.get(key);
			final byte[] compressedData = writeChunk(chunk, buf);
			chunkData.put(key, compressedData);
			freespace += Math.ceil(compressedData.length / (double)SECTOR_SIZE);
			chunk.setState(Chunk.UNLOADING);
		}
		
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			int bytePosition = 0;
			// Header
			/*out.writeChar('R');
			out.writeChar('G');
			out.writeChar('N');
			out.writeByte(1);*/
			
			out.write(freespace >> 24);
			out.write(freespace >> 16);
			out.write(freespace >> 8);
			out.write(freespace);
			for(int i = 0; i < CHUNKS_PER_REGION; i++) {
				if (chunkData.containsKey(i)) {
					int len = chunkData.get(i).length;
					int lenBytes = (int) Math.ceil(len / (double)SECTOR_SIZE);
					out.write(bytePosition >> 16);
					out.write(bytePosition >> 8);
					out.write(bytePosition);
					out.write(lenBytes);
					bytePosition += lenBytes;
				} else {
					out.write(0);	// TODO: Only save chunks if theyre edited in some way
					out.write(0);
					out.write(0);
					out.write(0);
				}
			}
			
			for(int i = 0; i < CHUNKS_PER_REGION; i++) {
				final int padding;
				if (chunkData.containsKey(i)) {
					byte[] compressedData = chunkData.get(i);
					out.write(compressedData);
					
					final int size = compressedData.length;
					padding = (int) (Math.ceil(size/(double)SECTOR_SIZE)*SECTOR_SIZE) - size;
					
					try {
						out.write(new byte[padding]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void append(File file, Map<Integer, Chunk> map) {
		RunLengthOutputStream byteArrOutStream = new RunLengthOutputStream();
		
		Map<Integer, byte[]> chunkData = new HashMap<Integer, byte[]>();

		int spaceUsedByAppend = 0;
		for(int key : map.keySet()) {
			Chunk chunk = map.get(key);
			final byte[] compressedData = writeChunk(chunk, byteArrOutStream);
			chunkData.put(key, compressedData);
			
			chunk.setState(Chunk.UNLOADING);
			spaceUsedByAppend += Math.ceil(compressedData.length / (double)SECTOR_SIZE);
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			int freespace = raf.readInt();
			raf.seek(0);
			
			// Header
			List<Integer> chunkMemoryMap = new LinkedList<Integer>();
			int newFreespace = freespace + spaceUsedByAppend;
			write(raf, 
					newFreespace >> 24,
					newFreespace >> 16,
					newFreespace >> 8,
					newFreespace);
			
			// Lookup table
			for(int bytePosition = 0; bytePosition < CHUNKS_PER_REGION; bytePosition++) {
				if (chunkData.containsKey(bytePosition)) {
					raf.seek((1 + bytePosition) * 4);
					
					int existingTableData = raf.readInt();
					int len = chunkData.get(bytePosition).length;
					byte lenBytes = (byte) Math.ceil(len / (double)SECTOR_SIZE);
					if (existingTableData == 0) {
						raf.seek((1 + bytePosition) * 4);
						
						write(raf, 
								freespace >> 16,
								freespace >> 8,
								freespace,
								lenBytes);
						
						chunkMemoryMap.add(freespace);
						freespace += lenBytes;
					} else {
						if (lenBytes == (existingTableData & 0xFF)) {
							chunkMemoryMap.add(existingTableData >> 8);
						} else {
							chunkMemoryMap.add(existingTableData >> 8);
							Console.log("OH FUCK");
							// TODO: This happens when a chunk exceeds its current allocation of sectors
							// when this happens, change its last sector's last 3 bytes to be a 
							// pointer to the 'freespace' variable above, take the original 3 bytes and 
							// insert it to the beginning of the chunk data, further data written for
							// this chunk will be added to this new allocation of sectors
							
							// Alternatively extend the sector size of this chunk and push all the data
							// of further chunks ahead, ajusting the memory map array & lookup table
							// accordingly
						}
						
					}
				}
			}
			
			Iterator<Integer> freespaceIterator = chunkMemoryMap.iterator();
			for(byte[] compressedData : chunkData.values()) { // Hopefully in order ack
				raf.seek(LOOKUPTBL_OFFSET_BYTES + (LOOKUPTBL_SIZE_BYTES) + (freespaceIterator.next() * SECTOR_SIZE));
				raf.write(compressedData);
				
				final int size = compressedData.length;
				final int padding = (int) (Math.ceil(size/(double)SECTOR_SIZE)*SECTOR_SIZE) - size;
				try {
					raf.write(new byte[padding]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			raf.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void write(RandomAccessFile raf, int b1, int b2, int b3, int b4) throws IOException {
		raf.write((byte) b1);
		raf.write((byte) b2);
		raf.write((byte) b3);
		raf.write((byte) b4);
	}

	public static void load(String filename, Map<Integer, Chunk> chunks, boolean ignoreTimeDifference) {
		File file = new File(filename);
		if (!file.exists()) {
			for(Chunk chunk : chunks.values()) {
				chunk.setState(Chunk.GENERATING);
			}
			return;
		}
	
		//synchronized(ChunkStreamer.class) {
			
			try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
				
				int[] header = new int[CHUNKS_PER_REGION /*HEADER_SIZE_BYTES / 4*/];
				raf.seek(LOOKUPTBL_OFFSET_BYTES);	// Freespace int, not used in game, only for appending
				for(int i = 0; i < header.length; i++) {
					header[i] = raf.readInt();
					//header[i] = raf.read() << 24 + raf.read() << 16 + raf.read() << 8 + raf.read();
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
	
	public static void load(String filename, int[] header, Map<Integer, Chunk> chunks, boolean ignoreTimeDifference) {
		try (RandomAccessFile raf = new RandomAccessFile(new File(filename), "r")) {
			load(header, raf, chunks, ignoreTimeDifference);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
	}
	
	private static void load(int[] header, RandomAccessFile raf, Map<Integer, Chunk> chunks, boolean ignoreTimeDifference) throws IOException, DataFormatException {
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
