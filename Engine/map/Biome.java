package map;

import static map.Moisture.*;
import static map.Temperature.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import dev.Console;

import static map.BiomeColors.*;

public enum Biome {
	
	//NAME("name", TEMPERATURE, MOISTURE,
	// max_height, max_skew, perlin_roughness, perlin_scale (+.0015)
	
	PLAINS("Plains", TEMPERATE, AVERAGE,
			2.3f, 0, 1, DEFAULT_SKY, DEFAULT_GRASS),
	HIGH_HILLS("High Hills", TEMPERATE, AVERAGE,
			12.5f, .8f, 8, DEFAULT_SKY, SNOW_COLOR),
	DESERT("Desert", HOT, DRY,
			2f, 0, 8, DESERT_SKY, SAND_COLOR),
	TEST("TEST", TEMPERATE, WET,
			12.5f, 1.4f, 0, DEFAULT_SKY, new Vector3f(.75f, 0, 0));
	
	private String name;
	private Temperature temp;
	private Moisture moisture;
	
	public float terrainHeightFactor = 1f;
	public float terrainCliffFactor = 1f;
	public float terrainRoughness = 1;
	
	public Vector3f skyColor;
	public Vector3f groundColor;
	
	private Biome(String name, Temperature temp, Moisture moisture,
			float terrainHeightFactor, float terrainCliffFactor, float terrainRoughness, Vector3f skyTint, Vector3f groundColor) {
		// Climate
		this.name = name;
		this.temp = temp;
		this.moisture = moisture;
		
		// Terrain
		this.terrainHeightFactor = terrainHeightFactor;
		this.terrainCliffFactor = terrainCliffFactor;
		this.terrainRoughness = terrainRoughness;
		this.skyColor = skyTint;
		this.groundColor = groundColor;
		
		Map<Moisture, List<Biome>> section = Enviroment.biomeMap.get(temp);
		if (section == null) {
			section = new HashMap<Moisture, List<Biome>>();
			Enviroment.biomeMap.put(temp, section);
		}
		
		List<Biome> batch = section.get(moisture);
		if (batch == null) {
			batch = new ArrayList<Biome>();
			section.put(moisture, batch);
		}
		
		Console.log(moisture, temp, this.getName());
		batch.add(this);
	}
	
	public Moisture getMoisture() {
		return moisture;
	}

	public Temperature getTemperature() {
		return temp;
	}

	public String getName() {
		return name;
	}
	
	@Deprecated
	public static Biome getBiome(Temperature temp, Moisture moisture, float biomeRng) {
		// TODO: Incorporate temp & moisties
		/*Map<Moisture, List<Biome>> section = Enviroment.biomeMap.get(temp);
		if (section == null) {
			section = Enviroment.biomeMap.get(Temperature.TEMPERATE);
		}
		
		List<Biome> batch = section.get(moisture);
		if (batch == null) {
			return CRAIG;
		}
		
		int biomeNum = batch.size();
		int index = 0;//(int) (biomeFactor / (1f/biomeNum));
		
		
		return batch.get(index);*/
		return Biome.values()[(int)(biomeRng*(Biome.values().length-1))];
	}

	public static int getNumBiomesInClimate(Temperature temp, Moisture wet) {
		Map<Moisture, List<Biome>> section = Enviroment.biomeMap.get(temp);
		if (section == null) {
			return 1;
		}
		
		List<Biome> batch = section.get(wet);
		if (batch == null) {
			return 1;
		}
		
		return batch.size();
	}
	
	public Vector3f getGroundColor() {
		return this.groundColor;
	}

	public Vector3f getSkyColor() {
		return this.skyColor;
	}
}
