package procedural.biome.types.average;

import java.util.Random;

import map.Chunk;
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
		this.terrainHeightFactor = 6f;
		this.terrainRoughness = .3f;
		
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
		float n = r.nextFloat();
		
		if (currentHeight < -3f) {
			if (n < .0001f) {
				return Props.MANGROVE;
			}
			return null;
		}
		
		int vc = Chunk.VERTEX_COUNT-1;
		int tx = ((x % vc) + vc) % vc;
		int tz = ((z % vc) + vc) % vc;
		if (x % 8 == 0 && z % 8 == 0) {
			double treeDensity = .15f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .2f;
			
			if (n < treeDensity) {
				switch(r.nextInt(10)) {
				case 0:
					return Props.AGAVE;
				case 1:
					return Props.ROCK;
				case 2:
					return Props.MANGROVE;
				default:
					return Props.PALM;
				}
			} else if (n < treeDensity * 4) {
				return Props.EVERGREEN;
			}
		} else if (tx != 0 && tileItems[tx-1][tz] == Props.EVERGREEN) {
				n = r.nextFloat();
				if (n < .2) {
					return Props.VINE;
				}
			}
		
		if (n > .99) {
			return Props.BIG_BUSH;
		}
		
		return null;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -3f;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}
}
