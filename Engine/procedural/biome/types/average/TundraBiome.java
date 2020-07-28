package procedural.biome.types.average;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class TundraBiome extends Biome {

	public TundraBiome() {
		////////////////////////////////////////////////
		this.name = "Tundra";
		this.temp = Temperature.FREEZING;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 0f;
		this.terrainRoughness = 0.5f;
		
		this.soilQuality = .1f;
		
		this.skyColor = BiomeColors.SNOW_SKY;
		this.groundColor = BiomeColors.SNOW_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}

	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		return null;
	}

	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}

	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}
}
