package be.humphreys.simplevoronoi;

import org.joml.Vector2f;

public class IntersectionGraphEdge {
	public Vector2f pos;
	public GraphEdge edge;

	public IntersectionGraphEdge(float x, float y, GraphEdge edge) {
		this.pos = new Vector2f(x, y);
		this.edge = edge;
	}
}
