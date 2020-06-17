package map;

import static map.building.BuildingTile.TILE_SIZE;

import java.util.List;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import core.res.TileableModel;
import core.res.Vbo;
import dev.Console;
import geom.Plane;
import gl.Camera;
import io.ChunkStreamer;
import map.building.BuildingTile;
import map.tile.EnvTile;
import map.tile.TileData;
import scene.overworld.Overworld;
import util.MathUtil;

public class Terrain {

	public static int size = 7;
	private Chunk[][] data;
	private Enviroment enviroment;
	private TileData itemResources;
	private ChunkStreamer streamer;
	
	public Terrain(Enviroment enviroment, int chunkArrSize) {
		size = chunkArrSize;

		itemResources = new TileData();
		
		streamer = new ChunkStreamer(Overworld.worldName, this);
		if (size < 0) {
			throw new IllegalArgumentException("Illegal Capacity: " + size);
		}

		this.enviroment = enviroment;
		this.data = new Chunk[size][size];

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
		streamer.update();
		streamer.finish();
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
					Console.log("E");
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
	public Vector3f buildingRaycast(Overworld ow, Vector3f origin, Vector3f dir, float maxDist, byte facing, boolean floor) {
		BuildingTile output;
		
		Vector3f point = new Vector3f(origin);
		Vector3f offset = Vector3f.mul(dir, raycastDelta);
		
		Vector3f potentialConnect = null;
		byte potentialConnectFacingByte = 0;
		
		float tx = Float.MIN_VALUE;
		float ty = Float.MIN_VALUE;
		float tz = Float.MIN_VALUE;
		
		for(float i = 0; i <= maxDist; i += raycastDelta) {
			float x = (float) ((Math.floor(point.x/BuildingTile.TILE_SIZE))*BuildingTile.TILE_SIZE);
			float y = (float) ((Math.floor(point.y/BuildingTile.TILE_SIZE))*BuildingTile.TILE_SIZE);
			float z = (float) ((Math.floor(point.z/BuildingTile.TILE_SIZE))*BuildingTile.TILE_SIZE);
			if (x != tx || y != ty || z != tz) {
				tx = x;
				ty = y;
				tz = z;
				
				output = getTileAt(tx, ty, tz);
				
				byte side = 0;
	        	if (output != null && (side = BuildingTile.checkRay(point, dir, x, y, z, output.getWalls())) != 0) {
	        		return new Vector3f(tx, ty, tz);
	        	} else {
	        		Vector3f normal = MathUtil.rayBoxEscapeNormal(point, dir, x, y, z, TILE_SIZE);
	        		byte potentialFacingByte = BuildingTile.getFacingByte(normal);

					if (output != null && side == 0 && (BuildingTile.getOpposingBytes(output.getWalls()) & potentialFacingByte) == 0) {
	        			potentialConnect = new Vector3f(tx, ty, tz);
	        			potentialConnectFacingByte = potentialFacingByte;
	        		}
	        		
	        		// Check neighbors
	        		if (floor) {
	        			if ((output = getTileAt(tx + TILE_SIZE, ty, tz)) != null && 
	        					(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
	        				//ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
	        			}
						if ((output = getTileAt(tx - TILE_SIZE, ty, tz)) != null && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							//ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
						if ((output = getTileAt(tx, ty, tz + TILE_SIZE)) != null && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							//ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
						if ((output = getTileAt(tx, ty, tz - TILE_SIZE)) != null && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							//ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
	        		} else {
					
	        			float dx = ((facing & 3) == 0) ? TILE_SIZE : 0;
						float dz = TILE_SIZE - dx;
						if ((facing & 1) != 0) dz *= -1;
						if ((facing & 32) != 0) dx *= -1;
						
						if ((output = getTileAt(tx-dx, ty, tz-dz)) != null && (output.getWalls() & 0x04) == 0 &&
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
						if ((output = getTileAt(tx+dx, ty, tz+dz)) != null && (output.getWalls() & 0x04) == 0 && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
						if ((output = getTileAt(tx, ty+TILE_SIZE, tz)) != null && (output.getWalls() & 0x04) == 0 && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
						if ((output = getTileAt(tx, ty-TILE_SIZE, tz)) != null && (output.getWalls() & 0x04) == 0 && 
								(facing & output.getWalls()) != 0 && BuildingTile.checkRay(normal, output.getWalls()) != 0) {
							ow.setCamFacingByte(potentialFacingByte);
							return new Vector3f(tx, ty, tz);
						}
					}
	        	}
			}
			
			point.add(offset);
		}
		
		if (potentialConnect != null) {
			ow.setCamFacingByte(potentialConnectFacingByte);
		}
		return potentialConnect;
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
			
			ti = chunk.getTileItems().testCollision(origin, dir, point[0], tileY, point[1]);
			
			if (ti != null) {
				return ti;
			}
			
			float hitDist = plane.rayIntersection(origin, dir);
			if (hitDist < 0 || hitDist == Float.MAX_VALUE)
				continue;

			Vector3f hit = Vector3f.add(origin, Vector3f.mul(dir, hitDist));
			
			if (hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
				hit.y += .25f;
				return new TerrainIntersection(hit, 0, chunk);
			} else {
				plane = getChunkAt(point[0], point[1]).getPlane(point[0], point[1], true);
				hitDist = plane.rayIntersection(origin, dir);
				if (hitDist < 0 || hitDist == Float.MAX_VALUE)
					continue;

				hit = Vector3f.add(origin, Vector3f.mul(dir, hitDist));
				if (hit.x < point[0] + 1 && hit.x >= point[0] && hit.z < point[1] + 1 && hit.z >= point[1]) {
					hit.y += .25f;
					return new TerrainIntersection(hit, 0, chunk);
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
		
		float[][] heightmap = new float[Chunk.VERTEX_COUNT*2][Chunk.VERTEX_COUNT*3];
		Chunk chunk = new Chunk(x, z, this);
		chunk.heightmap = heightmap;
		chunk.items.initEmpty();
		chunk.arrX = arrX;
		chunk.arrZ = arrY;
		
		streamer.queueForLoading(chunk);
		
		data[arrX][arrY] = chunk;
	}
	
	public Chunk getChunkAt(float x, float z) {
		final float relx = x - data[0][0].realX;
		final float relz = z - data[0][0].realZ;
		final int tx = (int) Math.floor(relx / Chunk.CHUNK_SIZE);
		final int tz = (int) Math.floor(relz / Chunk.CHUNK_SIZE);
		
		if (tx < 0 || tz < 0 || tx >= data.length || tz >= data[0].length) {
			return null;
		}

		return data[tx][tz];
	}
	
	public BuildingTile getTileAt(float x, float y, float z) {
		Chunk chunkPtr = getChunkAt(x, z);
		
		return chunkPtr.getBuilding().getTileAt(
				x - chunkPtr.realX,
				y,
				z - chunkPtr.realZ);
	}
	
	public Material getMaterialAt(float x, float y, float z, byte facing) {
		BuildingTile tile = getTileAt(x,y,z);
		return tile == null ? Material.NONE : tile.getMaterial(facing);
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
