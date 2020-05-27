package be.humphreys.simplevoronoi;

import java.util.List;

import org.joml.Vector2f;

public class GraphSegment {
	public Vector2f start, end;
	public GraphEdge parent;

	public GraphSegment(List<Vector2f> pts, GraphEdge parent) {
		start = pts.get(0);
		end = pts.get(1);
		this.parent = parent;
	}

	public GraphSegment(Vector2f start, Vector2f end, GraphEdge parent) {
		this.start = start;
		this.end = end;
		this.parent = parent;
	}
}
