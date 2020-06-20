package procedural.biome.types.average;

import java.util.Random;

import map.Chunk;
import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class DeciduousForestBiome extends Biome {

	public DeciduousForestBiome() {
		////////////////////////////////////////////////
		this.name = "Deciduous Forest";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 4f;
		this.terrainRoughness = 1.0f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.DECIDUOUSFOREST_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}

	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		int vc = Chunk.VERTEX_COUNT-1;
		int tx = ((x % vc) + vc) % vc;
		int tz = ((z % vc) + vc) % vc;
		
		if (x % 2 == 0 && z % 2 == 0) {
			double treeDensity = .1f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .2f;
			
			float n = r.nextFloat();
			if (n < treeDensity) {
				switch(r.nextInt() & 0x15) {
				case 0:
					return 9;
				default:
					return 6;
				}
			}
		} else if (tx != 0 && tileItems[tx-1][tz] == 6) {
			float n = r.nextFloat();
			if (n < .1) {
				return 12;
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
