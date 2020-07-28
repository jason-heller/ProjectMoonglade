package procedural.biome.types.wet;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class OceanBiome extends Biome {
	public OceanBiome() {
		////////////////////////////////////////////////
		this.name = "Ocean";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 8f;
		this.terrainRoughness = 64f;
		this.shoreSize = .2f;
		
		this.soilQuality = 1f;
		
		this.groundTx = 1 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.SAND_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return -35f;// + currentHeight;//(float) (65f / (1f + Math.exp(-.1f * (currentHeight - 2f))));

	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		if (currentHeight > -2) {
			int n = r.nextInt(4000);
			if (n == 0) {
				return map.prop.Props.PALM;
			}
		}
		
		return null;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -0.5f;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}
}
