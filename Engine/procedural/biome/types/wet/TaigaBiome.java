package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class TaigaBiome extends Biome {

	public TaigaBiome() {
		////////////////////////////////////////////////
		this.name = "Taiga";
		this.temp = Temperature.COLD;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 30f;
		this.terrainRoughness = .5f;
		
		this.soilQuality = .3f;
		
		this.skyColor = BiomeColors.SNOW_SKY;
		this.groundColor = BiomeColors.TAIGA_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}

	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (currentHeight > -5 && x % 2 == 0 && z % 2 == 0) {
			double treeDensity = .15f + NoiseUtil.interpNoise2d(x/64f, z/64f, subseed) * .1f;
			
			float n = r.nextFloat();
			if (n < treeDensity) {
				switch(r.nextInt() & 0x20) {
				case 0:
					return Props.THIN_TREE;
				default:
					return Props.PINE;
				}
			}
		}
		return null;
	}

	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -5;
	}

	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}
}
