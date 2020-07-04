package map;

import map.tile.BuildingTile;

public class ChunkDataWrapper {
	public int x, z, arrX, arrZ;
	public float[][] heightmap;
	public int[][] tilemap;
	//public float[][] waterTable;
	public BuildingTile[][][] tiles;
	public byte editFlags = 1;
	
	public byte state = 0;
	
	public ChunkDataWrapper(Chunk chunk) {
		this.x = chunk.dataX;
		this.z = chunk.dataZ;
		this.arrX = chunk.arrX;
		this.arrZ = chunk.arrZ;
		
		this.heightmap = chunk.heightmap.clone();
		this.tilemap = chunk.getChunkEntities().getTilemap().clone();
		this.tiles = chunk.getBuilding().getTilemap().clone();
	}

	public void setState(byte state) {
		this.state = state;
	}
}
