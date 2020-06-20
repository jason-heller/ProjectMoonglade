package procedural.biome.types.average;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class TropicalForestBiome extends Biome {
	public TropicalForestBiome() {
		////////////////////////////////////////////////
		this.name = "Desert";
		this.temp = Temperature.WARM;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 2f;
		this.terrainRoughness = 11f;
		
		this.skyColor = BiomeColors.TROPIC_SKY;
		this.groundColor = BiomeColors.TROPIC_GRASS_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		if (x % 8 == 0 && z % 8 == 0) {
			double treeDensity = .1f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .2f;
			
			float n = r.nextFloat();
			if (n < treeDensity) {
				switch(r.nextInt() & 0x15) {
				case 0:
					return 7;
				case 1:
					return 4;
				default:
					return 11;
				}
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
