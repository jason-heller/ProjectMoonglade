package map;

import org.joml.Vector3f;

import core.Application;
import dev.Debug;
import geom.Frustum;
import geom.Plane;
import geom.Polygon;
import gl.particle.ParticleHandler;
import gl.res.Model;
import map.building.Building;
import map.tile.ChunkTiles;
import map.tile.EnvTile;
import map.tile.TileProperties;
import procedural.NoiseUtil;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;
import procedural.terrain.TerrainMeshBuilder;
import procedural.terrain.WaterMeshBuilder;
import scene.entity.EntityHandler;
import scene.entity.utility.FallingTreeEntity;
import scene.overworld.Overworld;
import util.ModelBuilder;

public class Chunk {
	/* Model vars */
	public static int POLYGON_SIZE = 1;//32;
	public static int VERTEX_COUNT = 16;//16;
	public static int CHUNK_SIZE = (VERTEX_COUNT - 1) * POLYGON_SIZE;
	public static float DIG_SIZE = 1f;

	/* Async loading */
	public static final byte UNLOADED = 0, LOADING = 1, BUILDING = 2, LOADED = 3, UNLOADING = 4, GENERATING = 5;

	public final int dataX, dataZ;
	public final int realX, realZ;
	public float[][] heightmap;
	public ChunkTiles items;
	public float[][] waterTable;
	
	public int arrX, arrZ;

	private Vector3f min;
	private Vector3f max;

	private byte state = UNLOADED;
	private Model groundModel, wallModel, waterModel;
	private Building building;
	private boolean culled = true;
	
	private Terrain terrain;
	
	public byte editFlags;
	
	private long seed;

	public Chunk(int x, int z, Terrain terrain) {
		heightmap = new float[VERTEX_COUNT][VERTEX_COUNT];
		waterTable = new float[VERTEX_COUNT][VERTEX_COUNT];
		this.dataX = x;
		this.dataZ = z;
		this.realX = x * CHUNK_SIZE;
		this.realZ = z * CHUNK_SIZE;

		this.min = new Vector3f(realX, -128, realZ);
		this.max = new Vector3f(min.x + CHUNK_SIZE, 1, min.z + CHUNK_SIZE);
		
		items = new ChunkTiles(x, z, this, null);
		
		building = new Building(this);
		this.terrain = terrain;
		
		seed = NoiseUtil.szudzik(x*VERTEX_COUNT, z*VERTEX_COUNT) * (Enviroment.seed + 2113);
	}
	
	void generate(Terrain terrain, BiomeVoronoi biomeVoronoi, int arrX, int arrZ) {
		//loadState = BUILDING;
		final int wid = (VERTEX_COUNT-1);
		if (Debug.flatTerrain) {
			GenTerrain.buildFlatTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		} else {
			GenTerrain.buildTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		}
		setState(LOADED);
	}
	
	void finishGenerationPass(BiomeVoronoi biomeVoronoi) {
		Model[] models = TerrainMeshBuilder.buildMeshes(this, biomeVoronoi);
		items.buildModel();
		
		groundModel = models[0];
		wallModel = models[1];
		waterModel = WaterMeshBuilder.buildChunkMesh(this);
	}
	
	public void setState(byte state) {
		this.state = state;
	}
	
	public void build(BiomeVoronoi biomeVoronoi) {
		building.buildModel();
		
		final int wid = (VERTEX_COUNT-1);
		GenTerrain.buildTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		
		Model[] models = TerrainMeshBuilder.buildMeshes(this, biomeVoronoi);
		items.buildModel();
		
		groundModel = models[0];
		wallModel = models[1];
		waterModel = WaterMeshBuilder.buildChunkMesh(this);
		rebuildWalls();
		
		setState(LOADED);
	}

	public void checkIfCulled(Frustum frustum) {
		culled = !frustum.containsBoundingBox(max, min);
	}

