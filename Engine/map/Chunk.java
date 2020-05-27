package map;

import org.joml.Vector3f;

import core.Resources;
import core.res.Model;
import dev.Console;
import geom.Frustum;
import geom.Polygon;
import gl.particle.ParticleHandler;
import map.building.Building;
import procedural.BiomeVoronoi;
import procedural.terrain.TerrainMeshBuilder;
import util.ModelBuilder;

public class Chunk {
	/* Model vars */
	public static int POLYGON_SIZE = 1;//32;
	public static int VERTEX_COUNT = 16;//16;
	public static int CHUNK_SIZE = (VERTEX_COUNT - 1) * POLYGON_SIZE;
	public static float DIG_SIZE = 8 / 32f;

	/* Async loading */
	public static final byte UNLOADED = 0, LOADING = 1, LOADED = 2;

	public int x, z;
	public float[][] heightmap;
	private Chunk[] adjacent;

	private final Vector3f min, max;

	private byte loadState = UNLOADED;
	private Model groundModel, wallModel;
	private Building building;
	private boolean culled = false;

	public Chunk(int x, int z, Terrain terrain) {
		heightmap = new float[VERTEX_COUNT][VERTEX_COUNT];
		this.x = x;
		this.z = z;

		this.min = new Vector3f(x * CHUNK_SIZE, -1000, z * CHUNK_SIZE);
		this.max = new Vector3f(min.x + CHUNK_SIZE, 1000, min.z + CHUNK_SIZE);
		
		adjacent = new Chunk[4];
		
		building = new Building(this);
	}

	public void checkIfCulled(Frustum frustum) {
		culled = !frustum.containsBoundingBox(max, min);
	}

	public void cleanUp() {
		
		if (groundModel != null) {
			groundModel.cleanUp();
		}

		
		if (wallModel != null) {
			wallModel.cleanUp();
		}
		this.building.cleanUp();
		this.building = null;
		this.groundModel = null;
		this.wallModel = null;
	}

	public byte getLoadedState() {
		return loadState;
	}

	public Model getModel() {
		return groundModel;
	}
	
	public Model getWallModel() {
		return wallModel;
	}
	
	public void build(int x, int y, int z, byte wall, int item) {
		building.add2x2(x, y, z, wall, item);
	}
	
	public void destroy(int x, int y, int z, byte wall, int item) {
		building.remove(x, y, z, wall, item);
	}
	
	public void smoothHeight(int x, int z) {
		float tr = heightmap[x*2+2][z*2+1];
		float tl = heightmap[x*2+1][z*2+1];
		float bl = heightmap[x*2+1][z*2+2];
		float br = heightmap[x*2+2][z*2+2];
		float lowest = Math.min(tr, Math.min(tl, Math.min(br, bl)));
		lowest = (float) (Math.floor(lowest / Chunk.DIG_SIZE)*Chunk.DIG_SIZE);
		
		setHeight(x, z, lowest);
	}
	
