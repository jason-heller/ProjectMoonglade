package procedural.biome.types;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.biome.Biome;

public class GrasslandBiome extends Biome {
	public GrasslandBiome() {
		////////////////////////////////////////////////
		this.name = "Grassland";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 0.1f;
		this.terrainRoughness = 0f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.DEFAULT_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r) {
		return 0;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}
}
