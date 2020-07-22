package map;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;

import core.Application;
import dev.Console;
import geom.Plane;
import gl.Camera;
import gl.res.Vbo;
import io.terrain.ChunkStreamer;
import map.prop.Props;
import map.tile.Tile;
import map.tile.TilePicker;
import procedural.terrain.GenTerrain;
import procedural.terrain.TerrainMeshBuilder;
import scene.overworld.Overworld;
import util.MathUtil;

public class Terrain {

	public static int size = 13;
	private Chunk[][] data;
	private Enviroment enviroment;
	private ChunkStreamer streamer;
	
	private TilePicker tilePicker;
	
	public Terrain(Enviroment enviroment) {
		TerrainMeshBuilder.init();

		Props.init();
		
		streamer = new ChunkStreamer(Overworld.worldName, enviroment);
		if (size < 0) {
			throw new IllegalArgumentException("Illegal Capacity: " + size);
		}

		this.enviroment = enviroment;
		this.data = new Chunk[size][size];

		tilePicker = new TilePicker(this);
		// colorData = TextureUtils.getRawTextureData("res/terrain/ground_color.png");
	}

	public void cleanUp() {
		
		for (final Chunk[] chunkBatch : get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null)
					cleanUpChunk(chunk);
			}
		}
		//streamer.save(Application.scene.getCamera().getPosition());
		streamer.update();
		streamer.close();
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
				addChunk((x+i), (z+j), i, j, null);
			}
		}
		
		//streamer.load();
	}
	
	public void update(Camera camera) {
		List<Chunk> structPass = new LinkedList<Chunk>();
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final Chunk chunk = data[i][j];
				switch(chunk.getState()) {
				case Chunk.GENERATING:
					chunk.generate(this, enviroment.getBiomeVoronoi());
					structPass.add(chunk);
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
		
		for(Chunk chunk : structPass) {
			GenTerrain.buildStructures(chunk);
			chunk.finishGenerationPass(enviroment.getBiomeVoronoi());
		}
		
		streamer.update();
		
	}

	public TerrainIntersection terrainRaycast(Vector3f origin, Vector3f dir, float maxDist) {
		Vector3f end = Vector3f.add(origin, Vector3f.mul(dir, maxDist));
		List<int[]> terrainTiles = MathUtil.bresenham(origin.x+dir.x, origin.z+dir.z, end.x, end.z);
		TerrainIntersection ti = null;
		
		for(int[] point : terrainTiles) {
			final Chunk chunk = getChunkAt(point[0], point[1]);
			if (chunk == null) return null;
			
			Plane plane = chunk.getPlane(point[0], point[1], false);
			
			final float tileY = chunk.getTerrain().getHeightAt(point[0] + .5f, point[1] + .5f);//plane.projectPoint(new Vector3f(point[0] + .5f, 0, point[1] + .5f)).y;
			
			ti = chunk.getProps().testCollision(origin, dir, point[0], tileY, point[1]);
			
			if (ti != null) {
				return ti;
			}
			
			float hitDist = plane.rayIntersection(origin, dir);
			if (hitDist < 0 || hitDist == Float.MAX_VALUE)
				continue;

			Vector3f hit = Vector3f.add(origin, Vector3f.mul(dir, hitDist));
			
			if (hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
				hit.y += .25f;
				return new TerrainIntersection(hit, null, 0, 0, chunk);
			} else {
				plane = getChunkAt(point[0], point[1]).getPlane(point[0], point[1], true);
				hitDist = plane.rayIntersection(origin, dir);
				if (hitDist < 0 || hitDist == Float.MAX_VALUE)
					continue;

				hit = Vector3f.add(origin, Vector3f.mul(dir, hitDist));
				if (hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
					hit.y += .25f;
					return new TerrainIntersection(hit, null, 0, 0, chunk);
				}
			}
		}
	
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

		final int i = shiftDir == 1 ? size - 1 : 0;
		for (int j = 0; j < size; j++) {
			final int nx = data[i][j].dataX + shiftDir;
			final int nz = data[i][j].dataZ;
			addChunk(nx, nz, i, j, null);
		}
		
		//streamer.save(Application.scene.getCamera().getPosition());
		//streamer.load();
	}

	public void shiftY(int dz) {
		final byte shiftDir = (byte) Math.signum(dz);
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

		final int j = shiftDir == 1 ? size - 1 : 0;
		for (int i = 0; i < size; i++) {
			final int nx = data[i][j].dataX;
			final int nz = data[i][j].dataZ + shiftDir;
			addChunk(nx, nz, i, j, null);
		}
		
		//streamer.save(Application.scene.getCamera().getPosition());
		//streamer.load();
	}

	
	// Called by populate, shiftX, and shiftY
	private void addChunk(int x, int z, int arrX, int arrY, Vbo[][] vbos) {
		
		Chunk chunk = new Chunk(x, z, this);
		chunk.arrX = arrX;
		chunk.arrZ = arrY;
		
		streamer.queueForLoading(chunk);
		
		data[arrX][arrY] = chunk;
	}
	
	/*public Chunk getChunkAt(float x, float z) {
		for(Chunk[] stripe : data) {
			for(Chunk chunk : stripe) {
				if (chunk.realX <= x && chunk.realZ <= z && chunk.realX + Chunk.CHUNK_SIZE > x && chunk.realZ + Chunk.CHUNK_SIZE > z) {
					return chunk;
				}
			}
		}
		
		return null;
	}*/
	
	public Chunk getChunkAt(float x, float z) {
		float relx = (float) Math.floor(x / Chunk.CHUNK_SIZE);
		float relz = (float) Math.floor(z / Chunk.CHUNK_SIZE);
		final int tx = (int) relx - enviroment.x;//Math.floor(relx / Chunk.CHUNK_SIZE);
		final int tz = (int) relz - enviroment.z;//Math.floor(relz / Chunk.CHUNK_SIZE);
		
		if (tx >= 0 && tz >= 0 && tx < data.length && tz < data.length) {
			Chunk chunk = data[tx][tz];
			
			if (x >= chunk.realX && z >= chunk.realZ && x < chunk.realX + Chunk.CHUNK_SIZE && z < chunk.realZ + Chunk.CHUNK_SIZE) {
				return chunk;
			}
		}
		
		return null;
	}
	
	public Tile getTileAt(float x, float y, float z) {
		Chunk chunkPtr = getChunkAt(x, z);
		if (chunkPtr == null) return null;
		
		return chunkPtr.getBuilding().getTileAt(
				x - chunkPtr.realX,
				y,
				z - chunkPtr.realZ);
	}
	
	public Material getMaterialAt(float x, float y, float z, byte facing) {
		Tile tile = getTileAt(x,y,z);
		return tile == null ? Material.NONE : tile.getMaterial(facing);
	}

	public float getHeightAt(float x, float z) {
		Chunk c = getChunkAt(x, z);
		if (c == null)
			return Float.MIN_VALUE;
		return c.getPolygon(x, z).barryCentric(x, z);
	}

	public void reload() {
		
		for(Chunk[] stripe : data) {
			for(Chunk chunk : stripe) {
				chunk.cleanUp();
			}
		}
		this.data = new Chunk[size][size];
		
		populate(enviroment.x, enviroment.z);
	}

	public Vector3f buildingRaycast(Overworld overworld, Vector3f position, Vector3f directionVector, int playerReach, byte facing) {
		return tilePicker.buildingRaycast(overworld, position, directionVector, playerReach, facing);
	}

	public ChunkStreamer getStreamer() {
		return this.streamer;
	}

}
