package procedural.biome.types.average;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class PineForestBiome extends Biome {

	public PineForestBiome() {
		////////////////////////////////////////////////
		this.name = "Pine Forest";
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
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		float n = r.nextFloat();
		if (x % 2 == 0 && z % 2 == 0) {
			final float treeDensity = .08f;
			
			
			if (n < treeDensity) {
				return Props.PINE;
			}
		}
		
		if (n > .999) {
			return Props.BIG_BUSH;
		}
		if (n < .003f) {
			return Props.BERRY_BUSH;
		}
		if (n < .001f) {
			return Props.BUSH;
		}
		if (n < .01f) {
			return Props.GRASS;
		}
		
		return null;
	}

	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}
	@Override
	public Structure getTerrainStructures(int x, int z, float currentHeight, int subseed, Random r) {
		if (r.nextInt(45000) == 0) {
			return Structure.PYLON;
		}
		
		return null;
	}

}
