package procedural.biome.types.average;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class TropicalForestBiome extends Biome {
	public TropicalForestBiome() {
		////////////////////////////////////////////////
		this.name = "Tropical Forest";
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
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (x % 8 == 0 && z % 8 == 0) {
			double treeDensity = .1f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .2f;
			
			float n = r.nextFloat();
			if (n < treeDensity) {
				switch(r.nextInt() & 0x15) {
				case 0:
					return Props.AGAVE;
				case 1:
					return Props.ROCK;
				default:
					return Props.PALM;
				}
			}
		}
		
		return null;
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
