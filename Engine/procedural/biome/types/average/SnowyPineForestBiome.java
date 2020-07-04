package procedural.biome.types.average;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class SnowyPineForestBiome extends Biome {

	public SnowyPineForestBiome() {
		////////////////////////////////////////////////
		this.name = "snowy Pine Forest";
		this.temp = Temperature.COLD;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 4f;
		this.terrainRoughness = 1.0f;
		
		this.soilQuality = .3f;
		
		this.skyColor = BiomeColors.SNOW_SKY;
		this.groundColor = BiomeColors.SNOW_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}

	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		if (x % 2 == 0 && z % 2 == 0) {
			final float treeDensity = .03f;
			
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

	@Override
	public Structure getTerrainStructures(int x, int z, float currentHeight, int subseed, Random r) {
		return null;
	}
}
