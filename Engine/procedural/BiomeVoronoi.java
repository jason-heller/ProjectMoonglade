package procedural;

import dev.Console;
import map.Biome;
import map.Chunk;

public class BiomeVoronoi {
	private int arrSize;
	private float pointMinDist;
	
	private int x, y;
	
	private float[][][] points;
	
	private int seed;
	
	private float[] closest;
	private float[] secondClosest;
	
	// TODO: make a setter
	private float transitionSize = 3;
	private float transition = 0;
	
	public BiomeVoronoi(int arrSize, int pointMinDist, float px, float py, int seed) {
		this.arrSize = arrSize;
		this.pointMinDist = pointMinDist;
		
		x = arrSize / 2;
		y = arrSize / 2;
		
		this.points = new float[arrSize][arrSize][5];
		
		this.seed = seed;
		
		buildArray();
		//calcNearest(px,py);
	}
	
	public void buildArray() {
		//int s = arrSize/2;
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				setPoint(i,j,i,j);
			}
		}
	}
	
	public void update(float px, float py) {
		int rx = (int) Math.floor(px / pointMinDist);
		int ry = (int) Math.floor(py / pointMinDist);
		
		if (rx != x) {
			shiftX((int)Math.signum(rx - x));
			x = rx;
		}
		
		if (ry != y) {
			shiftY((int)Math.signum(ry - y));
			y = ry;
		}
		
		calcNearest(px, py);
	}
	
	private void calcNearest(float px, float py) {
		float closestDist = Float.MAX_VALUE;
		float secondClosestDist = Float.MAX_VALUE;
		
		// TODO: optimize this
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px/pointMinDist - points[i][j][0];
				float dy = py/pointMinDist - points[i][j][1];
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < closestDist) {
					closestDist = distSqr;
					closest = points[i][j];
				}
			}
		}
		
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px/pointMinDist - points[i][j][0];
				float dy = py/pointMinDist - points[i][j][1];
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < secondClosestDist && points[i][j] != closest) {
					secondClosestDist = distSqr;
					secondClosest = points[i][j];
				}
			}
		}
		
		/*for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px/pointMinDist - points[i][j][0];
				float dy = py/pointMinDist - points[i][j][1];
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < closestDist) {
					if (closestDist != Float.MAX_VALUE) {
						secondClosestDist = closestDist;
						secondClosest = closest;
					}
					
					closestDist = distSqr;
					closest = points[i][j];
				}
				else if (distSqr < secondClosestDist && distSqr > closestDist) {
					secondClosestDist = distSqr;
					secondClosest = points[i][j];
				}
			}
			
		}*/
		
		closestDist = (float) Math.sqrt(closestDist);
		secondClosestDist = (float) Math.sqrt(secondClosestDist);
		
		transition = (float) Math.min((secondClosestDist - closestDist) * transitionSize, 1f);
	}
	
	public BiomeData getDataAt(float px, float py) {
		float closestDist = Float.MAX_VALUE;
		
		Biome[] influencingBiomes = new Biome[(arrSize*arrSize)];
		float[] distances = new float[influencingBiomes.length];
		float[] influence = new float[influencingBiomes.length];
		int infIndex = 0;
		int mainBiome = 0;
		
		// TODO: optimize this
		for(int i = 0; i < arrSize; i++) {
			for(int j = 0; j < arrSize; j++) {
				float dx = px/pointMinDist - points[i][j][0];
				float dy = py/pointMinDist - points[i][j][1];
				
				float distSqr = (dx*dx)+(dy*dy);
				
				if (distSqr < closestDist) {
					mainBiome = infIndex;
					closestDist = distSqr;
				}
				
				influencingBiomes[infIndex] = Biome.values()[(int) points[i][j][2]];
				distances[infIndex] = distSqr;
				infIndex++;
			}
		}
		influence[mainBiome] = 1f;
		
		int[] b = new int[4];
		for(int i = 0; i < 4; i++) b[i] = -1;
		int bi = 0;
		
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
				b[bi++] = i;
			}

			
		}
		for(int i = 0; i < bi; i++) {
			if (b[i] == -1) break;
			
			switch(numIntersectingBiomes) {
			case 2:
				influence[b[i]] /= 2;
				break;
			
			case 3:
				float s, t;
				s = (distances[b[i]] - distances[mainBiome])*8;
				s = Math.max(0f, Math.min(s, 1f));
				
				int k = (i == 1) ? 0 : 1;
				t = (distances[b[i]] - distances[b[k]])*8;
				t = Math.max(0f, Math.min(t, 1f));
				
				influence[b[i]] = 1f - Math.max(s, t);
				influence[b[i]] /= 2;
				break;
			
			case 4:
				float u;
				s = (distances[b[i]] - distances[mainBiome])*8;
				s = Math.max(0f, Math.min(s, 1f));
				
				k = (i == 1) ? 0 : 1;
				t = (distances[b[i]] - distances[b[k]])*8;
				t = Math.max(0f, Math.min(t, 1f));
				
				k = (i == 1) ? 2 : (i == 0) ? 2 : 0;
				u = (distances[b[i]] - distances[b[k]])*8;
				u = Math.max(0f, Math.min(u, 1f));
				
				influence[b[i]] = 1f - Math.max(s, Math.max(t, u));
				influence[b[i]] /= 2;
				influence[b[i]] = 0;
				break;
			}
			
			influence[mainBiome] -= influence[b[i]];
		}
		
		//
		return new BiomeData(influencingBiomes, influence, mainBiome);
	}
	
	public void shiftX(int dx) {
		final byte shiftDir = (byte) Math.signum(dx);
		final int shiftStartPos = shiftDir == 1 ? 1 : arrSize - 2;
		final int shiftEndPos = shiftDir == 1 ? arrSize : -1;

		for (int i = shiftStartPos; i != shiftEndPos; i += shiftDir) {
			for (int j = 0; j < arrSize; j++) {
				points[i - shiftDir][j] = points[i][j];
			}
		}

		for (int j = 0; j < arrSize; j++) {
			final int i = shiftDir == 1 ? arrSize - 1 : 0;
			final int nx = (int)points[i][j][3] + shiftDir;
			final int nz = (int)points[i][j][4];
			setPoint(nx, nz, i, j);
		}
	}

	public void shiftY(int k) {
		final byte shiftDir = (byte) Math.signum(k);
		final int shiftStartPos = shiftDir == 1 ? 1 : arrSize - 2;
		final int shiftEndPos = shiftDir == 1 ? arrSize : -1;

		for (int i = 0; i < arrSize; i++) {
			for (int j = shiftStartPos; j != shiftEndPos; j += shiftDir) {
				points[i][j - shiftDir] = points[i][j];

			}
		}

		for (int i = 0; i < arrSize; i++) {
			final int j = shiftDir == 1 ? arrSize - 1 : 0;
			final int nx = (int)points[i][j][3];
			final int nz = (int)points[i][j][4] + shiftDir;
			setPoint(nx, nz, i, j);
		}
	}

	private void setPoint(int nx, int ny, int i, int j) {
		points[i][j] = new float[5];
		points[i][j][0] = (nx+NoiseUtil.noise2d(nx, ny, seed));
		points[i][j][1] = (ny+NoiseUtil.noise2d(nx, ny, -seed));
		points[i][j][2] = (int) (NoiseUtil.noise2d(nx, ny, 420)*Biome.values().length);
		points[i][j][3] = nx;
		points[i][j][4] = ny;
		Console.log(nx,ny);
	}
	
	public float getTransition() {
		return transition;
	}
	
	public float[] getClosest() {
		return closest;
	}
	
	public float[] getSecondClosest() {
		return secondClosest;
	}

	public float[][][] getData() {
		return points;
	}
}
