package procedural.terrain;

import static map.Chunk.POLYGON_SIZE;
import static map.Chunk.VERTEX_COUNT;

import org.joml.Vector3f;

import dev.Debug;
import gl.res.Model;
import gl.res.Vbo;
import map.Chunk;
import map.tile.BuildData;
import map.tile.Tile;
import procedural.biome.Biome;
import procedural.biome.BiomeData;
import procedural.biome.BiomeVoronoi;
import procedural.biome.types.BiomeColors;
import util.ModelBuilder;
import util.ModelBuilderOld;

public class TerrainMeshBuilder {
	
	public static final float TERRAIN_ATLAS_SIZE = 1f / 8f;
	
	private static ModelBuilder groundBuilder;
	private static ModelBuilder wallBuilder;
	
	private static int groundIndices = 0, wallIndices = 0;
	
	private static float[] random;
	private static int randomIndex = 0;

	public static void init() {
		random = new float[100];
		for (int i = 0; i < 100; i++)
			random[i] = (float) Math.random();
	}

	public static float getRandom() {
		if (randomIndex >= 99)
			randomIndex = 0;
		else
			randomIndex = randomIndex + 1;
		return random[randomIndex];
	}
	
	private static Vbo[][] buildChunkMesh(Chunk chunk, BiomeVoronoi biomeVoronoi) {
		final int x = chunk.dataX * (VERTEX_COUNT-1);
		final int z = chunk.dataZ * (VERTEX_COUNT-1);
		final float[][] heights = chunk.heightmap;
		
		groundBuilder = new ModelBuilder(true, true, true);
		wallBuilder = new ModelBuilder(true, true, true);
		groundIndices = 0;
		wallIndices = 0;
		
		if ((chunk.editFlags & 0x04) != 0) {
			for(int j = 0; j < VERTEX_COUNT; j++) {
				for(int i = 0; i < VERTEX_COUNT; i++) {
					int dx = (i)-1;
					int dz = (j)-1;
					BiomeData biomeData = biomeVoronoi.getDataAt((x+i)*POLYGON_SIZE, (z+j)*POLYGON_SIZE);
					
					if (j != 0 && i != 0) {
						addTile((x+dx)*POLYGON_SIZE, (z+dz)*POLYGON_SIZE,
								heights[i*2-1][j*2-1], heights[i*2][j*2-1],
								heights[i*2-1][j*2], heights[i*2][j*2], biomeData);
					}
					
				}
			}
		} else {
			if (!Debug.structureMode) {
				for(int j = 0; j < VERTEX_COUNT; j++) {
					for(int i = 0; i < VERTEX_COUNT; i++) {
						BiomeData biomeCellData = biomeVoronoi.getDataAt((x+i)*POLYGON_SIZE, (z+j)*POLYGON_SIZE);
						if (j != 0 && i != 0) {
							addTile((x+i-1)*POLYGON_SIZE, (z+j-1)*POLYGON_SIZE,
									heights[i*2-2][j*2-2], heights[i*2][j*2-2],
									heights[i*2-2][j*2], heights[i*2][j*2], biomeCellData);
						}
						
					}
				}
			} else {
				for(int j = 0; j < VERTEX_COUNT; j++) {
					for(int i = 0; i < VERTEX_COUNT; i++) {
						BiomeData biomeCellData = BiomeVoronoi.DEFAULT_DATA;
						if (j != 0 && i != 0) {
							addTile((x+i-1)*POLYGON_SIZE, (z+j-1)*POLYGON_SIZE,
									heights[i*2-2][j*2-2], heights[i*2][j*2-2],
									heights[i*2-2][j*2], heights[i*2][j*2], biomeCellData);
						}
						
					}
				}
			}
		}
		
		return new Vbo[][] {groundBuilder.asVbos(), wallBuilder.asVbos()};
	}

	private static void addTile(float x, float z, float topLeft, float topRight, float btmLeft, float btmRight, BiomeData biomeData) {
		groundBuilder.addVertex(x + POLYGON_SIZE, 	topRight, 	z);
		groundBuilder.addVertex(x, 					topLeft, 	z);
		groundBuilder.addVertex(x, 					btmLeft, 	z + POLYGON_SIZE);
		groundBuilder.addVertex(x + POLYGON_SIZE, 	btmRight, 	z + POLYGON_SIZE);
		
		Biome mainBiome = biomeData.getMainBiome();
		Biome secondaryBiome = biomeData.getSecondaryBiome();
		float influence = biomeData.getTerrainFactor();
		
		if ((mainBiome.groundTx != 0f || mainBiome.groundTy != 0f) && (influence != 0f || (secondaryBiome == mainBiome
				|| (secondaryBiome.groundTx == mainBiome.groundTx && secondaryBiome.groundTy == mainBiome.groundTy))
				)) {

			float dx = mainBiome.groundTx;
			float dy = mainBiome.groundTy;

			groundBuilder.addUv(dx + TERRAIN_ATLAS_SIZE, dy);
			groundBuilder.addUv(dx, dy);
			groundBuilder.addUv(dx, dy + TERRAIN_ATLAS_SIZE);
			groundBuilder.addUv(dx + TERRAIN_ATLAS_SIZE, dy + TERRAIN_ATLAS_SIZE);
		} else{
			groundBuilder.addUv(TERRAIN_ATLAS_SIZE, 0);
			groundBuilder.addUv(0, 0);
			groundBuilder.addUv(0, TERRAIN_ATLAS_SIZE);
			groundBuilder.addUv(TERRAIN_ATLAS_SIZE, TERRAIN_ATLAS_SIZE);
		}
		
		Vector3f normal = Vector3f.cross(new Vector3f(POLYGON_SIZE, topRight-topLeft, 0),
				new Vector3f(0, btmLeft-topLeft, POLYGON_SIZE));
		normal.normalize().negate();
		
		groundBuilder.addNormal(normal);
		groundBuilder.addNormal(normal);
		groundBuilder.addNormal(normal);
		groundBuilder.addNormal(normal);
	
		Vector3f color = biomeData.getColor();
		
		groundBuilder.addColor(color);
		groundBuilder.addColor(color);
		groundBuilder.addColor(color);
		groundBuilder.addColor(color);
		
		groundBuilder.addRelativeIndices(4, 0, 1, 3, 3, 1, 2);
		groundIndices += 6;
	}

