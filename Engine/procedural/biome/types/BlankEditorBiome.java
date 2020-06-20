package procedural.biome.types;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.biome.Biome;
import procedural.structures.Structure;
import ui.Colors;

public class BlankEditorBiome extends Biome {
	public BlankEditorBiome() {
		////////////////////////////////////////////////
		this.name = "Edit";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 0f;
		this.terrainRoughness = 0f;
		
		this.skyColor = Colors.BLACK;
		this.groundColor = Colors.LT_SILVER;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, int[][] tileItems) {
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
