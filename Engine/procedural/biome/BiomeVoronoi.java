package procedural.biome;

import java.util.ArrayList;
import java.util.List;

import map.Enviroment;
import procedural.NoiseUtil;
import procedural.terrain.GenTerrain;

public class BiomeVoronoi {
	private int arrSize;
	
	private int x, y;
	
	private Enviroment enviroment;
	private BiomeNode[][] points;
	
	private int seed;
	
	private BiomeNode closest;
	private float scale;
	
	public BiomeVoronoi(Enviroment enviroment, int terrainArrSize, float scale, float px, float py, int seed) {
		this.enviroment = enviroment;
		this.scale = scale;
		this.arrSize = (int) (Math.ceil(terrainArrSize/scale) + 4);
		
		this.points = new BiomeNode[arrSize][arrSize];
		
		this.seed = seed;
		
		buildArray(0, 0);
		//update(px, py);
	}
	
	public void buildArray(int camX, int camZ) {
		x = camX;
		y = camZ;
		
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				setPoint(x+i-2,y+j-2,i,j);
			}
		}
	}
	
	public void tick(float px, float py) {
		int camX = (int) Math.floor(px/scale);
		int camZ = (int) Math.floor(py/scale);
		if (x != camX) {
			final int dx = camX - x;
			if (Math.abs(dx) > 1) {
				buildArray(camX, camZ);
			} else {
				shiftX(dx);
			}
			x = camX;
		}

		if (y != camZ) {
			final int dz = camZ - y;
			if (Math.abs(dz) > 1) {
				buildArray(camX, camZ);
			} else {
				shiftY(dz);
			}
			y = camZ;
		}
		
		calcNearest(px, py);
	}
	
	private void calcNearest(float px, float py) {
		float closestDist = Float.MAX_VALUE;
		
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px/scale - points[i][j].x;
				float dy = py/scale - points[i][j].z;
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < closestDist) {
					closestDist = distSqr;
					closest = points[i][j];
				}
			}
		}
		
		closestDist = (float) Math.sqrt(closestDist);
	}
	
	public BiomeData getDataAt(float px, float py) {
		float closestDist = Float.MAX_VALUE;
		
		Biome[] influencingBiomes = new Biome[(arrSize*arrSize)];
		BiomeNode[] fullBiomeData = new BiomeNode[influencingBiomes.length];
		float[] distances = new float[influencingBiomes.length];
		float[] influence = new float[influencingBiomes.length];
		int infIndex = 0;
		int mainBiome = 0;
		
		px /= scale;
		py /= scale;
		
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px - points[i][j].x;
				float dy = py - points[i][j].z;
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < closestDist) {
					mainBiome = infIndex;
					closestDist = distSqr;
				}
				
				influencingBiomes[infIndex] = points[i][j].biome;
				fullBiomeData[infIndex] = points[i][j];
				distances[infIndex] = distSqr;
				infIndex++;
			}
		}
		influence[mainBiome] = 1f;
		
		List<Integer> b = new ArrayList<Integer>();
		
		int numIntersectingBiomes = 1;
		for(int i = 0; i < influencingBiomes.length; i++) {
			if (i == mainBiome)
				continue;
			if (distances[i] - distances[mainBiome] > 64) {
				influencingBiomes[i] = null;
				continue;
			}
			
			
			influence[i] = (distances[i] - distances[mainBiome])*8;
			influence[i] = Math.max(0f, Math.min(influence[i], 1f));
			influence[i] = 1f - influence[i];
			
			if (influence[i] != 0 && influence[i] != 1) {
				numIntersectingBiomes++;
				b.add(i);
			}
		}
		
		if (numIntersectingBiomes == 2) {
			influence[b.get(0)] /= 2;
			influence[mainBiome] -= influence[b.get(0)];
		} else {
			float add = 0;
			for(int i = 0; i < b.size(); i++) {
				influence[b.get(i)] /= b.size()+.75f;
				add += influence[b.get(i)];
			}
			influence[mainBiome] -= add;//*= (p3[2]);
		}
		
		return new BiomeData(influencingBiomes, influence, mainBiome, fullBiomeData[mainBiome].subseed);
	}
	
	public void shiftX(int dx) {
		final byte shiftDir = (byte) Math.signum(dx);
		final int shiftStartPos = shiftDir == 1 ? 1 : arrSize - 2;
		final int shiftEndPos = shiftDir == 1 ? arrSize : - 1;

		for (int i = shiftStartPos; i != shiftEndPos; i += shiftDir) {
			for (int j = 0; j < arrSize; j++) {
				points[i - shiftDir][j].copy(points[i][j]);
			}
		}

		for (int j = 0; j < arrSize; j++) {
			final int i = shiftDir == 1 ? arrSize - 1 : 0;
			final int nx = points[i][j].arrX + shiftDir;
			final int nz = points[i][j].arrZ;
			setPoint(nx, nz, i, j);
		}
	}

	public void shiftY(int k) {
		final byte shiftDir = (byte) Math.signum(k);
		final int shiftStartPos = shiftDir == 1 ? 1 : arrSize - 2;
		final int shiftEndPos = shiftDir == 1 ? arrSize : -1;

		for (int i = 0; i < arrSize; i++) {
			for (int j = shiftStartPos; j != shiftEndPos; j += shiftDir) {
				points[i][j - shiftDir].copy(points[i][j]);
			}
		}

		for (int i = 0; i < arrSize; i++) {
			final int j = shiftDir == 1 ? arrSize - 1 : 0;
			final int nx = points[i][j].arrX;
			final int nz = points[i][j].arrZ + shiftDir;
			setPoint(nx, nz, i, j);
		}
	}

	private void setPoint(int nx, int ny, int i, int j) {
		BiomeNode node = new BiomeNode();
		node.arrX = nx;
		node.arrZ = ny;
		node.x = (nx + NoiseUtil.valueNoise2d(nx, ny, seed))*1;
		node.z = (ny + NoiseUtil.valueNoise2d(nx, ny, -seed))*1;

		node.subseed = (int) (NoiseUtil.valueNoise2d(nx, ny, NoiseUtil.szudzik(seed, -seed))*65535);// Wtf am i doin
				
		float[] climateProperties = GenTerrain.getClimateProperties(node.x, node.z);
		
		node.biome = enviroment.calcBiome(nx, ny, seed,
				GenTerrain.getTemperature(climateProperties[0]),
				GenTerrain.getMoisture(climateProperties[0]),
				climateProperties[2]
				);
		points[i][j] = node;
	}
	
	public BiomeNode getClosest() {
		return closest;
	}

	public BiomeNode[][] getData() {
		return points;
	}
	
	public class BiomeNode {
		public float x, z;
		public Biome biome;
		public int arrX, arrZ;
		public int subseed;
		public void copy(BiomeNode node) {
			this.x = node.x;
			this.z = node.z;
			this.biome = node.biome;
			this.arrX = node.arrX;
			this.arrZ = node.arrZ;
			this.subseed = node.subseed;
		}
	}
}
