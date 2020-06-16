package io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.joml.Vector3f;

import core.Application;
import dev.Console;
import map.Chunk;
import map.Terrain;

public class ChunkStreamer extends Thread {
	
	private String worldName = "world1";
	
	// String of region file, and chunk
	private Map<String, Map<Integer, Chunk>> loadList = new HashMap<String, Map<Integer, Chunk>>();
	private Map<String, Map<Integer, Chunk>> saveList = new HashMap<String, Map<Integer, Chunk>>();
	
	private Map<String, int[]> cachedHeaders = new HashMap<String, int[]>();
	
	private boolean running = true;
	public volatile boolean tick = false;

	public ChunkStreamer(String worldName, Terrain terrain) {
		RegionIO.terrainPtr = terrain;
		this.worldName = worldName;
		start();
	}
	
	public void update() {
		tick = true;
		if (!loadList.isEmpty()) {
			load();
			loadList.clear();
		}
		
		if (!saveList.isEmpty()) {
			save(Application.scene.getCamera().getPosition());
			saveList.clear();
		}
	}
	
	@Override
	public void run() {
		/*while(running) {
			if (tick) {
				if (!loadList.isEmpty()) {
					load();
					loadList.clear();
				}
				
				if (!saveList.isEmpty()) {
					save(Application.scene.getCamera().getPosition());
					saveList.clear();
				}
				tick = false;
			}
		}*/
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
			
			//TEMP
			for(Chunk c : saveList.get(filename).values()) {
				c.cleanUp();
			}
		}
		
	}
	
	private void load() {
		for(String filename : loadList.keySet()) {
			Map<Integer, Chunk> chunks = loadList.get(filename);
			
			int[] header = cachedHeaders.get(filename);
			if (header != null) {
				RegionIO.load(filename, header, chunks, false);
				
			} else {
				RegionIO.load(filename, chunks, false);
				
			}
		}
		loadList.clear();
		cachedHeaders.clear();
	}
	
	public synchronized void queueForSaving(Chunk chunk) {
		if (chunk.editFlags == 0x0) {
			return;
		}
		
		int regionX = chunk.dataX >> 4;
		int regionY = 0;
		int regionZ = chunk.dataZ >> 4;
		
		int id = RegionIO.getOffset(chunk.dataX, 0, chunk.dataZ);
		
		String filename = RegionIO.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = saveList.get(filename);
		
		if (map == null) {
			map = new TreeMap<Integer, Chunk>();//ConcurrentSkipListMap
			saveList.put(filename, map);
		}
		
		map.put(id, chunk);
	}
	
	public synchronized void queueForLoading(Chunk chunk) {
		int regionX = chunk.dataX >> 4;
		int regionY = 0;
		int regionZ = chunk.dataZ >> 4;
		
		int id = RegionIO.getOffset(chunk.dataX, 0, chunk.dataZ);
		
		String filename = RegionIO.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = loadList.get(filename);
		
		if (map == null) {
			map = new TreeMap<Integer, Chunk>();
			loadList.put(filename, map);
		};
		
		
		map.put(id, chunk);
	}
}
