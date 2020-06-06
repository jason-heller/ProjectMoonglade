package procedural.biome;

import java.util.Random;

import org.joml.Vector3f;

import map.Moisture;
import map.Temperature;
import procedural.biome.types.BiomeColors;

public abstract class Biome {
	
	//NAME("name", TEMPERATURE, MOISTURE,
	// max_height, max_skew, perlin_roughness, perlin_scale (+.0015)
	
	/*PLAINS("Plains", TEMPERATE, AVERAGE,
			2.3f, 0, 1, DEFAULT_SKY, DEFAULT_GRASS),
	HIGH_HILLS("High Hills", TEMPERATE, AVERAGE,
			12.5f, .8f, 8, DEFAULT_SKY, SNOW_COLOR),
	DESERT("Desert", HOT, DRY,
			2f, 0, 8, DESERT_SKY, SAND_COLOR),
	TEST("TEST", TEMPERATE, WET,
			12.5f, 1.4f, 0, DEFAULT_SKY, new Vector3f(.75f, 0, 0));*/
	
	protected String name;
	protected Temperature temp;
	protected Moisture moisture;
	
	public float terrainHeightFactor = 1f;
	public float terrainRoughness = 1;
	
	public Vector3f skyColor = BiomeColors.DEFAULT_SKY;
	public Vector3f groundColor = BiomeColors.DEFAULT_GRASS;
	
	public abstract float augmentTerrainHeight(int x, int z, float currentHeight, int subseed, Random r);
	public abstract int getTerrainTileItems(int x, int z, float currentHeight, int subseed, Random r);
	public abstract float getWaterTable(int x, int z, float height, int subseed);
	
	public Moisture getMoisture() {
		return moisture;
	}

	public Temperature getTemperature() {
		return temp;
	}

	public String getName() {
		return name;
	}
	
	public Vector3f getGroundColor() {
		return this.groundColor;
	}

	public Vector3f getSkyColor() {
		return this.skyColor;
	}
	
}