	public void setHeight(int x, int z, float offset) {
		setHeightmap((x*2)+2, (z*2)+1, offset);
		setHeightmap((x*2)+1, (z*2)+1, offset);
		setHeightmap((x*2)+1, (z*2)+2, offset);
		setHeightmap((x*2)+2, (z*2)+2, offset);
		
		rebuildModel(x, z);
		
		int rx = ((this.x * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		int rz = ((this.z * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;
		
		if (offset <= 0) {
			int half = Chunk.POLYGON_SIZE/2;
			ParticleHandler.addBurst(Resources.getTexture("particles"), 0, 0, new Vector3f((rx + half), heightmap[x*2 + 2][z*2 + 1], rz + half),30);
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
		
		int rx = ((this.x * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		int rz = ((this.z * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;
		
		if (offset <= 0) {
			int half = Chunk.POLYGON_SIZE/2;
			ParticleHandler.addBurst(Resources.getTexture("particles"), 0, 0,
					new Vector3f((rx + half), heightmap[x*2 + 2][z*2 + 1], rz + half),
					30);
		}
	}

	private void rebuildModel(int x, int z) {
		// Adjust heightmap model
		int index = (z * (Chunk.VERTEX_COUNT - 1) + (x));

		int rx = ((this.x * (Chunk.VERTEX_COUNT - 1)) + x) * Chunk.POLYGON_SIZE;
		int rz = ((this.z * (Chunk.VERTEX_COUNT - 1)) + z) * Chunk.POLYGON_SIZE;

		this.getModel().getVbo(0).updateData((index * 12),
				new float[] { (rx + Chunk.POLYGON_SIZE), heightmap[x * 2 + 2][z * 2 + 1], rz, rx,
						heightmap[x * 2 + 1][z * 2 + 1], rz,

						rx, heightmap[x * 2 + 1][z * 2 + 2], (rz + Chunk.POLYGON_SIZE), (rx + Chunk.POLYGON_SIZE),
						heightmap[x * 2 + 2][z * 2 + 2] });

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
				adjacent[1].heightmap[0][z] = heightmap[heightmap.length-2][z];
			}
			if (z == heightmap.length-2) {
				adjacent[3].heightmap[x][0] = heightmap[x][heightmap.length-2];
			}
			if (x == 1) {
				adjacent[0].heightmap[heightmap.length-1][z] = heightmap[1][z];
			}
			if (z == 1) {
				adjacent[2].heightmap[x][heightmap.length-1] = heightmap[x][1];
			}
		} else {
			if (adjX == -1) {
				adjacent[0].heightmap[x][z] = val;
			} else if (adjX == 1) {
				adjacent[1].heightmap[x][z] = val;
			} else if (adjZ == -1) {
				adjacent[2].heightmap[x][z] = val;
			} else {
				adjacent[3].heightmap[x][z] = val;
			}
		}
	}

	private void rebuildWalls() {
		ModelBuilder wallBuilder = new ModelBuilder();
		
		int cx = this.x*Chunk.CHUNK_SIZE;
		int cz = this.z*Chunk.CHUNK_SIZE;
		
		for(int j = 1; j < heightmap.length; j += 2) {
			for (int i = 1; i < heightmap.length; i += 2) {
				int rx = i / 2 * Chunk.POLYGON_SIZE;
				int rz = j / 2 * Chunk.POLYGON_SIZE;
				final int s = Chunk.POLYGON_SIZE;
				
				float p1, p1n, p2, p2n;	// Point1, point1 neighbor, point2, point2 neighbor
				// Left/Right wall
				p1 = heightmap[i][j];//heightLookup(i, j);
				p1n = heightmap[i-1][j];//heightLookup(i - 1, j);
				p2 = heightmap[i][j+1];//heightLookup(i, j + 1);
				p2n = heightmap[i-1][j+1];//heightLookup(i - 1, j + 1);
				
				if (p1 != p1n || p2 != p2n) {
					TerrainMeshBuilder.addWall(wallBuilder, new Vector3f(cx + rx, p1n, cz + rz),
							new Vector3f(cx + rx, p2n, cz + rz + s), new Vector3f(cx + rx, p2, cz + rz + s),
							new Vector3f(cx + rx, p1, cz + rz));
				}
				
				if (i == 1) continue;
				// Top/Bottom wall
				p1 = heightmap[i-1][j];//heightLookup(i, j);
				p1n = heightmap[i-1][j-1];//heightLookup(i - 1, j);
				p2 = heightmap[i-2][j];//heightLookup(i, j + 1);
				p2n = heightmap[i-2][j-1];//heightLookup(i - 1, j + 1);
				
				if (p1 != p1n || p2 != p2n) {
					TerrainMeshBuilder.addWall(wallBuilder, new Vector3f(cx + rx, p1n, cz + rz),
							new Vector3f(cx + rx - s, p2n, cz + rz), new Vector3f(cx + rx - s, p2, cz + rz),
							new Vector3f(cx + rx, p1, cz + rz));
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
				return adjacent[0].heightmap[xRel][zRel];
			} else if (adjX == 1) {
				return adjacent[1].heightmap[xRel][zRel];
			} else if (adjZ == -1) {
				return adjacent[2].heightmap[xRel][zRel];
			} else {
				return adjacent[3].heightmap[xRel][zRel];
			}
		}
	}

	/*public void smooth(int x, int y, int z) {
		if (Math.abs(heightmap[x][z] - y) > 3) return;
		
		heightmap[x][z] -= Math.floor(heightmap[x][z]);
		this.getModel().getVbo(0).updateData(z*Chunk.VERTEX_COUNT + x, new float[] {heightmap[x][z]});
	}*/

	public Polygon getPolygon(float x, float z) {
		final float relx = x - this.x * Chunk.CHUNK_SIZE;
		final float relz = z - this.z * Chunk.CHUNK_SIZE;
		int terrainx = (int) Math.floor(relx / Chunk.POLYGON_SIZE);
		int terrainz = (int) Math.floor(relz / Chunk.POLYGON_SIZE);

		final float trueX = this.x * Chunk.CHUNK_SIZE + terrainx * Chunk.POLYGON_SIZE;
		final float trueZ = this.z * Chunk.CHUNK_SIZE + terrainz * Chunk.POLYGON_SIZE;

		if (terrainx < 0 || terrainz < 0 || terrainx >= heightmap.length - 1 || terrainz >= heightmap.length - 1) {
			System.err.println(relx + "," + terrainx + "," + this.x * Chunk.CHUNK_SIZE);
			System.err.println(relz + "," + terrainz + "," + this.z * Chunk.CHUNK_SIZE);
			System.err.println();
			return null;
		}

		final float localx = relx % Chunk.POLYGON_SIZE;
		final float localz = relz % Chunk.POLYGON_SIZE;

		// PlayingSceneGui.posDgbX.setText(""+localx);
		// PlayingSceneGui.posDgbZ.setText(""+localz);
		
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

	public boolean isCulled() {
		return culled;
	}

	void load(Terrain terrain, BiomeVoronoi biomeVoronoi) {
		// loadState = LOADING;
		
		Model[] models = TerrainMeshBuilder.buildMeshes(this, biomeVoronoi);
		
		groundModel = models[0];
		wallModel = models[1];
		
		loadState = LOADED;
	}

	public Chunk[] getAdjacents() {
		return adjacent;
	}

	public Building getBuilding() {
		return building;
	}
}
