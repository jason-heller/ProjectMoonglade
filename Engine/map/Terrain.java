package map;

import org.joml.Vector3f;

import core.res.Vbo;
import dev.Console;
import map.building.Tile;
import procedural.terrain.TerrainGen;

public class Terrain {

	static int size;
	private map.Chunk[][] data;
	private Enviroment enviroment;
	
	public Terrain(Enviroment enviroment, int chunkArrSize) {
		size = chunkArrSize;
		
		if (size < 0) {
			throw new IllegalArgumentException("Illegal Capacity: " + size);
		}

		this.enviroment = enviroment;
		this.data = new Chunk[size][size];
		
		// colorData = TextureUtils.getRawTextureData("res/terrain/ground_color.png");
	}

	public void cleanUp() {
		
		for (final Chunk[] chunkBatch : get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null)
					chunk.cleanUp();
			}
		}
	}

	public Chunk[][] get() {
		return data;
	}

	public Chunk get(int x, int y) {
		return data[x][y];
	}

	public Chunk getChunkAt(float x, float z) {
		final float relx = x - data[0][0].x * Chunk.CHUNK_SIZE;
		final float relz = z - data[0][0].z * Chunk.CHUNK_SIZE;
		final int tx = (int) Math.floor(relx / Chunk.CHUNK_SIZE);
		final int tz = (int) Math.floor(relz / Chunk.CHUNK_SIZE);

		if (tx < 0 || tz < 0 || tx >= data.length || tz >= data[0].length) {
			return null;
		}

		return data[tx][tz];
	}

	public void populate(int x, int z) {
		for (final Chunk[] chunkBatch : get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null) {
					chunk.cleanUp();
				}
			}
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				loadChunk(x+i, z+j, i, j, null);
			}
		}
	}
	
	public void update() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final Chunk chunk = data[i][j];
				if (chunk.getLoadedState() == Chunk.UNLOADED) {
					fillAdjacents(chunk, i, j);
					chunk.load(this, enviroment.getBiomeVoronoi());
				}
			}
		}
	}

	public Vector3f buildingRaycast(Vector3f origin, Vector3f dir, float maxDist) {
		int i, dx, dy, dz, l, m, n, x_inc, y_inc, z_inc, err_1, err_2, dx2, dy2, dz2;
		Tile output;
		
		Vector3f point = Vector3f.div(origin, Tile.TILE_SIZE);
		Vector3f endPoint = Vector3f.add(point, Vector3f.mul(dir, maxDist/Tile.TILE_SIZE));
		//point.y--;
		//endPoint.y--;
		
	    dx = (int)(endPoint.x - point.x);
	    dy = (int)(endPoint.y - point.y);
	    dz = (int)(endPoint.z - point.z);
	    x_inc = (dx < 0) ? -1 : 1;
	    l = Math.abs(dx);
	    y_inc = (dy < 0) ? -1 : 1;
	    m = Math.abs(dy);
	    z_inc = (dz < 0) ? -1 : 1;
	    n = Math.abs(dz);
	    dx2 = l << 1;
	    dy2 = m << 1;
	    dz2 = n << 1;
	    
	    if ((l >= m) && (l >= n)) {
	        err_1 = dy2 - l;
	        err_2 = dz2 - l;
	        for (i = 0; i < l; i++) {
	        	float tx = point.x*Tile.TILE_SIZE;
	        	float ty = point.y*Tile.TILE_SIZE;
	        	float tz = point.z*Tile.TILE_SIZE;
	        	output = getTileAt(tx, ty, tz);
	        	if (output != null) {
	        		byte side = Tile.checkRay(origin, dir, tx, ty, tz, output.getWalls());
	        		if (side != 0) {
	        			return Vector3f.mul(point, Tile.TILE_SIZE);
	        		}
	        	}
	            if (err_1 > 0) {
	            	point.y += y_inc;
	                err_1 -= dx2;
	            }
	            if (err_2 > 0) {
	            	point.z += z_inc;
	                err_2 -= dx2;
	            }
	            err_1 += dy2;
	            err_2 += dz2;
	            point.x += x_inc;
	        }
	    } else if ((m >= l) && (m >= n)) {
	        err_1 = dx2 - m;
	        err_2 = dz2 - m;
	        for (i = 0; i < m; i++) {
	        	float tx = point.x*Tile.TILE_SIZE;
	        	float ty = point.y*Tile.TILE_SIZE;
	        	float tz = point.z*Tile.TILE_SIZE;
	        	output = getTileAt(tx, ty, tz);
	        	if (output != null) {
	        		byte side = Tile.checkRay(origin, dir, tx, ty, tz, output.getWalls());
	    
	        		if (side != 0) {
	        			return Vector3f.mul(point, Tile.TILE_SIZE);
	        		}
	        	}
	            if (err_1 > 0) {
	            	point.x += x_inc;
	                err_1 -= dy2;
	            }
	            if (err_2 > 0) {
	            	point.z += z_inc;
	                err_2 -= dy2;
	            }
	            err_1 += dx2;
	            err_2 += dz2;
	            point.y += y_inc;
	        }
	    } else {
	        err_1 = dy2 - n;
	        err_2 = dx2 - n;
	        for (i = 0; i < n; i++) {
	        	float tx = point.x*Tile.TILE_SIZE;
	        	float ty = point.y*Tile.TILE_SIZE;
	        	float tz = point.z*Tile.TILE_SIZE;
	        	output = getTileAt(tx, ty, tz);
	        	if (output != null) {
	        		byte side = Tile.checkRay(origin, dir, tx, ty, tz, output.getWalls());
	        		if (side != 0) {
	        			return Vector3f.mul(point, Tile.TILE_SIZE);
	        		}
	        	}
	            if (err_1 > 0) {
	            	point.y += y_inc;
	                err_1 -= dz2;
	            }
	            if (err_2 > 0) {
	            	point.x += x_inc;
	                err_2 -= dz2;
	            }
	            err_1 += dy2;
	            err_2 += dx2;
	            point.z += z_inc;
	        }
	    }
	    float tx = point.x*Tile.TILE_SIZE;
    	float ty = point.y*Tile.TILE_SIZE;
    	float tz = point.z*Tile.TILE_SIZE;
    	output = getTileAt(tx, ty, tz);
    	if (output != null) {
    		byte side = Tile.checkRay(origin, dir, tx, ty, tz, output.getWalls());
    		if (side != 0) {
    			return Vector3f.mul(point, Tile.TILE_SIZE);
    		}
    	}
    	
	    return null;
	}

	public Tile getTileAt(float x, float y, float z) {
		Chunk chunkPtr = getChunkAt(x, z);
		return chunkPtr.getBuilding().getTileAt(
				x - (chunkPtr.x*Chunk.CHUNK_SIZE),
				y,
				z - (chunkPtr.z*Chunk.CHUNK_SIZE));
	}
	
	public void shiftX(int dx) {
		final byte shiftDir = (byte) Math.signum(dx);
		final int shiftStartPos = shiftDir == 1 ? 1 : size - 2;
		final int shiftEndPos = shiftDir == 1 ? size : -1;

		for (int i = shiftStartPos; i != shiftEndPos; i += shiftDir) {
			for (int j = 0; j < size; j++) {
				if (i == shiftStartPos) {
					Chunk chunk = data[i - shiftDir][j];
					if (chunk != null)
						chunk.cleanUp();
				}
				data[i - shiftDir][j] = data[i][j];
			}
		}

		for (int j = 0; j < size; j++) {
			final int i = shiftDir == 1 ? size - 1 : 0;
			final int nx = data[i][j].x + shiftDir;
			final int nz = data[i][j].z;
			loadChunk(nx, nz, i, j, null);
		}
	}

	public void shiftY(int k) {
		final byte shiftDir = (byte) Math.signum(k);
		final int shiftStartPos = shiftDir == 1 ? 1 : size - 2;
		final int shiftEndPos = shiftDir == 1 ? size : -1;

		for (int i = 0; i < size; i++) {
			for (int j = shiftStartPos; j != shiftEndPos; j += shiftDir) {
				if (j == shiftStartPos) {
					Chunk chunk = data[i][j - shiftDir];
					if (chunk != null)
						chunk.cleanUp();
				}

				data[i][j - shiftDir] = data[i][j];

			}
		}

		for (int i = 0; i < size; i++) {
			final int j = shiftDir == 1 ? size - 1 : 0;
			final int nx = data[i][j].x;
			final int nz = data[i][j].z + shiftDir;
			loadChunk(nx, nz, i, j, null);
		}
	}

	private void loadChunk(int x, int z, int arrX, int arrY, Vbo[][] vbos) {
		float[][] heightmap = new float[Chunk.VERTEX_COUNT*2][Chunk.VERTEX_COUNT*3];
		heightmap = TerrainGen.buildHeightmap(x*(Chunk.VERTEX_COUNT-1), 0, z*(Chunk.VERTEX_COUNT-1), Chunk.VERTEX_COUNT, Chunk.POLYGON_SIZE, enviroment.getBiomeVoronoi(), heightmap);
	
		Chunk chunk = new Chunk(x, z, this);
		chunk.heightmap = heightmap;
		data[arrX][arrY] = chunk;
	}
	
	// The adjacents[] array is structured as follows: {LEFT, RIGHT, TOP, BOTTOM}
	private void fillAdjacents(Chunk chunk, int arrX, int arrY) {
		// I hate if condition spam AAAAAAAA
		if (arrX > 0)
			chunk.getAdjacents()[0] = data[arrX - 1][arrY];
		if (arrX < size - 2)
			chunk.getAdjacents()[1] = data[arrX + 1][arrY];
		if (arrY > 0)
			chunk.getAdjacents()[2] = data[arrX][arrY - 1];
		if (arrY < size - 2)
			chunk.getAdjacents()[3] = data[arrX][arrY + 1];
	}

	public float getHeightAt(float x, float z) {
		Chunk c = this.getChunkAt(x, z);
		if (c == null)
			return 0;
		int rx = (int) (x - (c.x * Chunk.CHUNK_SIZE)) / Chunk.POLYGON_SIZE;
		int rz = (int) (z - (c.z * Chunk.CHUNK_SIZE)) / Chunk.POLYGON_SIZE;

		return c.heightmap[rx*2+1][rz*2+1];
	}
}
