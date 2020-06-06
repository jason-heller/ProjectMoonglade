package procedural.biome.types;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.biome.Biome;

public class SnowyPineForestBiome extends Biome {

	public SnowyPineForestBiome() {
		////////////////////////////////////////////////
		this.name = "snowy Pine Forest";
		this.temp = Temperature.COLD;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 4f;
		this.terrainRoughness = 1.0f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.CONIFERFOREST_GRASS;
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
