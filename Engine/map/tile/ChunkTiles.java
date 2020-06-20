package map.tile;

import java.util.Random;

import org.joml.Vector3f;

import geom.AABB;
import gl.res.Model;
import gl.res.TileableModel;
import map.Chunk;
import map.Terrain;
import map.TerrainIntersection;
import util.ModelBuilder;

public class ChunkTiles {
	private int[][] tilemap;
	private TileProperties[][] tileProperties;
	
	private Chunk chunk;
	private Model model;
	private int x, z;
	
	public ChunkTiles(int x, int z, Chunk chunk, int[][] tilemap) {
		this.chunk = chunk;
		this.tilemap = tilemap;
		this.x = x;
		this.z = z;
	}
	
	public void buildModel() {
		if (this.model != null) {
			this.model.cleanUp();
		}
		ModelBuilder builder = new ModelBuilder();
		Terrain terrain = chunk.getTerrain();
		
		Random r = new Random();
		r.setSeed(chunk.getSeed());
		
		float rx = x*Chunk.CHUNK_SIZE + .5f;
		float ry;
		float rz = z*Chunk.CHUNK_SIZE + .5f;
		
		for(int i = 0; i < tilemap.length; i++) {
			for(int j = 0; j < tilemap.length; j++) {
				int tile = tilemap[i][j];
				
				ry = (chunk.heightmap[i * 2 + 0][j * 2 + 0] + chunk.heightmap[i * 2 + 1][j * 2 + 1]) / 2f;
				
				if (tile != 0) {
					float dx, dz, scale;
					
					TileableModel tiledModel = terrain.getTileItem(tile);
					if (tileProperties[i][j] != null) {
						dx = tileProperties[i][j].dx;
						dz = tileProperties[i][j].dz;
						scale = tileProperties[i][j].scale;
					} else {
						final float posVar =  tiledModel.getPositionVariance();
						final float scaleVar = tiledModel.getScaleVariance();
						dx = -posVar + (r.nextFloat()*(posVar*2));
						dz = -posVar + (r.nextFloat()*(posVar*2));
						scale = (1 - scaleVar) + (r.nextFloat()*(scaleVar*2));
						
						tileProperties[i][j] = new TileProperties(dx, dz, scale);
						tileProperties[i][j].damage = terrain.getTileById(tile).getStrength();
					}
				
					//if (tiledModel.getNumSubmodels() == 1) {
						builder.addTileableModel(rx+i, ry, rz+j, scale, tiledModel);
					/*} else {
						byte flags = 0;
						if (i > 0 && tilemap[i-1][j] != tile)
							flags += 1;
						if (i < tilemap.length-1 && tilemap[i+1][j] != tile)
							flags += 2;
						if (j > 0 && tilemap[i][j-1] != tile)
							flags += 4;
						if (j < tilemap.length-1 && tilemap[i][j+1] != tile)
							flags += 8;
							
						builder.addTileableModel(rx+i, ry, rz+j, tiledModel, flags);
					}*/
				}
			}
		}

		this.model = builder.finish();
	}
	
	public Model getModel() {
		return model;
	}

	public void cleanUp() {
		if (model != null) {
			model.cleanUp();
		}
		tilemap = null;
		tileProperties = null;
		chunk = null;
		model = null;
	}
	
	public TileProperties getTileProperties(int x, int z) {
		return tileProperties[x][z];
	}

	public int getTileId(int x, int z) {
		return tilemap[x][z];
	}
	
	public void removeTile(int x, int z) {
		tilemap[x][z] = 0;
		tileProperties[x][z] = null;
	}

	public int[][] getTilemap() {
		return tilemap; 
	}

	public void initEmpty() {
		final int size = Chunk.VERTEX_COUNT-1;
		tilemap = new int[size][size];
		tileProperties = new TileProperties[size][size];	
	}

	public TerrainIntersection testCollision(Vector3f origin, Vector3f dir, float tileX, float tileY, float tileZ) {
		final int tileArrX = (int) (tileX - chunk.realX);
		final int tileArrZ = (int) (tileZ - chunk.realZ);

		final int tileId = tilemap[tileArrX][tileArrZ];
		
		if (tileId == 0) return null;
		
		final TileProperties tileProps = tileProperties[tileArrX][tileArrZ];
		final float x = tileProps.dx + (tileX + .5f);
		final float z = tileProps.dz + (tileZ + .5f);
		
		final EnvTile tile = chunk.getTerrain().getTileById(tileId);
		Vector3f bounds = tile.getBounds();

		final float y = tileY + (tileProps.scale * bounds.y) / 2f;

		final AABB aabb = new AABB(x, y, z, bounds.x * tileProps.scale, bounds.y * tileProps.scale, bounds.z * tileProps.scale);

		final float hitDistance = aabb.collide(origin, dir);
		if (!Float.isNaN(hitDistance)) {
			return new TerrainIntersection(Vector3f.add(origin, Vector3f.mul(dir, hitDistance)), tileId, chunk);
		}

		return null;
	}

	public TileProperties[][] getTilePropertyMap() {
		return tileProperties;
	}
}
