package procedural.biome.types.dry;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class DesertBiome extends Biome {
	public DesertBiome() {
		////////////////////////////////////////////////
		this.name = "Desert";
		this.temp = Temperature.HOT;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 2f;
		this.terrainRoughness = 8f;
		
		this.soilQuality = .1f;
		
		this.groundTx = 1 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		
		this.skyColor = BiomeColors.DESERT_SKY;
		this.groundColor = BiomeColors.SAND_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		int tile = r.nextInt(520);
		switch(tile) { 
		case 0: return Props.ROCK;
		case 1: return Props.AGAVE;
		case 2: return Props.CACTUS;
		}
		return null;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}
	
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		switch(quadrantSize) {
		case 2:
			if (r.nextInt(50) == 0) {
				return Structure.TOMB;
			}
			break;
		}
		
		return null;
	}
}
