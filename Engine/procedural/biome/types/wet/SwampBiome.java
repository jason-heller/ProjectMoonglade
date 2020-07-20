package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class SwampBiome extends Biome {
	public SwampBiome() {
		////////////////////////////////////////////////
		this.name = "Swamp";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 4f;
		this.terrainRoughness = .5f;
		this.terrainTransitionScale = 3f;
		this.waterColor = BiomeColors.SWAMP_WATER;
		
		this.groundTx = 4 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.SWAMP_SKY;
		this.groundColor = BiomeColors.SWAMP_MUD;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return -.75f + currentHeight;
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (r.nextInt(200) == 1) {
			return Props.ROCK;
		}
		
		double treeDensity = -.2f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .7f;
		
		if (treeDensity > 0 && (r.nextInt() % 50) == 0) {
			if (r.nextBoolean()) {
				return Props.CYPRESS_BIG;
			} else {
				return Props.CYPRESS;
			}
		}
		
		if (currentHeight < -.25f) {
			if (treeDensity > 0 && (r.nextInt() % 5) == 0) {
				return Props.DUCKWEED;
			}
		}
			
		return null;
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
