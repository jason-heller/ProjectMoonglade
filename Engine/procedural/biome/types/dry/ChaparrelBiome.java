package procedural.biome.types.dry;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.NoiseUtil;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;
import procedural.terrain.TerrainMeshBuilder;

public class ChaparrelBiome extends Biome {
	public ChaparrelBiome() {
		////////////////////////////////////////////////
		this.name = "Chaparrel";
		this.temp = Temperature.HOT;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 19f;
		this.terrainRoughness = .5f;
		this.terrainTransitionScale = 2.75f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.CHAPARREL_GRASS;
		this.groundTx = 2 * TerrainMeshBuilder.TERRAIN_ATLAS_SIZE;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		double density = .125f + NoiseUtil.interpNoise2d(x/24f, z/24f, subseed) * .2f;
		
		float n = r.nextFloat();
		if (n < density) {
			return Props.CHAPARREL_BUSH;
		}
		
		switch(r.nextInt() % 24) {
		case 0: return Props.CHAPARREL_FLOWER;
		case 1: return Props.DEAD_GRASS;
		case 2: return Props.ROCK;
		}
		
		if ((r.nextInt() % 700) == 0) {
			return Props.JOSHUA_TREE;
		}
		
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
