package io.terrain;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dev.Console;
import map.Chunk;
import map.prop.Props;
import map.prop.StaticPropProperties;
import map.tile.BuildData;
import map.tile.BuildSector;
import map.tile.Tile;
import scene.entity.Entity;
import scene.entity.EntityData;
import scene.entity.EntityHandler;
import scene.overworld.Overworld;
import util.RunLengthOutputStream;
import util.ZLibUtil;  

// stores 16x16x16 chunk region
public class RegionSaver implements Runnable {
	
	final static int CHUNKS_PER_AXIS = 16;
	final static int CHUNKS_PER_REGION = CHUNKS_PER_AXIS * 1 * CHUNKS_PER_AXIS;
	private final static int LOOKUPTBL_SIZE_BYTES = CHUNKS_PER_REGION * 4;
	private final static int LOOKUPTBL_OFFSET_BYTES = 4;
	private final static int SECTOR_SIZE = 4096;
	
	private String filename;
	private Map<Integer, Chunk> map;
	private ChunkCallbackInterface c;
	
	public RegionSaver(ChunkCallbackInterface c, String filename, Map<Integer, Chunk> map) {
		this.filename = filename;
		this.map = map;
		this.c = c;
	}

	@Override
	public void run() {
		save(filename, map);
	}
	
	private byte[] writeChunk(Chunk chunk, RunLengthOutputStream data) {
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
		
		final Props[][] items = chunk.chunkProps.getPropMap();
		final StaticPropProperties[][] tileProps = chunk.chunkProps.getEntityPropertyMap();
		if ((flags & 0x02) != 0) {
			for (int x = 0; x < items.length; x++) {
				for (int z = 0; z < items.length; z++) {
					if (items[x][z] == null) {
						data.writeShort(Short.MAX_VALUE);
					} else {
						data.writeShort(items[x][z].ordinal());
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
		}
		
		BuildData building = chunk.getBuilding();
		
		if ((flags & 0x01) != 0) {
			Collection<BuildSector> sectors = building.getSectors();
			data.writeShort(sectors.size());
			for(BuildSector sector : sectors) {
				data.writeByte(sector.getX() + (sector.getZ() << 4));
				data.writeByte(sector.getY());
				for(int x = 0; x < BuildSector.SIZE; x++) {
					for(int z = 0; z < BuildSector.SIZE; z++) {
						for(int y = 0; y < BuildSector.SIZE; y++) {
							Tile tile = sector.get(x, y, z);
							if (tile == null) {
								for(int i = 0; i < Tile.NUM_MATS*2 + 1; i++) {
									data.writeByte(0);
								}
							} else {
								data.writeByte(tile.getSlope());
								for (int i = 0; i < Tile.NUM_MATS ; i++) {
									data.writeByte(tile.materials[i].ordinal());
									data.writeByte(tile.getFlags()[i]);
								}
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

	void save(String filename, Map<Integer, Chunk> map) {
		File file = new File(filename);
		//Map<Integer, Chunk> map = new HashMap<Integer, Chunk>(m);\

		if (file.exists()) {
			append(file, map);
			this.c.saveCallback();
			return;
		}
		
		RunLengthOutputStream buf = new RunLengthOutputStream();
		
		Map<Integer, byte[]> chunkData = new HashMap<Integer, byte[]>();

		int freespace = 0;
		for(int key : map.keySet()) {
			Chunk chunk = map.get(key);
			final byte[] compressedData = writeChunk(chunk, buf);
			chunkData.put(key, compressedData);

			Console.log("saving ",chunk.dataX,chunk.dataZ," at "+freespace*SECTOR_SIZE);
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
		
		this.c.saveCallback();
	}

	private void append(File file, Map<Integer, Chunk> map) {
		RunLengthOutputStream byteArrOutStream = new RunLengthOutputStream();
		
		Map<Integer, byte[]> chunkData = new TreeMap<Integer, byte[]>();	// Index each chunk's memory by it's offset in header (see getOffset()`)

		int spaceUsedByAppend = 0;
		for(int key : map.keySet()) {	// Sort data by memory position
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
			List<Integer> chunkMemoryMap = new LinkedList<Integer>();	// Index into each chunks position in memory
			int newFreespace = freespace + spaceUsedByAppend;	// Guarenteed new free memory, this probably skips over space
																// (since only new chunks really expand the data, not appended chunks). Should fix that eventually.
			write(raf, 
					newFreespace >> 24,
					newFreespace >> 16,
					newFreespace >> 8,
					newFreespace);
			
			// Lookup table
			for(int bytePosition = 0; bytePosition < CHUNKS_PER_REGION; bytePosition++) {
				if (chunkData.containsKey(bytePosition)) {
					raf.seek((1 + bytePosition) * 4);	// 1 (freespace ptr) + position*4 (int holding location & size)
					
					int existingTableData = raf.readInt();
					int len = chunkData.get(bytePosition).length;
					byte lenBytes = (byte) Math.ceil(len / (double)SECTOR_SIZE);
					if (existingTableData == 0) {	// Chunk has no data yet
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
							chunkMemoryMap.add(existingTableData >> 8);	// Chunk exists, and data doesn't overflow
						} else {
							chunkMemoryMap.add(existingTableData >> 8);	// Chunk exists, but new data wont fit into allocation
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

	private void write(RandomAccessFile raf, int b1, int b2, int b3, int b4) throws IOException {
		raf.write((byte) b1);
		raf.write((byte) b2);
		raf.write((byte) b3);
		raf.write((byte) b4);
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
