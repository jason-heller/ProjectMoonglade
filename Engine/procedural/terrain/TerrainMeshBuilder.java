package procedural.terrain;

import static map.Chunk.POLYGON_SIZE;
import static map.Chunk.VERTEX_COUNT;

import org.joml.Vector3f;

import core.res.Model;
import core.res.Vbo;
import map.Chunk;
import map.building.Building;
import map.building.Tile;
import procedural.biome.BiomeData;
import procedural.biome.BiomeVoronoi;
import procedural.biome.types.BiomeColors;
import util.ModelBuilder;

public class TerrainMeshBuilder {
	
	private static ModelBuilder groundBuilder;
	private static ModelBuilder wallBuilder;
	
	private static Vbo[][] buildChunkMesh(Chunk chunk, BiomeVoronoi biomeVoronoi) {
		final int x = chunk.x * (VERTEX_COUNT-1);
		final int z = chunk.z * (VERTEX_COUNT-1);
		final float[][] heights = chunk.heightmap;
		
		groundBuilder = new ModelBuilder();
		wallBuilder = new ModelBuilder();
		
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
		
		return new Vbo[][] {groundBuilder.asVbos(), wallBuilder.asVbos()};
	}

	private static void addTile(int x, int z, float topLeft, float topRight, float btmLeft, float btmRight, BiomeData biomeData) {
		groundBuilder.addVertex(x + POLYGON_SIZE, 	topRight, 	z);
		groundBuilder.addVertex(x, 					topLeft, 	z);
		groundBuilder.addVertex(x, 					btmLeft, 	z + POLYGON_SIZE);
		groundBuilder.addVertex(x + POLYGON_SIZE, 	btmRight, 	z + POLYGON_SIZE);
		
		groundBuilder.addTextureCoord(1, 0);
		groundBuilder.addTextureCoord(0, 0);
		groundBuilder.addTextureCoord(0, 1);
		groundBuilder.addTextureCoord(1, 1);
		
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
		ground.setIndexBuffer(vbos[0][4], (Chunk.VERTEX_COUNT*Chunk.VERTEX_COUNT*6));
		ground.unbind();
		
		Model wall = Model.create();
		wall.bind();
		wall.createAttribute(0, vbos[1][0], 3);
		wall.createAttribute(1, vbos[1][1], 2);
		wall.createAttribute(2, vbos[1][2], 3);
		wall.createAttribute(3, vbos[1][3], 4);
		wall.setIndexBuffer(vbos[1][4], (Chunk.VERTEX_COUNT*Chunk.VERTEX_COUNT*6));
		wall.unbind();
		
		return new Model[] {ground, wall};
	}
	
	public static void addWall(ModelBuilder builder, Vector3f p1, Vector3f p2, Vector3f p3,
			Vector3f p4, Chunk chunk) {
		
		Vector3f normal = Vector3f.cross(Vector3f.sub(p3, p1), Vector3f.sub(p2, p1));
		normal.normalize().negate();
		
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
		
		Building b = chunk.getBuilding();
		int cx = chunk.x*Chunk.CHUNK_SIZE;
		int cz = chunk.z*Chunk.CHUNK_SIZE;
		
		float y1 = p1.y;
		float y2 = p2.y;
		for(; y1 > p3.y + Chunk.POLYGON_SIZE; y1 -= Chunk.POLYGON_SIZE) {
			Tile t = b.getTileAt(p1.x-cx, y2, p1.z-cz);
			
			if (t == null || !t.isActive()) {
				v1.set(p1.x, y1, p1.z);
				v2.set(p2.x, y2, p2.z);
				v3.set(p3.x, y2 - Chunk.POLYGON_SIZE, p3.z);
				v4.set(p4.x, y1 - Chunk.POLYGON_SIZE, p4.z);
				
				builder.addVertex(v1);
				builder.addVertex(v2);
				builder.addVertex(v3);
				builder.addVertex(v4);
				
				builder.addTextureCoord(1,0);
				builder.addTextureCoord(0,0);
				builder.addTextureCoord(0,1);
				builder.addTextureCoord(1,1);
				
				builder.addNormal(normal);
				builder.addNormal(normal);
				builder.addNormal(normal);
				builder.addNormal(normal);
				
				float colFactor1 = 1;//p1.y - y1;
				float colFactor2 = 1;//p2.y - y2;
				Vector3f dirtColor = BiomeColors.DIRT_COLOR;
				builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
				builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
				builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
				builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
				
				builder.addRelativeIndices(4, 0, 1, 3, 3, 1, 2);
			}
			y2 -= Chunk.POLYGON_SIZE;
		}
		
		Tile t = b.getTileAt(p3.x-cx, y1, p3.z-cz);
		
		if (t == null || !t.isActive()) {
			v3.set(p3.x, y2, p3.z);
			v4.set(p4.x, y1, p4.z);
			
			builder.addVertex(v4);
			builder.addVertex(v3);
			builder.addVertex(p3);
			builder.addVertex(p4);
			
			builder.addTextureCoord(1,0);
			builder.addTextureCoord(0,0);
			builder.addTextureCoord(0,(v3.y-p3.y)/Chunk.POLYGON_SIZE);
			builder.addTextureCoord(1,(v4.y-p4.y)/Chunk.POLYGON_SIZE);
			
			builder.addNormal(normal);
			builder.addNormal(normal);
			builder.addNormal(normal);
			builder.addNormal(normal);
			
			
			float colFactor1 = 1;//(v4.y - y1)/256;
			float colFactor2 = 1;//(v3.y - y2)/256;
			Vector3f dirtColor = BiomeColors.DIRT_COLOR;
			builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
			builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
			builder.addColor(dirtColor.x*colFactor1, dirtColor.y*colFactor1, dirtColor.z*colFactor1);
			builder.addColor(dirtColor.x*colFactor2, dirtColor.y*colFactor2, dirtColor.z*colFactor2);
			
			builder.addRelativeIndices(4, 0, 1, 3, 3, 1, 2);
		}
	}
}
