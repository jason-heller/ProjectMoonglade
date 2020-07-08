package map;

import org.joml.Vector3f;

import map.prop.Props;

public class TerrainIntersection {
	private Vector3f point;
	private Props tile;
	private Chunk chunk;
	
	public TerrainIntersection(Vector3f point, Props tile, Chunk chunk) {
		this.point = point;
		this.tile = tile;
		this.chunk = chunk;
	}
	
	public Vector3f getPoint() {
		return point;
	}
	
	public Props getTile() {
		return tile;
	}
	
	public Chunk getChunk() {
		return chunk;
	}
}
