package map;

import static map.Chunk.POLYGON_SIZE;
import static map.Chunk.VERTEX_COUNT;

import dev.Debug;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;

public class ChunkCreator {
	private Terrain terrain;
	public BiomeVoronoi biomeVoronoi;
	
	
	public ChunkCreator(Terrain terrain, BiomeVoronoi biomeVoronoi) {
		this.terrain = terrain;
		this.biomeVoronoi = biomeVoronoi;
	}
	
	public void generate(Chunk chunk) {
		final int x = chunk.dataX;
		final int z = chunk.dataZ;
		//loadState = BUILDING;
		final int wid = (VERTEX_COUNT-1);
		if (Debug.flatTerrain) {
			GenTerrain.buildFlatTerrain(chunk, x*wid, 0, z*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		} else {
			GenTerrain.buildTerrain(chunk, x*wid, 0, z*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		}
		
	}
}
