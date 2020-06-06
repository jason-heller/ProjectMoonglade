package io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import dev.Console;
import map.Chunk;
import map.Terrain;
import map.building.Building;
import map.building.Material;
import map.building.Tile;
import util.ZLibUtil;  

// stores 16x16x16 chunk region
public class RegionIO {
	
	final static int CHUNKS_PER_AXIS = 16;
	final static int CHUNKS_PER_REGION = CHUNKS_PER_AXIS * 1 * CHUNKS_PER_AXIS;
	private final static int HEADER_SIZE = CHUNKS_PER_REGION;
	private final static int SECTOR_SIZE = 2048;
	public static Terrain terrainPtr;

	private static byte[] writeChunk(Chunk chunk, ByteArrayOutputStream data) {
		data.reset();
		
		//final float[][] heights = chunk.heightmap;
		//if (chunk_heightmap_was_changed) {
		
		//}
		
		//final int[][] items = chunk.items.tilemap;
		//if (chunk_itemtiles_was_changed) {
		
		//}
		
		Building building = chunk.getBuilding();
		Tile[][][] tiles = building.getTilemap();

		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles.length; z++) {
				for (int y = 0; y < tiles[0].length; y++) {
					
					Tile tile = tiles[x][y][z];
					if (tile == null) {
						for(int i = 0; i < 7; i++) {
							data.write(0);
						}
					} else {
						data.write(tile.getWalls());
						
						for (int i = 0; i < 6 ; i++) {
							data.write((byte)tile.id[i].ordinal());
						}
					}
				}
			}
		}
		
		byte[] uncompressedData = data.toByteArray();
		try {
			Console.log("saved ",chunk.x,chunk.z);
			return ZLibUtil.compress(uncompressedData);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	private static void readChunk(Chunk chunk, byte[] decompressedData) {
		Tile[][][] tiles = chunk.getBuilding().getTilemap();
		
		int index = 0;
		for(int x = 0; x < tiles.length; x++) {
			for(int z = 0; z < tiles.length; z++) {
				for(int y = 0; y < tiles[0].length; y++) {
					byte wallFlags = decompressedData[index++];
					
					Material[] mats = new Material[6];
					for (int j = 0; j < 6 ; j++) {
						mats[j] = Material.values()[decompressedData[index++]];
					}
					
					tiles[x][y][z] = new Tile(chunk, mats, wallFlags);
				}
			}
		}
		
		//synchronized(terrainPtr) {
		Console.log("loaded ",chunk.x,chunk.z);
			chunk.setState(Chunk.BUILDING);
		//}
	}

	public static void save(String filename, Map<Integer, Chunk> map) {
		File file = new File(filename);
		//Map<Integer, Chunk> map = new HashMap<Integer, Chunk>(m);\
		
		if (file.exists()) {
			//append(file, map);
			//return;
			file.delete();
		}
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		Map<Integer, byte[]> chunkData = new HashMap<Integer, byte[]>();

		for(int key : map.keySet()) {
			Chunk chunk = map.get(key);
			final byte[] compressedData = writeChunk(chunk, buf);
			chunkData.put(key, compressedData);
			
			chunk.setState(Chunk.UNLOADING);
		}
		
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			int bytePosition = 0;
			// Header
			for(int i = 0; i < CHUNKS_PER_REGION; i++) {
				if (chunkData.containsKey(i)) {
					int len = chunkData.get(i).length;
					int lenBytes = (int) Math.ceil(len / (double)SECTOR_SIZE);
					//System.err.println(bytePosition + ", "+lenBytes);
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
				if (chunkData.containsKey(i)) {
					byte[] compressedData = chunkData.get(i);
					out.write(compressedData);
					
					final int size = compressedData.length;
					final int padding = (int) (Math.ceil(size/(double)SECTOR_SIZE)*SECTOR_SIZE) - size;
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
		// TODO Auto-generated method stub
		
	}

	public static void load(String filename, Map<Integer, Chunk> chunks) {
		File file = new File(filename);
		if (!file.exists()) {
			for(Chunk chunk : chunks.values()) {
				chunk.setState(Chunk.GENERATING);
			}
			return;
		}
	
		//synchronized(ChunkStreamer.class) {
			
			try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

				int[] header = new int[HEADER_SIZE];
				for(int i = 0; i < header.length; i++) {
					header[i] = in.readInt();
				}
				
				load(header, in, chunks);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		//}
	}
	
	public static void load(String filename, int[] header, Map<Integer, Chunk> chunks) {
		try (DataInputStream in = new DataInputStream(new FileInputStream(new File(filename)))) {
			load(header, in, chunks);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
	}
	
	private static void load(int[] header, DataInputStream in, Map<Integer, Chunk> chunks) throws IOException, DataFormatException {
		// Assumed chunks are in sequential offset order
		//synchronized(ChunkStreamer.class) {
		
		
			int bytePosition = 0;	// Excluding header
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
				while(bytePosition != dataPosition) {
					bytePosition += in.skipBytes(dataPosition - bytePosition);
				}
			
				// Read chunk data
				byte[] compressedData = new byte[dataLength];
				final int endPosition = dataPosition + dataLength;
				int startPosition = bytePosition;
				while(bytePosition != endPosition) {
					bytePosition += in.read(compressedData, bytePosition - startPosition, endPosition - bytePosition);
				}
				bytePosition = endPosition;
				// Parse data
				byte[] decompressedData = ZLibUtil.decompress(compressedData);
				readChunk(chunk, decompressedData);
			}
		//}
	}

	public static String getFilename(int regionX, int regionY, int regionZ) {
		return "saves/r." + regionX + "." + regionY + "." + regionZ + ".rgn";
	}
	
	public static int getOffset(int regionX, int regionY, int regionZ) {
		int rx = Math.floorMod(regionX, CHUNKS_PER_AXIS);
		int ry = 0;//Math.floorMod(regionY, CHUNKS_PER_AXIS);
		int rz = Math.floorMod(regionZ, CHUNKS_PER_AXIS);

		return ((rx) + (rz * CHUNKS_PER_AXIS)
				+ (ry * (CHUNKS_PER_AXIS * CHUNKS_PER_AXIS)));
	}
}
