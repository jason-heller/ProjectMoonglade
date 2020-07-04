package procedural.biome.types.dry;

import java.util.Random;

import map.Chunk;
import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class TemperateMountainBiome extends Biome {
	public TemperateMountainBiome() {
		////////////////////////////////////////////////
		this.name = "Mountain";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 12;
		this.terrainRoughness = .1f;
		
		this.soilQuality = .1f;
		
		this.groundTx = 1 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.DEFAULT_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return (float) (65f / (1f + Math.exp(-.1f * (currentHeight - 2f))));
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		if (currentHeight > 32f) {
			int tile = r.nextInt(1000);
			switch(tile) { 
			case 1: return 4;
			case 2: return 1;
			}
			return 0;
		} else if (currentHeight > 24f) {
			int tile = r.nextInt(100);
			switch(tile) {
			case 1: return 4;
			case 2: return 1;
			}
			
			if (tile < 10)
				return 2;
			return 0;
		}
		
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
