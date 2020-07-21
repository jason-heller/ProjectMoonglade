package map;

import org.joml.Vector3f;

import dev.Debug;
import geom.Frustum;
import geom.Plane;
import geom.Polygon;
import gl.particle.ParticleHandler;
import gl.res.Model;
import map.prop.ChunkProps;
import map.prop.Props;
import map.prop.StaticProp;
import map.prop.StaticPropProperties;
import map.tile.BuildData;
import procedural.NoiseUtil;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;
import procedural.terrain.TerrainMeshBuilder;
import procedural.terrain.WaterMeshBuilder;
import scene.entity.EntityHandler;
import scene.entity.utility.FallingTreeEntity;
import scene.entity.utility.ItemEntity;
import util.ModelBuilderOld;

public class Chunk {
	
	/* Model vars */
	public static int POLYGON_SIZE = 1;		// Maybe remove this later?
	public static int VERTEX_COUNT = 16;	// 16;
	public static int CHUNK_SIZE = (VERTEX_COUNT - 1) * POLYGON_SIZE;
	public static float DIG_SIZE = 1f;

	/* Async loading */
	public static final byte UNLOADED = 0, LOADING = 1, BUILDING = 2, LOADED = 3, UNLOADING = 4, GENERATING = 5;
	private byte state = UNLOADED;

	/* position */
	public final int dataX, dataZ; 		// Position of chunk in chunk space
	public final int realX, realZ; 		// Position of chunk in world space
	public int arrX, arrZ;				// Position in chunk array

	private Vector3f min;				// \ Chunk bounds for frustum culling
	private Vector3f max;				// /
	private boolean culled = true;		// Flag, set if chunk is to be culled (usually from frustum culling, if not always)

	/* Chunk data */
	public float[][] heightmap;			// Terrain heightmap
	public ChunkProps chunkProps;
	public float[][] waterTable;		// Water heightmap, effectively
	private BuildData building;			// Building tiles
	
	private long seed;					// Local seed for chunk, used in chunk generation
	
	/* Graphics */
	private Model groundModel, wallModel, waterModel;	// Models for the ground and water surface
	
	private Terrain terrain;			// Pointer to the terrain object
	
	public byte editFlags;				// Flags that are set when a certain section of chunk data is modified (see "chunk data" above)
										// Used for compressing/determining what data to save/load
	
	public Chunk(int x, int z, Terrain terrain) {
		heightmap = new float[VERTEX_COUNT*2][VERTEX_COUNT*2];
		waterTable = new float[VERTEX_COUNT][VERTEX_COUNT];
		this.dataX = x;
		this.dataZ = z;
		this.realX = x * CHUNK_SIZE;
		this.realZ = z * CHUNK_SIZE;

		this.min = new Vector3f(realX, -128, realZ);
		this.max = new Vector3f(min.x + CHUNK_SIZE, 1, min.z + CHUNK_SIZE);
		
		chunkProps = new ChunkProps(x, z, this);
		
		building = new BuildData(this);
		this.terrain = terrain;
		
		seed = NoiseUtil.szudzik(x*VERTEX_COUNT, z*VERTEX_COUNT) * (Enviroment.seed + 2113);
	}
	
	void generate(Terrain terrain, BiomeVoronoi biomeVoronoi) {
		final int wid = (VERTEX_COUNT-1);
		if (Debug.flatTerrain) {
			GenTerrain.buildFlatTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		} else {
			GenTerrain.buildTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		}
		setState(LOADED);
	}
	
	public void finishGenerationPass(BiomeVoronoi biomeVoronoi) {
		Model[] models = TerrainMeshBuilder.buildMeshes(this, biomeVoronoi);
		chunkProps.buildModel();
		
		groundModel = models[0];
		wallModel = models[1];
		waterModel = WaterMeshBuilder.buildChunkMesh(this);
		setState(Chunk.LOADED);
	}
	
