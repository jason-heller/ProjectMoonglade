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
import map.prop.Props;
import map.tile.BuildData;
import map.tile.BuildSector;
import map.tile.Tile;

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
		structures.put(Structure.PYLON, StructureLoader.read("pylon.str"));
	}
	
	
	/** Adds a structure to the map
	 * @param chunk
	 * @param x
	 * @param y
	 * @param z
	 * @param struct
	 */
	public void addStructure(Chunk chunk, int x, int y, int z, Structure struct) {
		StructureData data = structures.get(struct);
		final int width = (int) Math.ceil(data.getWidth() / (float)Chunk.CHUNK_SIZE);
		final int length = (int) Math.ceil(data.getLength() / (float)Chunk.CHUNK_SIZE);
		
		for(int i = 0; i <= width; i ++) {
			for(int j = 0; j <= length; j ++) {
				int dataX = chunk.dataX + i;// * Chunk.CHUNK_SIZE;
				int dataZ = chunk.dataZ + j;// * Chunk.CHUNK_SIZE;
				List<StructPlacement> list;

				//if (arrX > Terrain.size || arrZ > Terrain.size) {
					Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(dataX);
					if (batch == null) {
						batch = new HashMap<Integer, List<StructPlacement>>();
						list = new ArrayList<StructPlacement>();
						batch.put(dataZ, list);
						saveDataQueue.put(dataX, batch);
					} else {
						list = batch.get(dataZ);
						
						if (list == null) {
							list = new ArrayList<StructPlacement>();
						}
						batch.put(dataZ, list);
					}
					
					y = (int)Math.floor(terrain.getHeightAt(x, z)-.5f);
					
					list.add(new StructPlacement(x, y, z, struct));
					Console.log("added structure to ",dataX,dataZ);
					/* }else {
					buildStructurePartial(terrain.get(arrX, arrZ), x, y, z, struct);
				}*/
			}
		}
	}
	
	// For debugging ONLY
	public void flush() {
		for(int arrX : saveDataQueue.keySet()) {
			for(int arrZ : saveDataQueue.get(arrX).keySet()) {
				for(StructPlacement strPlace : saveDataQueue.get(arrX).get(arrZ)) {
					for(Chunk[] stripe : terrain.get()) {
						for(Chunk chunk : stripe) {
							if (chunk.arrX == arrX && chunk.arrZ == arrZ) {
								buildStructurePartial(chunk, strPlace);
								chunk.getBuilding().buildModel();
							}
						}
					}
				}
			}
		}
	}
	
	public void checkForStructure(Chunk chunk) {
		Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(chunk.dataX);
		
		if (batch == null) {
			return;
		}
		
		List<StructPlacement> structPlacements = batch.get(chunk.dataZ);
		
		if (structPlacements != null) {
			Iterator<StructPlacement> iter = structPlacements.iterator();
			while(iter.hasNext()) {
				StructPlacement structPlacement = iter.next();
				buildStructurePartial(chunk, structPlacement);
				iter.remove();
				//Console.log("removed",chunk.dataX,chunk.dataZ);
			}
		}
	}

	private void buildStructurePartial(Chunk chunk, StructPlacement structPlacement) {
		// X, Y, Z, should be within chunk bounds!!
		int dx = structPlacement.x - chunk.realX;
		int dz = structPlacement.z - chunk.realZ;
		
		int offsetX = -Math.min(dx, 0);
		int offsetZ = -Math.min(dz, 0);
		
		dx += offsetX;
		dz += offsetZ;

		Structure struct = structPlacement.struct;
		StructureData data = structures.get(struct);
		
		int width = Math.min(Chunk.CHUNK_SIZE-dx, data.getWidth()-offsetX);
		int length = Math.min(Chunk.CHUNK_SIZE-dz, data.getLength()-offsetZ);
		
		boolean hasTerrain = data.hasTerrain();
		boolean hasBuildingTiles = data.hasBuildingTiles();
		boolean hasEnvTiles = data.hasEnvTiles();
		
		BuildData building = chunk.getBuilding();
		Props[][] envTiles = chunk.chunkProps.getPropMap();
		int tileX = 0, tileZ = 0;
		
		int offsetY = structPlacement.y;
		
		for(int i = dx; i < dx+width; i++) {
			for(int j = dz; j < dz+length; j++) {
				//int realX = i + chunk.realX;
				//int relaZ = j + chunk.realZ;
				
				tileX = i - dx;
				tileZ = j - dz;
				
				if (hasTerrain) {
					chunk.heightmap[i][j] = data.getTerrain(i, j);
				}
				
				if (hasBuildingTiles) {
					for(int k = 0; k < data.getHeight(); k++) {
						CompTileData tile = data.getBuildingTile(tileX + offsetX, k, tileZ + offsetZ);
						if (tile != null) {
							int sx = Math.floorDiv(i, 8);
							int sy = Math.floorDiv(k+offsetY, 8);
							int sz = Math.floorDiv(j, 8);
							BuildSector sector = building.getSector(sx, sy, sz);
							if (sector == null) {
								sector = building.addSector(sx, sy, sz);
							}
							sector.addTile(new Tile(tile.getMaterials(), tile.getFlags()), i - (sx*8), (k+offsetY) - (sy*8), j - (sz*8));
						}
					}
				}
				
				if (hasEnvTiles) {
					envTiles[i][j] = data.getProp(i, j);
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


	public Terrain getTerrain() {
		return terrain;
	}
}

class StructPlacement {
	public int x, y, z;
	public Structure struct;
	//public boolean mirrorY, mirrorX;
	
	public StructPlacement(int x, int y, int z, Structure struct) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.struct = struct;
		//this.mirrorX = (Math.random() > .5);
		//this.mirrorY = (Math.random() > .5);
	}
}
