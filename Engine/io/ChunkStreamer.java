package io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.joml.Vector3f;

import core.Application;
import dev.Console;
import map.Chunk;
import map.Terrain;

public class ChunkStreamer extends Thread {
	
	// String of region file, and chunk
	private Map<String, Map<Integer, Chunk>> loadList = new HashMap<String, Map<Integer, Chunk>>();
	private Map<String, Map<Integer, Chunk>> saveList = new HashMap<String, Map<Integer, Chunk>>();
	
	private Map<String, int[]> cachedHeaders = new HashMap<String, int[]>();
	
	private boolean running = true;
	public boolean tick = false;

	public ChunkStreamer(Terrain terrain) {
		RegionIO.terrainPtr = terrain;
	}
	
	public void update() {
		if (!loadList.isEmpty()) {
			load();
			loadList.clear();
		}
		
		if (!saveList.isEmpty()) {
			save(Application.scene.getCamera().getPosition());
			saveList.clear();
		}
		
		
	}
	
	public void finish() {
		running = false;
	}
	
	private void save(Vector3f cameraPos) {
		/*int camX = ((int)cameraPos.x / Chunk.CHUNK_SIZE);
		int camY = ((int)cameraPos.y / Chunk.CHUNK_SIZE);
		int camZ = ((int)cameraPos.z / Chunk.CHUNK_SIZE);*/
		
		Iterator<String> iter = saveList.keySet().iterator();
		
		while(iter.hasNext()) {
			String filename = iter.next();
			/*String[] fileSplit = filename.split("\\.");
			
			int regionX = Integer.parseInt(fileSplit[1]) * RegionIO.CHUNKS_PER_AXIS;
			int regionY = Integer.parseInt(fileSplit[2]) * RegionIO.CHUNKS_PER_AXIS;
			int regionZ = Integer.parseInt(fileSplit[3]) * RegionIO.CHUNKS_PER_AXIS;
			
			// Check if it is time to stream to file
			if (regionX - camX >= Globals.chunkRenderDist ||
				regionY - camY >= Globals.chunkRenderDist ||
				regionZ - camZ >= Globals.chunkRenderDist ||
				camX - regionX >= Globals.chunkRenderDist + RegionIO.CHUNKS_PER_AXIS ||
				camY - regionY >= Globals.chunkRenderDist + RegionIO.CHUNKS_PER_AXIS ||
				camZ - regionZ >= Globals.chunkRenderDist + RegionIO.CHUNKS_PER_AXIS) {
				
			}*/
			RegionIO.save(filename, saveList.get(filename));
			iter.remove();
		}
	}
	
	private void load() {
		for(String filename : loadList.keySet()) {
			Map<Integer, Chunk> chunks = loadList.get(filename);
			
			int[] header = cachedHeaders.get(filename);
			if (header != null) {
				RegionIO.load(filename, header, chunks);
				
			} else {
				RegionIO.load(filename, chunks);
				
			}
		}
		loadList.clear();
		cachedHeaders.clear();
	}
	
	public void queueForSaving(Chunk chunk) {
		int regionX = chunk.x >> 4;
		int regionY = 0;
		int regionZ = chunk.z >> 4;
		
		int id = RegionIO.getOffset(chunk.x, 0, chunk.z);
		
		String filename = RegionIO.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = saveList.get(filename);
		
		if (map == null) {
			map = new ConcurrentSkipListMap<Integer, Chunk>();
			saveList.put(filename, map);
		}
		
		map.put(id, chunk);
	}
	
	public void queueForLoading(Chunk chunk) {
		int regionX = chunk.x >> 4;
		int regionY = 0;
		int regionZ = chunk.z >> 4;
		
		
		
		int id = RegionIO.getOffset(chunk.x, 0, chunk.z);
		
		String filename = RegionIO.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = loadList.get(filename);
		
		if (map == null) {
			map = new ConcurrentSkipListMap<Integer, Chunk>();
			loadList.put(filename, map);
		};
		
		
		map.put(id, chunk);
	}
}