	public void build(BiomeVoronoi biomeVoronoi) {
		building.buildModel();
		
		final int wid = (VERTEX_COUNT-1);
		GenTerrain.buildTerrain(this, dataX*wid, 0, dataZ*wid, VERTEX_COUNT, POLYGON_SIZE, biomeVoronoi);
		
		Model[] models = TerrainMeshBuilder.buildMeshes(this, biomeVoronoi);
		chunkProps.buildModel();
		
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
		this.chunkProps.cleanUp();

		/*this.building = null;
		this.groundModel = null;
		this.waterModel = null;
		this.wallModel = null;
		this.heightmap = null;
		this.waterTable = null;
		this.items = null;
		this.max = null;
		this.min = null;
		this.terrain = null;*/
	}

	public void setTile(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		building.setTile(x, y, z, wall, material, specialFlags);
		editFlags |= 0x01;
	}
	
	public void setState(byte state) {
		this.state = state;
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
	
	public void damangeProp(int relX, int relZ, byte damage) {
		editFlags |= 0x02;
		Props id = chunkProps.getProp(relX, relZ);
		StaticPropProperties props = chunkProps.getEntityProperties(relX, relZ);
		final StaticProp tile = Props.get(id);
		if (tile != null) {
			props.damage -= damage;
			
			if (props.damage <= 0) {
				float dx = ((relX+realX)*Chunk.POLYGON_SIZE)+.5f;
				float dz = ((relZ+realZ)*Chunk.POLYGON_SIZE)+.5f;
				float dy = this.getTerrain().getHeightAt(dx, dz);
				EntityHandler.addEntity(new FallingTreeEntity(tile, dx, dy, dz, props.scale));
				destroyProp(relX, relZ);
			}
		}
	}
	
	public boolean destroyProp(int relX, int relZ) {
		editFlags |= 0x02;
		Props id = chunkProps.getProp(relX, relZ);
		final StaticProp tile = Props.get(id);
		
		if (tile != null) {
			//((Overworld) Application.scene).getInventory().addItem(tile.getDrop(), tile.getNumDrops());
			Vector3f pos = new Vector3f(realX + relX, heightLookup(relX, relZ), realZ + relZ);
			pos.add(.5f, .5f, .5f);
			EntityHandler.addEntity(new ItemEntity(pos, tile.getDrop(), tile.getNumDrops()));
			
			chunkProps.removeEntity(relX, relZ);
			chunkProps.buildModel();
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
		ModelBuilderOld wallBuilder = new ModelBuilderOld();
		
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
	
	public Plane getPlane(float x, float z, boolean bottomPlane) {
		final float relx = x - this.dataX * Chunk.CHUNK_SIZE;
		final float relz = z - this.dataZ * Chunk.CHUNK_SIZE;
		int terrainx = (int) Math.floor(relx / Chunk.POLYGON_SIZE);
		int terrainz = (int) Math.floor(relz / Chunk.POLYGON_SIZE);

		if (terrainz < 0 || terrainz < 0 || terrainz >= Chunk.VERTEX_COUNT || terrainz >= Chunk.VERTEX_COUNT) return null;
		
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
		final float relx = x - this.realX;
		final float relz = z - this.realZ;
		int terrainx = (int) relx;
		int terrainz = (int) relz;

		final float trueX = this.dataX * Chunk.CHUNK_SIZE + terrainx * Chunk.POLYGON_SIZE;
		final float trueZ = this.dataZ * Chunk.CHUNK_SIZE + terrainz * Chunk.POLYGON_SIZE;

		if (terrainx < 0 || terrainz < 0 || terrainx >= heightmap.length - 1 || terrainz >= heightmap.length - 1) {
			System.err.println(x + "," + terrainx + "," + this.realX);
			System.err.println(z + "," + terrainz + "," + this.realZ);
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
		return p;

	}

	public float getWaterHeight(int terrainx, int terrainz) {
		return waterTable[terrainx][terrainz];
	}
	
	public boolean isCulled() {
		return culled;
	}

	public BuildData getBuilding() {
		return building;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public ChunkProps getProps() {
		return chunkProps;
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
	
	public StaticPropProperties getChunkPropProperties(int relX, int relZ) {
		return chunkProps.getEntityProperties(relX, relZ);
	}
}
