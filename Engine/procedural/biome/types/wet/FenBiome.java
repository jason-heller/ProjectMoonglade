package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class FenBiome extends Biome {
	public FenBiome() {
		////////////////////////////////////////////////
		this.name = "Fen";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 1f;
		this.terrainRoughness = 50f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.FEN_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		if (currentHeight <= -.5f) {
			return (currentHeight) + (currentHeight+.5f)*5f;
		}
		
		return currentHeight;
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		if (currentHeight > -1 && currentHeight < 0.1f) {
			int n = r.nextInt(10);
			if (n == 1) {
				return 5;
			}
		}
		
		if (currentHeight >= 0) {
			double noise = NoiseUtil.interpNoise2d(x/4f, z/4f, subseed);
			if (noise > .2) {
				if (noise > .87) {
					int n = r.nextInt(20);
					if (n == 1) {
						return 2;
					}
				}
				return 1;
			} else if (noise < -.9) {
				if (r.nextInt(30) == 1) {
					return 3;
				}
			} else {
				if (r.nextInt(30) == 1) {
					return 4;
				}
			}
		}
		return 0;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -0.25f;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, float currentHeight, int subseed, Random r) {
		return null;
	}
}
