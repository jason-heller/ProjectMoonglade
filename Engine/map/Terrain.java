package map;

import static map.Chunk.VERTEX_COUNT;

import java.util.List;

import org.joml.Vector3f;

import core.Resources;
import core.res.TileableModel;
import core.res.Vbo;
import dev.Console;
import geom.Plane;
import gl.Camera;
import io.ChunkStreamer;
import map.building.Building;
import map.building.Material;
import map.building.Tile;
import map.tile.EnvTile;
import map.tile.TileData;
import util.MathUtil;

public class Terrain {

	public static int size;
	private Chunk[][] data;
	private Enviroment enviroment;
	private TileData itemResources;
	private ChunkStreamer streamer;
	
	public Terrain(Enviroment enviroment, int chunkArrSize) {
		size = chunkArrSize;

		itemResources = new TileData();
		
		streamer = new ChunkStreamer(this);
		if (size < 0) {
			throw new IllegalArgumentException("Illegal Capacity: " + size);
		}

		this.enviroment = enviroment;
		this.data = new Chunk[size][size];
		
		Resources.addSound("step_grass", "step_grass.ogg");
		
		// colorData = TextureUtils.getRawTextureData("res/terrain/ground_color.png");
	}

	public void cleanUp() {

		itemResources = null;
		
		for (final Chunk[] chunkBatch : get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null)
					cleanUpChunk(chunk);
			}
		}
		//streamer.save(Application.scene.getCamera().getPosition());
		streamer.finish();
		
		Resources.removeSound("step_grass");
	}

	private void cleanUpChunk(Chunk chunk) {
		streamer.queueForSaving(chunk);
	}

	public Chunk[][] get() {
		return data;
	}

	public Chunk get(int x, int y) {
		return data[x][y];
	}

	public void populate(int x, int z) {
		for (final Chunk[] chunkBatch : get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null) {
					cleanUpChunk(chunk);
				}
			}
		}
		
		//streamer.save(Application.scene.getCamera().getPosition());

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				addChunk(i-(size/2), j-(size/2), i, j, null);
			}
		}
		
		//streamer.load();
	}
	
	public void update(Camera camera) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final Chunk chunk = data[i][j];
				switch(chunk.getState()) {
				case Chunk.GENERATING:
					chunk.generate(this, enviroment.getBiomeVoronoi(), i, j);
					break;
				case Chunk.BUILDING:
					chunk.build(enviroment.getBiomeVoronoi());
					break;
				case Chunk.UNLOADING:
					chunk.cleanUp();
					break;
				case Chunk.LOADED:
					chunk.checkIfCulled(camera.getFrustum());
					break;
				}
			}
		}
		
		streamer.update();
		
	}

	final float raycastDelta = 0.1f;
	public Vector3f buildingRaycast(Vector3f origin, Vector3f dir, float maxDist) {
		Tile output;
		
		Vector3f point = new Vector3f(origin);
		Vector3f offset = Vector3f.mul(dir, raycastDelta);
		
		float tx = Float.MIN_VALUE;
		float ty = Float.MIN_VALUE;
		float tz = Float.MIN_VALUE;
		
		for(float i = 0; i <= maxDist; i += raycastDelta) {
			float x = (float) ((Math.floor(point.x/Tile.TILE_SIZE))*Tile.TILE_SIZE);
			float y = (float) ((Math.floor(point.y/Tile.TILE_SIZE))*Tile.TILE_SIZE);
			float z = (float) ((Math.floor(point.z/Tile.TILE_SIZE))*Tile.TILE_SIZE);
			if (x != tx || y != ty || z != tz) {
				tx = x;
				ty = y;
				tz = z;
				output = getTileAt(tx, ty, tz);
				//EntityControl.addEntity(new TestEntity(tx,ty,tz));
				
				
	        	if (output != null) {
	        		byte side = Tile.checkRay(point, dir, x, y, z, output.getWalls());
	        		if (side != 0) {
	        			return new Vector3f(tx, ty, tz);
	        		}
	        	}
			}
			
			point.add(offset);
		}
		
		return null;
	}
	
	public Vector3f terrainRaycast(Vector3f origin, Vector3f dir, float maxDist) {
		Vector3f end = Vector3f.add(origin, Vector3f.mul(dir, maxDist));
		List<int[]> terrainTiles = MathUtil.bresenham(origin.x, origin.z, end.x, end.z);
		for(int[] point : terrainTiles) {
			Chunk c = getChunkAt(point[0], point[1]);
			if (c == null) return null;
			Plane plane = c.getPlane(point[0], point[1], false);
			Vector3f hit = plane.rayIntersection(origin, dir);

			if (hit != null && hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
				
				return hit;
			} else {
				plane = getChunkAt(point[0], point[1]).getPlane(point[0], point[1], true);
				hit = plane.rayIntersection(origin, dir);
				if (hit != null && hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
					
					return hit;
				}
			}
		}
		/*float height;
		
		Vector3f point = new Vector3f(origin);
		Vector3f offset = Vector3f.mul(dir, raycastDelta);
		
		float tx = Float.MIN_VALUE;
		float ty = Float.MIN_VALUE;
		float tz = Float.MIN_VALUE;
		
		for(float i = 0; i <= maxDist; i += raycastDelta) {
			float x = (float) ((Math.floor(point.x/Tile.TILE_SIZE))*Tile.TILE_SIZE);
			float y = (float) ((Math.floor(point.y/Tile.TILE_SIZE))*Tile.TILE_SIZE);
			float z = (float) ((Math.floor(point.z/Tile.TILE_SIZE))*Tile.TILE_SIZE);

			if (x != tx || y != ty || z != tz) {
				tx = x;
				ty = y;
				tz = z;
				
				height = getHeightAt(tx,tz);
				if (height >= point.y) {
	        		return new Vector3f(point.x, height, point.z);
	        	}
			}
			
			point.add(offset);
		}*/
		
		return null;
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
						cleanUpChunk(chunk);
				}
				data[i - shiftDir][j] = data[i][j];
				data[i - shiftDir][j].arrX = i - shiftDir;
			}
		}

		for (int j = 0; j < size; j++) {
			final int i = shiftDir == 1 ? size - 1 : 0;
			final int nx = data[i][j].x + shiftDir;
			final int nz = data[i][j].z;
			addChunk(nx, nz, i, j, null);
		}
		
		//streamer.save(Application.scene.getCamera().getPosition());
		//streamer.load();
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
						cleanUpChunk(chunk);
				}

				data[i][j - shiftDir] = data[i][j];
				data[i][j - shiftDir].arrZ = j - shiftDir;

			}
		}

		for (int i = 0; i < size; i++) {
			final int j = shiftDir == 1 ? size - 1 : 0;
			final int nx = data[i][j].x;
			final int nz = data[i][j].z + shiftDir;
			addChunk(nx, nz, i, j, null);
		}
		
		//streamer.save(Application.scene.getCamera().getPosition());
		//streamer.load();
	}

	
	// Called by populate, shiftX, and shiftY
	private void addChunk(int x, int z, int arrX, int arrY, Vbo[][] vbos) {
		
		float[][] heightmap = new float[Chunk.VERTEX_COUNT*2][Chunk.VERTEX_COUNT*3];
		int[][] tileItems = new int[Chunk.VERTEX_COUNT-1][Chunk.VERTEX_COUNT-1];
		
		Chunk chunk = new Chunk(x, z, this);
		chunk.heightmap = heightmap;
		chunk.items.tilemap = tileItems;
		chunk.arrX = arrX;
		chunk.arrZ = arrY;
		
		streamer.queueForLoading(chunk);
		
		data[arrX][arrY] = chunk;
	}
	
	public void loadChunk() {
		
	}
	
	public void setChunkState(int x, int z, byte state) {
		
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
	
	public Tile getTileAt(float x, float y, float z) {
		Chunk chunkPtr = getChunkAt(x, z);
		return chunkPtr.getBuilding().getTileAt(
				x - (chunkPtr.x*Chunk.CHUNK_SIZE),
				y,
				z - (chunkPtr.z*Chunk.CHUNK_SIZE));
	}

	public float getHeightAt(float x, float z) {
		Chunk c = getChunkAt(x, z);
		if (c == null)
			return Float.MIN_VALUE;
		return c.getPolygon(x, z).barryCentric(x, z);
	}
	
	public TileableModel getTileItem(int id) {
		return itemResources.getModel(id);
	}

	public int getArraySize() {
		return Terrain.size;
	}

	public EnvTile getTileById(int id) {
		return this.itemResources.get(id);
	}
}
