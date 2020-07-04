package procedural.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.Console;
import io.StructureLoader;
import map.Chunk;
import map.Terrain;
import map.tile.BuildData;

public class StructureHandler {
	//Have a list of all possible structures
	
	private Terrain terrain;
	public Map<Structure, StructureData> structures;

	private Map<Integer, Map<Integer, List<StructPlacement>>> saveDataQueue;	// Data structure hell
	// When a structure is placed into the world, any chunks that are unloaded that it overlaps will will need to 
	// save it's existence so that they can finish building the structure upon their loading
	//
	// So when a structure is made, it'll check for unloaded chunks it overlaps, each one will have it's X, Z saved into the
	// keys of this map, and the value will be the structures X, Z, and id in the structures datastruct

	
	public StructureHandler(Terrain terrain) {
		this.terrain = terrain;
		saveDataQueue = new HashMap<Integer, Map<Integer, List<StructPlacement>>>();
		
		structures = new HashMap<Structure, StructureData>();
		structures.put(Structure.BUTTE, StructureLoader.read("butte.str"));
	}
	
	// TODO: On chunk generation, GenTerrain everything, THEN add a structure sweet of the chunks to add structures, then build the models
	
	public void addStructure(Chunk chunk, int x, int y, int z, Structure struct) {
		StructureData data = structures.get(struct);
		final int width = data.getWidth();
		final int length = data.getLength();
		
		for(int i = 0; i < width; i += Chunk.CHUNK_SIZE) {
			for(int j = 0; j < length; j += Chunk.CHUNK_SIZE) {
				int arrX = chunk.arrX + i;
				int arrZ = chunk.arrZ + j;
				List<StructPlacement> list;
				
				//if (arrX > Terrain.size || arrZ > Terrain.size) {
					Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(arrX);
					if (batch == null) {
						batch = new HashMap<Integer, List<StructPlacement>>();
						list = new ArrayList<StructPlacement>();
						batch.put(arrZ, list);
						saveDataQueue.put(arrX, batch);
					} else {
						list = batch.get(arrZ);
						
						if (list == null) {
							list = new ArrayList<StructPlacement>();
						}
						batch.put(arrZ, list);
					}
					
					list.add(new StructPlacement(x, y, z, struct));
					/* }else {
					buildStructurePartial(terrain.get(arrX, arrZ), x, y, z, struct);
				}*/
			}
		}
	}
	
	public void checkForStructure(Chunk chunk) {
		Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(chunk.arrX);
		
		if (batch == null) {
			return;
		}
		
		List<StructPlacement> structPlacements = batch.get(chunk.arrZ);
		
		if (structPlacements != null) {
			Iterator<StructPlacement> iter = structPlacements.iterator();
			while(iter.hasNext()) {
				StructPlacement structPlacement = iter.next();
				final int x = structPlacement.x;
				final int y = structPlacement.y;
				final int z = structPlacement.z;
				buildStructurePartial(chunk, x, y, z, structPlacement);
				iter.remove();
			}
		}
	}

	private void buildStructurePartial(Chunk chunk, int x, int y, int z, StructPlacement structPlacement) {
		// X, Y, Z, should be within chunk bounds!!
		int dx = x - chunk.realX;
		int dz = z - chunk.realZ;

		Structure struct = structPlacement.struct;
		StructureData data = structures.get(struct);
		
		int width = Math.min(Chunk.CHUNK_SIZE-dx, data.getWidth());
		int length = Math.min(Chunk.CHUNK_SIZE-dz, data.getLength());
		
		boolean hasTerrain = data.hasTerrain();
		boolean hasBuildingTiles = data.hasBuildingTiles();
		boolean hasEnvTiles = data.hasEnvTiles();
		
		BuildData building = chunk.getBuilding();
		int[][] envTiles = chunk.chunkProps.getTilemap();
		
		for(int i = dx; i < width; i++) {
			for(int j = dz; j < length; j++) {
				//int realX = i + chunk.realX;
				//int relaZ = j + chunk.realZ;
				
				if (hasTerrain) {
					chunk.heightmap[i][j] = data.getTerrain(i, j);
				}
				
				if (hasBuildingTiles) {
					for(int k = 0; k < data.getHeight(); k++) {
						CompTileData tile = data.getBuildingTile(i, k, j);
						if (tile != null)
							building.setTile(i, k, j, tile.getWalls(), tile.getSlope(), tile.getMaterials(), tile.getFlags());
					}
				}
				
				if (hasEnvTiles) {
					envTiles[i][j] = data.getEnvTile(i, j);
				}
			}
		}
		
		if (hasBuildingTiles) {
			chunk.getBuilding().buildModel();
		}
		
		// Remove from queue
		//Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(chunk.arrX);
		//batch.get(chunk.arrZ).remove(structPlacement);
	}
}

class StructPlacement {
	public int x, y, z;
	public Structure struct;
	
	public StructPlacement(int x, int y, int z, Structure struct) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.struct = struct;
	}
}
