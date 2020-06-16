package procedural.biome.types;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.Biome;

public class MangroveBiome extends Biome {
	public MangroveBiome() {
		////////////////////////////////////////////////
		this.name = "Mangrove Forest";
		this.temp = Temperature.HOT;
		this.moisture = Moisture.WET;
		this.terrainHeightFactor = 1f;
		this.terrainRoughness = 50f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.MUD_COLOR;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		if (currentHeight <= .25f) {
			return (currentHeight) + (currentHeight-.25f)*5f;
		}
		
		return currentHeight;
	}
	
	@Override
	public int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r) {
		if (x % 6 == 0 && z % 6 == 0 && currentHeight > -1) {
			int n = r.nextInt(5);
			if (n == 0) {
				return 10;
			}
		}
		
		return 0;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return 0f;
	}
}
