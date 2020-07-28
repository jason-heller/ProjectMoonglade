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

public class MysticForestBiome extends Biome {

	public MysticForestBiome() {
		////////////////////////////////////////////////
		this.name = "Mystic Forest";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.AVERAGE;
		this.terrainHeightFactor = 15f;
		this.terrainRoughness = .75f;
		this.terrainTransitionScale = 2f;
		
		this.skyColor = BiomeColors.MYSTIC_SKY;
		this.groundColor = BiomeColors.MYSTIC_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}

	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		int vc = Chunk.VERTEX_COUNT-1;
		int tx = ((x % vc) + vc) % vc;
		int tz = ((z % vc) + vc) % vc;
		
		float n = r.nextInt(100);
		if (currentHeight < -1f) {
			if (n <= 1) {
				return Props.LILYPAD;
			}
		} else {
			
			if (n == 0) {
				return Props.MYSTIC_BUSH;
			} else if (n < 5) {
				return Props.PURPLE_FLOWERS;
			} 
			
			if (r.nextInt(400) == 0) {
				return Props.ENCHANTED_TREE;
			} else if (r.nextInt(250) < 3) {
				return Props.CHERRY_BLOSSOM;
			}
		}
		
		return null;
	}

	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return -1f;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, int subseed, Random r, int quadrantSize) {
		return null;
	}

}
