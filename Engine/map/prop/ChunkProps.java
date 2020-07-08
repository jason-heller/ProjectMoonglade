package map.prop;

import java.util.Random;

import org.joml.Vector3f;

import geom.AABB;
import gl.res.Model;
import gl.res.PropModel;
import map.Chunk;
import map.Terrain;
import map.TerrainIntersection;
import util.ModelBuilderOld;

public class ChunkProps {
	private Props[][] propMap;
	private StaticPropProperties[][] properties;
	
	private Chunk chunk;
	private Model model;
	private int x, z;
	
	public ChunkProps(int x, int z, Chunk chunk) {
		this.chunk = chunk;
		this.x = x;
		this.z = z;
		final int size = Chunk.VERTEX_COUNT-1;
		propMap = new Props[size][size];
		properties = new StaticPropProperties[size][size];	
	}
	
	public void buildModel() {
		if (this.model != null) {
			this.model.cleanUp();
		}
		ModelBuilderOld builder = new ModelBuilderOld();
		
		Random r = new Random();
		r.setSeed(chunk.getSeed());
		
		float rx = x*Chunk.CHUNK_SIZE + .5f;
		float ry;
		float rz = z*Chunk.CHUNK_SIZE + .5f;
		
		for(int i = 0; i < propMap.length; i++) {
			for(int j = 0; j < propMap.length; j++) {
				Props tile = propMap[i][j];
				
				ry = (chunk.heightmap[i * 2 + 0][j * 2 + 0] + chunk.heightmap[i * 2 + 1][j * 2 + 1]) / 2f;
				
				if (tile != null) {
					float dx, dz, scale;
					
					PropModel tiledModel = Props.getModel(tile);
					if (properties[i][j] != null) {
						dx = properties[i][j].dx;
						dz = properties[i][j].dz;
						scale = properties[i][j].scale;
					} else {
						final float posVar =  tiledModel.getPositionVariance();
						final float scaleVar = tiledModel.getScaleVariance();
						dx = -posVar + (r.nextFloat()*(posVar*2));
						dz = -posVar + (r.nextFloat()*(posVar*2));
						scale = (1 - scaleVar) + (r.nextFloat()*(scaleVar*2));
						
						properties[i][j] = new StaticPropProperties(dx, dz, scale);
						properties[i][j].damage = Props.get(tile).getStrength();
					}
				
					//if (tiledModel.getNumSubmodels() == 1) {
						builder.addPropMdesh(rx+i, ry, rz+j, scale, tiledModel);
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
		//tilemap = null;
		//tileProperties = null;
		chunk = null;
		model = null;
	}
	
	public StaticPropProperties getEntityProperties(int x, int z) {
		return properties[x][z];
	}

	public Props getProp(int x, int z) {
		return propMap[x][z];
	}
	
	public void removeEntity(int x, int z) {
		propMap[x][z] = null;
		properties[x][z] = null;
	}

	public Props[][] getPropMap() {
		return propMap; 
	}
	
	public TerrainIntersection testCollision(Vector3f origin, Vector3f dir, float tileX, float tileY, float tileZ) {
		final int tileArrX = (int) (tileX - chunk.realX);
		final int tileArrZ = (int) (tileZ - chunk.realZ);

		final Props tileId = propMap[tileArrX][tileArrZ];
		
		if (tileId == null) return null;
		
		final StaticPropProperties tileProps = properties[tileArrX][tileArrZ];
		final float x = tileProps.dx + (tileX + .5f);
		final float z = tileProps.dz + (tileZ + .5f);
		
		final StaticProp tile = Props.get(tileId);
		Vector3f bounds = tile.getBounds();

		final float y = tileY + (tileProps.scale * bounds.y) / 2f;

		final AABB aabb = new AABB(x, y, z, bounds.x * tileProps.scale, bounds.y * tileProps.scale, bounds.z * tileProps.scale);

		final float hitDistance = aabb.collide(origin, dir);
		if (!Float.isNaN(hitDistance)) {
			return new TerrainIntersection(Vector3f.add(origin, Vector3f.mul(dir, hitDistance)), tileId, chunk);
		}

		return null;
	}

	public StaticPropProperties[][] getEntityPropertyMap() {
		return properties;
	}
}
