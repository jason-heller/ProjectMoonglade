package map;

import org.joml.Vector3f;

public class TerrainIntersection {
	private Vector3f point;
	private int tile;
	private Chunk chunk;
	
	public TerrainIntersection(Vector3f point, int tile, Chunk chunk) {
		this.point = point;
		this.tile = tile;
		this.chunk = chunk;
	}
	
	public Vector3f getPoint() {
		return point;
	}
	
	public int getTile() {
		return tile;
	}
	
	public Chunk getChunk() {
		return chunk;
	}
}
