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

public class ColdMountainBiome extends Biome {
	public ColdMountainBiome() {
		////////////////////////////////////////////////
		this.name = "Mountain";
		this.temp = Temperature.COLD;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 50;
		this.terrainRoughness = .1f;
		
		this.soilQuality = .1f;
		
		this.groundTx = 1 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.SNOW_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return (float) (terrainHeightFactor / (1f + Math.exp(-.1f * (currentHeight - 4f))));
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
		if (currentHeight > 29f) {
			return 0;
		}
		
		if (x % 2 == 0 && z % 2 == 0) {
			final float treeDensity = .02f;
			
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
