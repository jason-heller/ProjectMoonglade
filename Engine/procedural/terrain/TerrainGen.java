package procedural.terrain;

import map.Biome;
import map.Moisture;
import map.Temperature;
import procedural.BiomeData;
import procedural.BiomeVoronoi;
import procedural.Noise;
import procedural.SimplexNoise;

public class TerrainGen {
	private static SimplexNoise biomeNoise = new SimplexNoise(69);
	private static Noise temperatureNoise = new SimplexNoise(23234);
	private static Noise aridnessNoise = new SimplexNoise(233384);
	
	private static Noise heightNoise = new SimplexNoise(0);
	
	public static int heightmapIndexCount;
	
	public static float biomeScale = .001f;
	
	public static float[][] buildHeightmap(int x, int y, int z, int vertexStripeSize, int polygonSize, BiomeVoronoi biomeVoronoi, float[][] heights) {
		float terrainHeight;
		
		for(int j = 0; j < vertexStripeSize; j++) {
			for(int i = 0; i < vertexStripeSize; i++) {
				BiomeData biomeData = biomeVoronoi.getDataAt((x+i)*polygonSize, (z+j)*polygonSize);

				terrainHeight = getTerrainNoise(x+i, z+j, biomeData);
				heights[(i*2)+1][(j*2)+1] = y + terrainHeight;
				
				boolean l = (i*2) >= 0;
				boolean t = (j*2) >= 0;
				
				if (l)
					heights[(i*2)][(j*2)+1] = y + terrainHeight;
				
				if (t)
					heights[(i*2)+1][(j*2)] = y + terrainHeight;
				
				if (l && t)
					heights[(i*2)][(j*2)] = y + terrainHeight;
			}
		}
		
		// Fill in edge data
		heights[0][0] = heights[1][1];
		for(int i = 1; i < heights.length; i++) {
			heights[i][0] = heights[i][1];
			heights[0][i] = heights[1][i];
		}
		
		/*for(int i = 0; i < heights.length; i++) {
			for(int j = 0; j < heights.length; j++) {
				System.err.print((int)heights[i][j] + ", ");
			}System.err.println();
		}*/
		
		return heights;
	}

	private static float getTerrainNoise(int x, int z, BiomeData biomeData) {
		final int octaves = 4;//2 + (int)(noiseX.octavedNoise(x, z, 1, 1, .005f)*2);
		final float roughness = biomeData.getRoughness();
		final float scale = .005f;// + (biome.terrainScale * biomeInfluence);

		// mountain fbm, heightvariance fbm, cliff noise?
		float height = (float) heightNoise.fbm(x, z, octaves, roughness, scale);
		height *= biomeData.getTerrainFactor();
		
		return height;
	}

	public static float[] getBiomeFactor(int x, int z) {
		float transition = biomeNoise.noise(x * biomeScale, z * biomeScale);
		float temperature = temperatureNoise.noise(x * biomeScale, z * biomeScale);
		float aridness = aridnessNoise.noise(x * biomeScale, z * biomeScale);
		transition = (float)Math.sin(Math.abs(transition)*Math.PI);//(transition * transition * (-3.0f - 2.0f*transition));
		
		float biomeRng;
		//int i0 = (int) Math.abs(biomeNoise.influence[0]);
		//int i1 = (int) Math.abs(biomeNoise.influence[1]);
		//int i2 = (int) Math.abs(biomeNoise.influence[2]);
		double i0 = (biomeNoise.influence[0]);
		double i1 = (biomeNoise.influence[1]);
		double i2 = (biomeNoise.influence[2]);
		if (i0 > i1 && i0 > i2) {
			biomeRng = biomeNoise.rng[0];
		} else if (i1 > i0
				&& i1 > i2) {
			biomeRng = biomeNoise.rng[1];
		} else {
			biomeRng = biomeNoise.rng[2];
		}
		
		//biomeRng = MathUtil.clamp(biomeRng, 0f, 255f);
		
		return new float[] {
				(temperature+1)/2f, 
				(aridness+1)/2f,
				1f-transition,
				biomeRng/11f
				};
	}
	
	public static Temperature getTemperature(float[] biomeData) {
		float tempNum = 1f/Temperature.values().length;
		return Temperature.values()[(int) (biomeData[0]/tempNum)];
	}
	
	public static Moisture getMoisture(float[] biomeData) {
		float wetNum = 1f/Moisture.values().length;
		return Moisture.values()[(int) (biomeData[1]/wetNum)];
	}
}
