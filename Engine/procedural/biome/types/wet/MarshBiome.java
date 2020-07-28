package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class MarshBiome extends Biome {
	public MarshBiome() {
		////////////////////////////////////////////////
		this.name = "Marsh";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 2f;
		this.terrainRoughness = .5f;
		this.terrainTransitionScale = 3f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.FEN_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return -0.5f + Math.abs(currentHeight);
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (currentHeight >= -0.25f && r.nextInt(5) == 0) {
			return Props.REED;
		}
		return null;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -0.25f;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}
}