	public Vbo[][] finish() {
		return new Vbo[][] {groundBuilder.asVbos(), wallBuilder.asVbos()};
	}

	public static Model[] buildMeshes(Chunk chunk, BiomeVoronoi voronoi) {
		Vbo[][] vbos = buildChunkMesh(chunk, voronoi);
		
		// TODO: Just make buildChunkMesh go straight to Model()?
		Model ground = Model.create();
		ground.bind();
		ground.createAttribute(0, vbos[0][0], 3);
		ground.createAttribute(1, vbos[0][1], 2);
		ground.createAttribute(2, vbos[0][2], 3);
		ground.createAttribute(3, vbos[0][3], 4);
		ground.setIndexBuffer(vbos[0][4], groundIndices);
		ground.unbind();
		
		Model wall = Model.create();
		wall.bind();
		wall.createAttribute(0, vbos[1][0], 3);
		wall.createAttribute(1, vbos[1][1], 2);
		wall.createAttribute(2, vbos[1][2], 3);
		wall.createAttribute(3, vbos[1][3], 4);
		wall.setIndexBuffer(vbos[1][4], wallIndices);
		wall.unbind();
		
		return new Model[] {ground, wall};
	}
	
	public static void addWall(ModelBuilderOld builder, Vector3f p1, Vector3f p2, Vector3f p3,
			Vector3f p4, int bx, int bz,Chunk chunk) {
		
		Vector3f normal = Vector3f.cross(Vector3f.sub(p3, p1), Vector3f.sub(p2, p1));
		normal.normalize().negate();
		byte facing = Tile.getFacingByte(normal);
		
		Vector3f v1 = new Vector3f(), v2 = new Vector3f(), 
				v3 = new Vector3f(), v4 = new Vector3f();
		
		if (p1.y < p3.y) {
			Vector3f hold = new Vector3f(p1);
			p1.set(p3);
			p3.set(hold);
			hold = new Vector3f(p2);
			p2.set(p4);
			p4.set(hold);
		}
		
		BuildData b = chunk.getBuilding();
		//final int cx = chunk.realX;
		//final int cz = chunk.realZ;
		
		Tile tile;
		
		float y1 = p1.y;
		float y2 = p2.y;
		
		int dy = Math.round(y1 - Tile.TILE_SIZE);
		
		v1.set(p1.x, y1, p1.z);
		v2.set(p2.x, y2, p2.z);
		v3.set(p3.x, dy, p3.z);
		v4.set(p4.x, dy, p4.z);
		
		tile = b.getTileAt(bx, dy, bz);
		
		if (tile == null || (facing & tile.getWalls()) == 0) {
			addTerrainWall(builder, v1, v2, v3, v4, normal);
			builder.addTextureCoord(0, (1f - (((p1.y % 1) + 1) % 1)) * TERRAIN_ATLAS_SIZE);
			builder.addTextureCoord(TERRAIN_ATLAS_SIZE, (1f - (((p2.y % 1) + 1) % 1)) * TERRAIN_ATLAS_SIZE);
			builder.addTextureCoord(TERRAIN_ATLAS_SIZE, TERRAIN_ATLAS_SIZE);
			builder.addTextureCoord(0, TERRAIN_ATLAS_SIZE);
		}
		
		y1 = dy;
		y2 = dy;
		
		for(; y1 > p3.y; y1 -= Tile.TILE_SIZE) {
			v1.set(p1.x, y1, p1.z);
			v2.set(p2.x, y2, p2.z);
			v3.set(p3.x, y2 - Tile.TILE_SIZE, p3.z);
			v4.set(p4.x, y1 - Tile.TILE_SIZE, p4.z);

			addTerrainWall(builder, v1, v2, v3, v4, normal);
			builder.addTextureCoord(0,0);
			builder.addTextureCoord(TERRAIN_ATLAS_SIZE,0);
			builder.addTextureCoord(TERRAIN_ATLAS_SIZE,TERRAIN_ATLAS_SIZE);
			builder.addTextureCoord(0,TERRAIN_ATLAS_SIZE);
			
			y2 -= Tile.TILE_SIZE;
		}
	}

	private static void addTerrainWall(ModelBuilderOld builder, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector3f normal) {
		builder.addVertex(v1);
		builder.addVertex(v2);
		builder.addVertex(v3);
		builder.addVertex(v4);
		
		builder.addNormal(normal);
		builder.addNormal(normal);
		builder.addNormal(normal);
		builder.addNormal(normal);
		
		float colFactor1 = 1f + Math.min(0, v1.y)/24f;
		float colFactor2 = 1f + Math.min(0, v2.y)/24f;
		Vector3f dirtColor = BiomeColors.DIRT_COLOR;
		builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
		builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
		builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
		builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
		
		builder.addRelativeIndices(4, 0, 1, 3, 3, 1, 2);
		wallIndices += 6;
	}
}
