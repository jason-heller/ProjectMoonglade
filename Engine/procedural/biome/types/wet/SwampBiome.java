package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class SwampBiome extends Biome {
	public SwampBiome() {
		////////////////////////////////////////////////
		this.name = "Swamp";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 1f;
		this.terrainRoughness = 50f;
		this.terrainTransitionScale = 4f;
		this.waterColor = BiomeColors.SWAMP_WATER;
		
		this.skyColor = BiomeColors.SWAMP_SKY;
		this.groundColor = BiomeColors.SWAMP_MUD;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight-1f;
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (currentHeight >= 0) {
			if (r.nextInt(30) == 1) {
				return Props.BUSH;
			}
		} else {
			if (r.nextInt(200) == 1) {
				return Props.ROCK;
			}
		}
		
		if (r.nextInt(100) == 0) {
			return Props.CYPRESS;
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