	public void cleanUp() {
		EntityHandler.onChunkUnload(this);
		
		if (groundModel != null) {
			groundModel.cleanUp();
		}
		if (waterModel != null) {
			waterModel.cleanUp();
		}
		if (wallModel != null) {
			wallModel.cleanUp();
		}
		this.building.cleanUp();
		this.items.cleanUp();
		
		this.building = null;
		this.groundModel = null;
		this.waterModel = null;
		this.wallModel = null;
		this.heightmap = null;
		this.waterTable = null;
		this.items = null;
		this.max = null;
		this.min = null;
		this.terrain = null;
	}

	public byte getState() {
		return state;
	}

	public Model getGroundModel() {
		return groundModel;
	}
	
	public Model getWallModel() {
		return wallModel;
	}
	
	public Model getWaterModel() {
		return waterModel;
	}
	
	public void setTile(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		building.setTile(x, y, z, wall, material, specialFlags);
		editFlags |= 0x01;
	}
	
	public void smoothHeight(int x, int z) {
		float tr = heightmap[x*2+2][z*2+1];
		float tl = heightmap[x*2+1][z*2+1];
		float bl = heightmap[x*2+1][z*2+2];
		float br = heightmap[x*2+2][z*2+2];
		float lowest = Math.min(tr, Math.min(tl, Math.min(br, bl)));
		lowest = (float) (Math.round(lowest / Chunk.DIG_SIZE)*Chunk.DIG_SIZE);
		
		setHeight(x, z, lowest);
	}
	
	public TileProperties getEnvTileProperties(int relX, int relZ) {
		return items.getTileProperties(relX, relZ);
	}
	
	public void damageEnvTile(int relX, int relZ, byte damage) {
		editFlags |= 0x02;
		int id = items.getTileId(relX, relZ);
		TileProperties props = items.getTileProperties(relX, relZ);
		final EnvTile tile = terrain.getTileById(id);
		if (tile != null) {
			props.damage -= damage;
			
			if (props.damage <= 0) {
				float dx = ((relX+realX)*Chunk.POLYGON_SIZE)+.5f;
				float dz = ((relZ+realZ)*Chunk.POLYGON_SIZE)+.5f;
				float dy = this.getTerrain().getHeightAt(dx, dz);
				EntityHandler.addEntity(new FallingTreeEntity(tile, dx, dy, dz, props.scale));
				breakEnvTile(relX, relZ);
			}
		}
	}
	
	public boolean breakEnvTile(int relX, int relZ) {
		editFlags |= 0x02;
		int id = items.getTileId(relX, relZ);
		final EnvTile tile = terrain.getTileById(id);
		
		if (tile != null) {
			((Overworld) Application.scene).getInventory().addItem(tile.getDrop(), tile.getNumDrops());

			items.removeTile(relX, relZ);
			items.buildModel();
			return true;
		}
		
		return false;
	}
	
