package be.humphreys.simplevoronoi;

import java.util.ArrayList;
import java.util.List;

import procedural.NoiseUtil;

public class Cell {

	private static final long BIOME_SEED = 88888;
	public static final int CITY = 0;
	public static final int LAKE = 1;

	public static final int SHRUB = 2;

	public static final int MOUNTAIN = 3;
	public static final int SUBURB = 4;

	private static boolean lineInRect(float rx, float ry, float rs, float p1x, float p1y, float p2x, float p2y) {
		final float rxw = rx + rs;
		final float ryh = ry + rs;

		if (p1x > rx && p1x > rx && p1x > rxw && p1x > rxw && p2x > rx && p2x > rx && p2x > rxw && p2x > rxw) {
			return false;
		}
		if (p1x < rx && p1x < rx && p1x < rxw && p1x < rxw && p2x < rx && p2x < rx && p2x < rxw && p2x < rxw) {
			return false;
		}
		if (p1y > ry && p1y > ryh && p1y > ryh && p1y > ry && p2y > ry && p2y > ryh && p2y > ryh && p2y > ry) {
			return false;
		}
		if (p1y < ry && p1y < ryh && p1y < ryh && p1y < ry && p2y < ry && p2y < ryh && p2y < ryh && p2y < ry) {
			return false;
		}

		final float f1 = (p2y - p1y) * rx + (p1x - p2x) * ry + (p2x * p1y - p1x * p2y);
		final float f2 = (p2y - p1y) * rx + (p1x - p2x) * ryh + (p2x * p1y - p1x * p2y);
		final float f3 = (p2y - p1y) * rxw + (p1x - p2x) * ryh + (p2x * p1y - p1x * p2y);
		final float f4 = (p2y - p1y) * rxw + (p1x - p2x) * ry + (p2x * p1y - p1x * p2y);

		if (f1 < 0 && f2 < 0 && f3 < 0 && f4 < 0) {
			return false;
		}
		if (f1 > 0 && f2 > 0 && f3 > 0 && f4 > 0) {
			return false;
		}

		return true;
	}

	public List<GraphEdge> edges = new ArrayList<GraphEdge>();
	public Point center;

	public int type = 1;

	public boolean contains(float x, float z) {
		for (final GraphEdge edge : edges) {
			if (!onSameSide(x, z, edge)) {
				return false;
			}
		}

		return true;
	}

	public boolean intersectsBox(float bx, float by, float bs) {
		if (contains(bx + 1, by + 1)) {
			return true;
		}
		final float l = bs - 1;
		if (contains(bx + l, by + l)) {
			return true;
		}
		if (contains(bx + 1, by + l)) {
			return true;
		}
		if (contains(bx + l, by + 1)) {
			return true;
		}

		final boolean lastResult = lineInRect(bx, by, bs, edges.get(0).x1, edges.get(0).y1, edges.get(0).x2,
				edges.get(0).y2);

		final int len = edges.size() - 1;
		for (int i = 1; i < len; i++) {
			if (lastResult != lineInRect(bx, by, bs, edges.get(i).x1, edges.get(i).y1, edges.get(i + 1).x2,
					edges.get(i + 1).y2)) {
				return false;
			}
		}

		return lineInRect(bx, by, bs, edges.get(len).x1, edges.get(len).y1, edges.get(0).x2, edges.get(0).y2);
	}

	private boolean onSameSide(float x, float y, GraphEdge edge) {
		final float dx = edge.x2 - edge.x1;
		final float dy = edge.y1 - edge.y2;

		if ((dy * (center.x - edge.x1) + dx * (center.y - edge.y1)) * (dy * (x - edge.x1) + dx * (y - edge.y1)) < 0) {
			return false;
		}

		return true;
	}

	// TODO: This is bad
	public void setType() {
		type = (int) (NoiseUtil.valueNoise2d((long) center.x, (long) center.y, BIOME_SEED) * 4f);
	}
}
