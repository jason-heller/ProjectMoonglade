package io.terrain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Vector3f;

import core.Application;
import map.Chunk;
import map.Enviroment;

public class ChunkStreamer {
	
	// String of region file, and chunk
	private Map<String, Map<Integer, Chunk>> loadList = new HashMap<String, Map<Integer, Chunk>>();
	private Map<String, Map<Integer, Chunk>> saveList = new HashMap<String, Map<Integer, Chunk>>();
	
	//private List<Chunk> genList = new LinkedList<Chunk>();
	
	private Map<String, int[]> cachedHeaders = new HashMap<String, int[]>();
	
	private ChunkCallback callback;
	private Enviroment enviroment;

	public ChunkStreamer(String worldName, Enviroment enviroment) {
		this.enviroment = enviroment;
		callback = new ChunkCallback();
	}
	
	public void update() {
		if (!loadList.isEmpty()) {
			load();
		}
		
		if (!saveList.isEmpty()) {
			save(Application.scene.getCamera().getPosition());
			saveList.clear();
		}
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
			//RegionSaver.save(filename, saveList.get(filename));
			new Thread(new RegionSaver(callback, filename, saveList.get(filename))).start();
		}
		
	}
	
	private void load() {
		for(String filename : loadList.keySet()) {
			Map<Integer, Chunk> chunks = loadList.get(filename);
			
			int[] header = cachedHeaders.get(filename);
			if (header != null) {
				//RegionSaver.load(filename, header, chunks, false);
				new Thread(new RegionLoader(callback, filename, chunks, enviroment)).start();
			} else {
				//RegionSaver.load(filename, chunks, false);
				new Thread(new RegionLoader(callback, filename, chunks, enviroment)).start();
			}
		}
		loadList.clear();
		cachedHeaders.clear();
	}
	
	public void queueForSaving(Chunk chunk) {
		if (chunk.editFlags == 0x0) {
			chunk.cleanUp();
			return;
		}
		
		int regionX = chunk.dataX >> 4;
		int regionY = 0;
		int regionZ = chunk.dataZ >> 4;
		
		int id = RegionSaver.getOffset(chunk.dataX, 0, chunk.dataZ);
		
		String filename = RegionSaver.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = saveList.get(filename);
		
		if (map == null) {
			map = new TreeMap<Integer, Chunk>();//ConcurrentSkipListMap
			saveList.put(filename, map);
		}
		
		map.put(id, chunk);
	}
	
	public void queueForLoading(Chunk chunk) {
		int regionX = chunk.dataX >> 4;
		int regionY = 0;
		int regionZ = chunk.dataZ >> 4;
		
		int id = RegionSaver.getOffset(chunk.dataX, 0, chunk.dataZ);
		
		String filename = RegionSaver.getFilename(regionX, regionY, regionZ);
		
		Map<Integer, Chunk> map = loadList.get(filename);
		
		if (map == null) {
			map = new TreeMap<Integer, Chunk>();
			loadList.put(filename, map);
		};
		
		
		map.put(id, chunk);
	}
}

class ChunkCallback implements ChunkCallbackInterface {
	
	@Override
	public void saveCallback(Chunk chunk) {
		chunk.setState(Chunk.UNLOADING);
	}
	
	@Override
	public void loadCallback(Chunk chunk) {
		chunk.setState(Chunk.GENERATING);
	}
}
