package procedural.biome.types.dry;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class ButteBiome extends Biome {
	public ButteBiome() {
		////////////////////////////////////////////////
		this.name = "Butte";
		this.temp = Temperature.HOT;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 20f;
		this.terrainRoughness = 2f;
		//this.terrainTransitionScale = 4f;
		
		this.soilQuality = .1f;
		
		this.groundTx = 1 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.DESERT_SKY;
		this.groundColor = BiomeColors.SAND_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return (float) (11f / (1f + Math.exp(-2*(currentHeight-4f))));
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		int tile = r.nextInt(520);
		switch(tile) { 
		case 0: return 4;
		case 1: return 7;
		case 2: return 8;
		}
		return 0;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, float currentHeight, int subseed, Random r) {
		return null;
	}
}
