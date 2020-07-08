package procedural.biome.types.dry;

import java.util.Random;

import map.Moisture;
import map.Temperature;
import map.prop.Props;
import procedural.biome.Biome;
import procedural.biome.types.BiomeColors;
import procedural.structures.Structure;

public class GrasslandBiome extends Biome {
	public GrasslandBiome() {
		////////////////////////////////////////////////
		this.name = "Grassland";
		this.temp = Temperature.TEMPERATE;
		this.moisture = Moisture.DRY;
		this.terrainHeightFactor = 0.1f;
		this.terrainRoughness = 0f;
		
		this.skyColor = BiomeColors.DEFAULT_SKY;
		this.groundColor = BiomeColors.DEFAULT_GRASS;
		////////////////////////////////////////////////
	}

	@Override
	public float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r) {
		return currentHeight;
	}
	
	@Override
	public Props getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r, Props[][] tileItems) {
		int n = r.nextInt(2400);
		if (n == 42) {
			return Props.BUSH;
		}
		
		return null;
	}
	
	@Override
	public float getWaterTable(int x, int z, float height, int subseed) {
		return Float.MIN_VALUE;
	}
	
	@Override
	public Structure getTerrainStructures(int x, int z, float currentHeight, int subseed, Random r) {
		/*if (x==0&&z==0) {
			return Structure.BUTTE;
		}*/
		
		return null;
	}
}
