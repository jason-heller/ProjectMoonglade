package procedural.terrain;

import java.util.Random;

import map.Chunk;
import map.Enviroment;
import map.Moisture;
import map.Temperature;
import procedural.Noise;
import procedural.SimplexNoise;
import procedural.biome.Biome;
import procedural.biome.BiomeData;
import procedural.biome.BiomeVoronoi;

public class GenTerrain {
	private static Noise biomeNoise;
	private static Noise temperatureNoise;
	private static Noise aridnessNoise;
	private static Noise heightNoise;
	
	public static int heightmapIndexCount;
	
	public static float biomeScale = 1f / (Enviroment.biomeScale*Chunk.CHUNK_SIZE);
	
	private static final int octaves = 4;
	private static final float scale = .005f;
	
	public static int seed;
	
	public static void init(int s1, int s2, int s3, int s4) {
		biomeNoise = new SimplexNoise(s1);
		temperatureNoise = new SimplexNoise(s2);
		aridnessNoise = new SimplexNoise(-s2 - 1);
		heightNoise = new SimplexNoise(s3);
		seed = s4;
	}
	
	public static float[][] buildTerrain(Chunk chunk, int x, int y, int z, int vertexStripeSize, int polygonSize, BiomeVoronoi biomeVoronoi) {
		float terrainHeight;
		int terrainTile;
		float[][] heights = chunk.heightmap;
		int[][] tileItems = chunk.items.getTilemap();
		float[][] waterTable = chunk.waterTable;
		
		boolean needsTileItems = (chunk.editFlags & 0x02) == 0;
		boolean needsHeights = (chunk.editFlags & 0x04) == 0;
		
		Random r = new Random();
		r.setSeed(chunk.getSeed());
		
		for(int j = 0; j < vertexStripeSize; j++) {
			for(int i = 0; i < vertexStripeSize; i++) {
				BiomeData biomeData = biomeVoronoi.getDataAt((x+i)*polygonSize, (z+j)*polygonSize);

				boolean l = (i*2) >= 0;
				boolean t = (j*2) >= 0;
				
				if (needsHeights) {
					terrainHeight = getTerrainNoise(x+i, z+j, biomeData, r);
					
					float height = y + terrainHeight;//(float) (Math.floor(y + terrainHeight/.5f)*.5f);
					
					chunk.getMax().y = Math.max(chunk.getMax().y, height);
					chunk.getMin().y = Math.min(chunk.getMin().y, height);
					
					heights[(i*2)+1][(j*2)+1] = height;
					
					if (l)
						heights[(i*2)][(j*2)+1] = height;
					
					if (t)
						heights[(i*2)+1][(j*2)] = height;
					
					if (l && t)
						heights[(i*2)][(j*2)] = height;
				}
				
				if (needsTileItems && i != vertexStripeSize-1 && j != vertexStripeSize-1) {
					terrainTile = getTerrainTileItems(x+i,z+j, heights[(i*2)+1][(j*2)+1], biomeData, r);
					tileItems[i][j] = terrainTile;
				}

				waterTable[i][j] = getTerrainWaterTable(x+i,z+j, heights[(i*2)+1][(j*2)+1], biomeData);
			}
		}
		
		// Fill in edge data
		heights[0][0] = heights[1][1];
		for(int i = 1; i < heights.length; i++) {
			heights[i][0] = heights[i][1];
			heights[0][i] = heights[1][i];
		}
		
		chunk.getMax().y += 10;
		return heights;
	}
	
	public static float[][] buildFlatTerrain(Chunk chunk, int x, int y, int z, int vertexStripeSize, int polygonSize, BiomeVoronoi biomeVoronoi) {
		int terrainTile;
		float[][] heights = chunk.heightmap;
		int[][] tileItems = chunk.items.getTilemap();
		float[][] waterTable = chunk.waterTable;
		
		Random r = new Random();
		r.setSeed(chunk.getSeed());
		
		for(int j = 0; j < vertexStripeSize; j++) {
			for(int i = 0; i < vertexStripeSize; i++) {
				BiomeData biomeData = biomeVoronoi.getDataAt((x+i)*polygonSize, (z+j)*polygonSize);

				if (i != vertexStripeSize-1 && j != vertexStripeSize-1) {
					terrainTile = getTerrainTileItems(x+i,z+j, 0, biomeData, r);
					tileItems[i][j] = terrainTile;
				}

				waterTable[i][j] = getTerrainWaterTable(x+i,z+j, 0, biomeData);
			}
		}
	
		chunk.getMax().y += 10;
		return heights;
	}

	private static int getTerrainTileItems(int x, int z, float currentHeight, BiomeData biomeData, Random r) {
		Biome biome = biomeData.getInfluencingBiomes()[biomeData.mainBiome];
		return biome.getTerrainTileItems(x, z, currentHeight, biomeData.getSubseed(), r);
	}
	
	private static float getTerrainWaterTable(int x, int z, float height, BiomeData biomeData) {
		Biome biome = biomeData.getInfluencingBiomes()[biomeData.mainBiome];
		return biome.getWaterTable(x, z, height, biomeData.getSubseed());
	}

	private static float getTerrainNoise(int x, int z, BiomeData biomeData, Random r) {
		final float roughness = biomeData.getRoughness();
		Biome biome = biomeData.getInfluencingBiomes()[biomeData.mainBiome];
		float height = (float) heightNoise.fbm(x, z, octaves, roughness, scale);
		height *= biomeData.getTerrainFactor();
		height = (biome.augmentTerrainHeight(x, z, height, biomeData.getSubseed(), r)*biomeData.getInfluence()[biomeData.mainBiome]);
		
		return height;
	}

	public static float[] getClimateProperties(float x, float z) {
		float temperature = temperatureNoise.noise(x * biomeScale, z * biomeScale);
		float aridness = aridnessNoise.noise(x * biomeScale, z * biomeScale);
		float biomeRng = biomeNoise.noise(x * (biomeScale*5), z * (biomeScale*5));//1 + (float) NoiseUtil.noise1d((int)((temperature + aridness)*9999), 69)/2f;
		
		return new float[] {
				(temperature+1)/2f, 
				(aridness+1)/2f,
				(biomeRng+1)/2f
				};
	}
	
	public static Temperature getTemperature(float temperatureWeight) {
		float tempNum = 1f/Temperature.values().length;
		return Temperature.values()[(int) (temperatureWeight/tempNum)];
	}
	
	public static Moisture getMoisture(float moistureWeight) {
		float wetNum = 1f/Moisture.values().length;
		return Moisture.values()[(int) (moistureWeight/wetNum)];
	}
}
