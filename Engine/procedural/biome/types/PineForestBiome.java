package procedural.biome.types;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;

public class PineForestBiome extends Biome {

	public PineForestBiome() {
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
		if (x % 2 == 0 && z % 2 == 0) {
			final float treeDensity = .08f;
			
			float n = r.nextFloat();
			if (n < treeDensity) {
				return 9;
			}
		}
		return 0;
	}

	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}

}
