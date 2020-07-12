package map;

import org.joml.Vector3f;

import map.prop.Props;

public class TerrainIntersection {
	private Vector3f point;
	private Props tile;
	private Chunk chunk;
	private int propX, propZ;
	
	public TerrainIntersection(Vector3f point, Props tile, int propX, int propZ, Chunk chunk) {
		this.point = point;
		this.tile = tile;
		this.chunk = chunk;
		this.propX = propX;
		this.propZ = propZ;
	}
	
	public int getPropX() {
		return propX;
	}
	
	public int getPropZ() {
		return propZ;
	}
	
	public Vector3f getPoint() {
		return point;
	}
	
	public Props getProp() {
		return tile;
	}
	
	public Chunk getChunk() {
		return chunk;
	}
}