	public void setHeight(int x, int z, float offset) {
		setHeightmap((x*2)+2, (z*2)+1, offset);
		setHeightmap((x*2)+1, (z*2)+1, offset);
		setHeightmap((x*2)+1, (z*2)+2, offset);
		setHeightmap((x*2)+2, (z*2)+2, offset);
		
		rebuildModel(x, z);
		
		if (x == 0) {
			terrain.get(arrX-1, arrZ).rebuildWalls();
		} else if (x == Chunk.VERTEX_COUNT-2) {
			terrain.get(arrX+1, arrZ).rebuildWalls();
		}
		
		if (z == 0) {
			terrain.get(arrX, arrZ-1).rebuildWalls();
		} else if (z == Chunk.VERTEX_COUNT-2) {
			terrain.get(arrX, arrZ+1).rebuildWalls();
		}
		
		float rx = ((this.dataX * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		float rz = ((this.dataZ * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;
		rx += (Chunk.POLYGON_SIZE/2f);
		rz += (Chunk.POLYGON_SIZE/2f);
		
		if (offset <= 0) {
			ParticleHandler.addBurst("particles", 0, 0,
					new Vector3f(rx, heightmap[x*2 + 2][z*2 + 1], rz));
		}
	}
	
	public void addHeight(int x, int z, float offset) {
		float tr = heightmap[x*2+2][z*2+1];
		float tl = heightmap[x*2+1][z*2+1];
		float bl = heightmap[x*2+1][z*2+2];
		float br = heightmap[x*2+2][z*2+2];
		
		setHeightmap((x*2)+2, (z*2)+1, tr+offset);
		setHeightmap((x*2)+1, (z*2)+1, tl+offset);
		setHeightmap((x*2)+1, (z*2)+2, bl+offset);
		setHeightmap((x*2)+2, (z*2)+2, br+offset);
		
		rebuildModel(x, z);

		if (x == 0) {
			terrain.get(arrX-1, arrZ).rebuildWalls();
		} else if (x == Chunk.VERTEX_COUNT-2) {
			terrain.get(arrX+1, arrZ).rebuildWalls();
		}
		
		if (z == 0) {
			terrain.get(arrX, arrZ-1).rebuildWalls();
		} else if (z == Chunk.VERTEX_COUNT-2) {
			terrain.get(arrX, arrZ+1).rebuildWalls();
		}
		
		float rx = ((this.dataX * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		float rz = ((this.dataZ * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;
		rx += (Chunk.POLYGON_SIZE/2f);
		rz += (Chunk.POLYGON_SIZE/2f);
		
		if (offset <= 0) {
			ParticleHandler.addBurst("particles", 0, 0,
					new Vector3f(rx, heightmap[x*2 + 2][z*2 + 1], rz));
		}
	}

	private void rebuildModel(int x, int z) {
		// Adjust heightmap model
		int index = (z * (Chunk.VERTEX_COUNT - 1) + (x));

		int rx = ((this.dataX * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		int rz = ((this.dataZ * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;
		
		final float topRight = heightmap[x * 2 + 2][z * 2 + 1];
		final float topLeft = heightmap[x * 2 + 1][z * 2 + 1];
		final float btmRight = heightmap[x * 2 + 1][z * 2 + 2];
		final float btmLeft = heightmap[x * 2 + 2][z * 2 + 2];
		
		Vector3f normal = Vector3f.cross(new Vector3f(POLYGON_SIZE, topRight-topLeft, 0),
				new Vector3f(0, btmLeft-topLeft, POLYGON_SIZE));
		normal.normalize().negate();

		this.getGroundModel().getVbo(0).updateData((index * 12),
				new float[] { (rx + Chunk.POLYGON_SIZE), topRight, rz,
						rx, topLeft, rz,
						rx, btmRight, (rz + Chunk.POLYGON_SIZE),
						(rx + Chunk.POLYGON_SIZE), btmLeft });
		
		this.getGroundModel().getVbo(2).updateData((index * 12),
				new float[] {normal.x, normal.y, normal.z, normal.x, normal.y, normal.z,
						normal.x, normal.y, normal.z, normal.x, normal.y, normal.z});

		// Fill in gaps
		rebuildWalls();
	}

	private void setHeightmap(int x, int z, float val) {
		int len = heightmap.length;
		int adjX = (int)Math.floor(x / (float)len);
		int adjZ = (int)Math.floor(z / (float)len);
		x = (x + len) % len;
		z = (z + len) % len;
		
		if (adjX == 0 && adjZ == 0) {
			heightmap[x][z] = val;
			if (x == heightmap.length-2) {
				terrain.get(arrX+1, arrZ).heightmap[0][z] = val;
			}
			if (z == heightmap.length-2) {
				terrain.get(arrX, arrZ+1).heightmap[x][0] = val;
			}
			if (x == 1) {
				terrain.get(arrX-1, arrZ).heightmap[heightmap.length-1][z] = val;
			}
			if (z == 1) {
				terrain.get(arrX, arrZ-1).heightmap[x][heightmap.length-1] = val;
			}
		} else {
			if (adjX == -1) {
				terrain.get(arrX-1, arrZ).heightmap[x][z] = val;
			} else if (adjX == 1) {
				terrain.get(arrX+1, arrZ).heightmap[x][z] = val;
			} else if (adjZ == -1) {
				terrain.get(arrX, arrZ-1).heightmap[x][z] = val;
			} else {
				terrain.get(arrX, arrZ+1).heightmap[x][z] = val;
			}
		}
		
		editFlags |= 0x04;
	}

	public void rebuildWalls() {
		ModelBuilder wallBuilder = new ModelBuilder();
		
		for(int j = 1; j < heightmap.length; j += 2) {
			for (int i = 1; i < heightmap.length; i += 2) {
				int rx = i / 2 * Chunk.POLYGON_SIZE;
				int rz = j / 2 * Chunk.POLYGON_SIZE;
				final int s = Chunk.POLYGON_SIZE;
				float p1, p1n, p2, p2n;	// Point1, point1 neighbor, point2, point2 neighbor
				
				
				if (j != heightmap.length-1) {
					// Left/Right wall
					p1 = heightmap[i][j];//heightLookup(i, j);
					p1n = heightmap[i-1][j];//heightLookup(i - 1, j);
					p2 = heightmap[i][j+1];//heightLookup(i, j + 1);
					p2n = heightmap[i-1][j+1];//heightLookup(i - 1, j + 1);
					
					if (p1 != p1n || p2 != p2n) {
						TerrainMeshBuilder.addWall(wallBuilder, new Vector3f(realX + rx, p1n, realZ + rz),
								new Vector3f(realX + rx, p2n, realZ + rz + s), new Vector3f(realX + rx, p2, realZ + rz + s),
								new Vector3f(realX + rx, p1, realZ + rz), (i-2)/2, (j-1)/2, this);
					}
				}
				
				if (i != 1) {
					// Top/Bottom wall
					p1 = heightmap[i-1][j];//heightLookup(i, j);
					p1n = heightmap[i-1][j-1];//heightLookup(i - 1, j);
					p2 = heightmap[i-2][j];//heightLookup(i, j + 1);
					p2n = heightmap[i-2][j-1];//heightLookup(i - 1, j + 1);
					
					if (p1 != p1n || p2 != p2n) {
						TerrainMeshBuilder.addWall(wallBuilder, new Vector3f(realX + rx, p1n, realZ + rz),
								new Vector3f(realX + rx - s, p2n, realZ + rz), new Vector3f(realX + rx - s, p2, realZ + rz),
								new Vector3f(realX + rx, p1, realZ + rz), (i-2)/2, (j-1)/2, this);
					}
				}
			}
		}
		
		this.wallModel.cleanUp();
		this.wallModel = wallBuilder.finish();
	}
	
	/** Looks up heights found within this chunk of its adjacents<br><br>
	 * <b>NOTE:</b> This does no error handling, so if you call this
	 * looking for data outside this chunk or its axis-aligned
	 * neighbors it will just crash the game
	 * @param xRel height location relative to the chunk's X position
	 * @param zRel height location relative to the chunk's Z position
	 */
	public float heightLookup(int xRel, int zRel) {
		int len = heightmap.length;
		int adjX = (int)Math.floor(xRel / (float)len);
		int adjZ = (int)Math.floor(zRel / (float)len);
		xRel = (xRel + len) % len;
		zRel = (zRel + len) % len;
		
		if (adjX == 0 && adjZ == 0) {
			return heightmap[xRel][xRel];
		} else {
			if (adjX == -1) {
				return terrain.get(arrX-1, arrZ).heightmap[xRel][zRel];
			} else if (adjX == 1) {
				return terrain.get(arrX+1, arrZ).heightmap[xRel][zRel];
			} else if (adjZ == -1) {
				return terrain.get(arrX, arrZ-1).heightmap[xRel][zRel];
			} else {
				return terrain.get(arrX, arrZ+1).heightmap[xRel][zRel];
			}
		}
	}

	/*public void smooth(int x, int y, int z) {
		if (Math.abs(heightmap[x][z] - y) > 3) return;
		
		heightmap[x][z] -= Math.floor(heightmap[x][z]);
		this.getModel().getVbo(0).updateData(z*Chunk.VERTEX_COUNT + x, new float[] {heightmap[x][z]});
	}*/
	
	public Plane getPlane(float x, float z, boolean bottomPlane) {
		final float relx = x - this.dataX * Chunk.CHUNK_SIZE;
		final float relz = z - this.dataZ * Chunk.CHUNK_SIZE;
		int terrainx = (int) Math.floor(relx / Chunk.POLYGON_SIZE);
		int terrainz = (int) Math.floor(relz / Chunk.POLYGON_SIZE);

		final float trueX = this.dataX * Chunk.CHUNK_SIZE + terrainx * Chunk.POLYGON_SIZE;
		final float trueZ = this.dataZ * Chunk.CHUNK_SIZE + terrainz * Chunk.POLYGON_SIZE;
		
		terrainx = (terrainx*2)+1;
		terrainz = (terrainz*2)+1;
		
		if (bottomPlane) {
			return new Plane(new Vector3f(trueX, heightmap[terrainx][terrainz], trueZ),
					new Vector3f(trueX, heightmap[terrainx][terrainz + 1], trueZ + POLYGON_SIZE),
					new Vector3f(trueX + POLYGON_SIZE, heightmap[terrainx + 1][terrainz], trueZ));
		}
		
		return new Plane(new Vector3f(trueX, heightmap[terrainx + 1][terrainz], trueZ),
				new Vector3f(trueX, heightmap[terrainx][terrainz + 1], trueZ + POLYGON_SIZE),
				new Vector3f(trueX + POLYGON_SIZE, heightmap[terrainx + 1][terrainz + 1], trueZ));
	}

	public Polygon getPolygon(float x, float z) {
		final float relx = x - this.dataX * Chunk.CHUNK_SIZE;
		final float relz = z - this.dataZ * Chunk.CHUNK_SIZE;
		int terrainx = (int) Math.floor(relx / Chunk.POLYGON_SIZE);
		int terrainz = (int) Math.floor(relz / Chunk.POLYGON_SIZE);

		final float trueX = this.dataX * Chunk.CHUNK_SIZE + terrainx * Chunk.POLYGON_SIZE;
		final float trueZ = this.dataZ * Chunk.CHUNK_SIZE + terrainz * Chunk.POLYGON_SIZE;

		if (terrainx < 0 || terrainz < 0 || terrainx >= heightmap.length - 1 || terrainz >= heightmap.length - 1) {
			System.err.println(relx + "," + terrainx + "," + this.dataX * Chunk.CHUNK_SIZE);
			System.err.println(relz + "," + terrainz + "," + this.dataZ * Chunk.CHUNK_SIZE);
			System.err.println();
			return null;
		}

		final float localx = relx % Chunk.POLYGON_SIZE;
		final float localz = relz % Chunk.POLYGON_SIZE;
		
		terrainx = (terrainx*2)+1;
		terrainz = (terrainz*2)+1;

		if (localx <= POLYGON_SIZE - localz) {
			final Polygon p = new Polygon(new Vector3f(trueX, heightmap[terrainx][terrainz], trueZ),
					new Vector3f(trueX, heightmap[terrainx][terrainz + 1], trueZ + POLYGON_SIZE),
					new Vector3f(trueX + POLYGON_SIZE, heightmap[terrainx + 1][terrainz], trueZ));
			return p;
		}

		final Polygon p = new Polygon(new Vector3f(trueX + POLYGON_SIZE, heightmap[terrainx + 1][terrainz], trueZ),
				new Vector3f(trueX, heightmap[terrainx][terrainz + 1], trueZ + POLYGON_SIZE),
				new Vector3f(trueX + POLYGON_SIZE, heightmap[terrainx + 1][terrainz + 1], trueZ + POLYGON_SIZE));
		return p;// new Vector2f(MathUtil.barryCentric(p.p1, p.p2, p.p3, new Vector2f(localx,
					// localz)), p.normal.y);

	}

	public float getWaterHeight(int terrainx, int terrainz) {
		return waterTable[terrainx][terrainz];
	}
	
	public boolean isCulled() {
		return culled;
	}

	public Building getBuilding() {
		return building;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public ChunkTiles getTileItems() {
		return items;
	}

	public Vector3f getMax() {
		return max;
	}
	
	public Vector3f getMin() {
		return min;
	}

	public long getSeed() {
		return seed;
	}
}
