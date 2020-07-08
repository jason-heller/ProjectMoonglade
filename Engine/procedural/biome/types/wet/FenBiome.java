package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
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
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (currentHeight > -1 && currentHeight < 0.1f) {
			int n = r.nextInt(10);
			if (n == 1) {
				return Props.REED;
			}
		}
		
		if (currentHeight >= 0) {
			double noise = NoiseUtil.interpNoise2d(x/4f, z/4f, subseed);
			if (noise > .2) {
				if (noise > .87) {
					int n = r.nextInt(20);
					if (n == 1) {
						return Props.THIN_TREE;
					}
				}
				return Props.BUSH;
			} else if (noise < -.9) {
				if (r.nextInt(30) == 1) {
					return Props.DEAD_TREE;
				}
			} else {
				if (r.nextInt(30) == 1) {
					return Props.ROCK;
				}
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
