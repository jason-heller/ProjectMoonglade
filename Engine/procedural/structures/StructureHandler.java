package procedural.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.Console;
import gl.Camera;
import io.StructureLoader;
import map.Chunk;
import map.Terrain;
import map.prop.Props;
import map.tile.BuildData;
import map.tile.BuildSector;
import map.tile.Tile;
import procedural.biome.BiomeVoronoi;

public class StructureHandler {
	//Have a list of all possible structures
	
	private Terrain terrain;
	public Map<Structure, StructureData> structures;
	private StructureSpawner spawner;

	private StructurePositionMap saveDataQueue;
	// When a structure is placed into the world, any chunks that are unloaded that it overlaps will will need to 
	// save it's existence so that they can finish building the structure upon their loading
	//
	// So when a structure is made, it'll check for unloaded chunks it overlaps, each one will have it's X, Z saved into the
	// keys of this map, and the value will be the structures X, Z, and id in the structures datastruct

	private void loadStructures() {
		structures.put(Structure.PYLON, StructureLoader.read("pylon.str"));
		structures.put(Structure.TOMB, StructureLoader.read("tomb.str"));
	}
	
	public StructureHandler(Terrain terrain, BiomeVoronoi biomeVoronoi, long seed) {
		this.terrain = terrain;
		saveDataQueue = new StructurePositionMap();
		structures = new HashMap<Structure, StructureData>();
		
		loadStructures();
		
		spawner = new StructureSpawner(this, biomeVoronoi, seed);
	}
	

	/** Adds a structure to the map
	 * @param chunk
	 * @param x
	 * @param y
	 * @param z
	 * @param struct
	 */
	public void addStructure(int x, int y, int z, Structure struct, int quadrantIndex) {
		StructureData data = structures.get(struct);

		int quadrantSize = StructureSpawner.quadrants[quadrantIndex];
		int tilesPerQuadrant = Chunk.CHUNK_SIZE * quadrantSize;
		final int width  = (int) Math.ceil(data.getWidth()  / (float)tilesPerQuadrant);
		final int length = (int) Math.ceil(data.getLength() / (float)tilesPerQuadrant);
		
		int regX = Math.floorDiv(x, tilesPerQuadrant);
		int regZ = Math.floorDiv(z, tilesPerQuadrant);
		
		for(int i = 0; i <= width; i++) {
			for(int j = 0; j <= length; j++) {
				int dataX = regX + i;
				int dataZ = regZ + j;
				List<StructPlacement> list;

				//if (arrX > Terrain.size || arrZ > Terrain.size) {
					Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(quadrantIndex, dataX);
					if (batch == null) {
						batch = new HashMap<Integer, List<StructPlacement>>();
						list = new ArrayList<StructPlacement>();
						batch.put(dataZ, list);
						saveDataQueue.put(quadrantIndex, dataX, batch);
					} else {
						list = batch.get(dataZ);
						
						if (list == null) {
							list = new ArrayList<StructPlacement>();
						}
						batch.put(dataZ, list);
					}
					
					//y = (int)Math.floor(terrain.getHeightAt(x, z)-.5f);
					
					list.add(new StructPlacement(x, y, z, struct));
					/* }else {
					buildStructurePartial(terrain.get(arrX, arrZ), x, y, z, struct);
				}*/
			}
		}
	}
	
	public void checkForStructure(Chunk chunk) {
		for(int quadrantIndex = 0; quadrantIndex < StructureSpawner.quadrants.length; quadrantIndex++) {
			int quadrant = StructureSpawner.quadrants[quadrantIndex];
			int x = Math.floorDiv(chunk.dataX, quadrant);
			int z = Math.floorDiv(chunk.dataZ, quadrant);
			
			Map<Integer, List<StructPlacement>> batch = saveDataQueue.get(quadrantIndex, x);
			
			if (batch == null) {
				continue;
			}
			
			List<StructPlacement> structPlacements = batch.get(z);
			
			if (structPlacements != null) {
				Iterator<StructPlacement> iter = structPlacements.iterator();
				while(iter.hasNext()) {
					StructPlacement structPlacement = iter.next();
					buildStructurePartial(chunk, structPlacement);
					//iter.remove();
				}
				
				//batch.remove(z);
			}
			
			/*if (batch.size() == 0) {
				saveDataQueue.remove(quadrantIndex, x);
			}*/
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
		
		if (offsetY == 0) {
			//offsetY = (int)chunk.heightmap[dx][dz];
		}
		
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
	
	// For debugging ONLY
	public void flush() {
		for(int quadrantIndex = 0; quadrantIndex < StructureSpawner.quadrants.length; quadrantIndex++) {
			for(int arrX : saveDataQueue.keySet(quadrantIndex)) {
				for(int arrZ : saveDataQueue.get(quadrantIndex, arrX).keySet()) {
					for(StructPlacement strPlace : saveDataQueue.get(quadrantIndex, arrX).get(arrZ)) {
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
	}


	public void update(Camera camera) {
		spawner.update(camera);
	}


	public void resizeSpawnRadius() {
		spawner.resize();
	}


	public void clear(int quadrantIndex) {
		saveDataQueue.clear(quadrantIndex);
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
