package map.tile;

import core.res.Model;
import core.res.TileableModel;
import map.Chunk;
import map.Terrain;
import util.ModelBuilder;

public class ChunkTiles {
	public int[][] tilemap;
	
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
		
		float rx = x*Chunk.CHUNK_SIZE + .5f;
		float ry;
		float rz = z*Chunk.CHUNK_SIZE + .5f;
		
		for(int i = 0; i < tilemap.length; i++) {
			for(int j = 0; j < tilemap.length; j++) {
				int tile = tilemap[i][j];
				
				ry = (chunk.heightmap[i * 2 + 0][j * 2 + 0] + chunk.heightmap[i * 2 + 1][j * 2 + 1]) / 2f;
				
				if (tile != 0) {
					TileableModel tiledModel = terrain.getTileItem(tile);
				
					//if (tiledModel.getNumSubmodels() == 1) {
						builder.addTileableModel(rx+i, ry, rz+j, tiledModel);
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
	}
}
