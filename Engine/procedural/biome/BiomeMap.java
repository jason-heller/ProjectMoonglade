package procedural.biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import map.Moisture;
import map.Temperature;
import procedural.NoiseUtil;
import procedural.biome.types.DeciduousForestBiome;
import procedural.biome.types.DesertBiome;
import procedural.biome.types.FenBiome;
import procedural.biome.types.GrasslandBiome;
import procedural.biome.types.MangroveBiome;
import procedural.biome.types.PineForestBiome;
import procedural.biome.types.SnowyPineForestBiome;
import procedural.biome.types.TropicalForestBiome;

public class BiomeMap {
	private Map<Temperature, Map<Moisture, List<Biome>>> biomeMap = new HashMap<Temperature, Map<Moisture, List<Biome>>>();
	private List<Biome> allBiomes = new ArrayList<Biome>();
	
	public BiomeMap() {
		addBiome(new DeciduousForestBiome());
		addBiome(new GrasslandBiome());
		addBiome(new DesertBiome());
		addBiome(new FenBiome());
		addBiome(new PineForestBiome());
		addBiome(new SnowyPineForestBiome());
		addBiome(new MangroveBiome());
		addBiome(new TropicalForestBiome());
	}
	
	private void addBiome(Biome biome) {
		allBiomes.add(biome);
		
		Temperature temperature = biome.getTemperature();
		Moisture moisture = biome.getMoisture();
		Map<Moisture, List<Biome>> section = biomeMap.get(temperature);
		if (section == null) {
			section = new HashMap<Moisture, List<Biome>>();
			biomeMap.put(temperature, section);
		}
		
		List<Biome> batch = section.get(moisture);
		if (batch == null) {
			batch = new ArrayList<Biome>();
			section.put(moisture, batch);
		}
		
		batch.add(biome);
	}
	
	public Biome getBiome(int x, int y, int seed, Temperature temperature, Moisture moisture, float randBiomeChange) {
		return randomBiome(x, y, seed);
		/*Map<Moisture, List<Biome>> temps = biomeMap.get(temperature);
		if (temps == null) return randomBiome(x, y, seed);
		List<Biome> compatibleBiomes = temps.get(moisture);
		if (compatibleBiomes == null) return randomBiome(x, y, seed);
		return compatibleBiomes.get((int)(randBiomeChange * compatibleBiomes.size()));*/
	}

	private Biome randomBiome(int x, int y, int seed) {
		return allBiomes.get((int) (NoiseUtil.valueNoise2d(x, y, seed) * allBiomes.size()));
	}
	
	/*private int getNumBiomesInClimate(Temperature temp, Moisture wet) {
		Map<Moisture, List<Biome>> section = biomeMap.get(temp);
		if (section == null) {
			return 1;
		}
		
		List<Biome> batch = section.get(wet);
		if (batch == null) {
			return 1;
		}
		
		return batch.size();
	}*/
}
