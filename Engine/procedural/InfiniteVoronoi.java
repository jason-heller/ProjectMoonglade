package procedural;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import be.humphreys.simplevoronoi.Cell;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.GraphSegment;
import be.humphreys.simplevoronoi.IntersectionGraphEdge;
import be.humphreys.simplevoronoi.Site;
import be.humphreys.simplevoronoi.Voronoi;

@Deprecated
public class InfiniteVoronoi {
	public static final float NO_SITE = 0.4230428f;

	public static final float INTERSECTION_SIZE = 256f;
	private static final float OVERLAP = 270f;

	public static int minX;

	public static int minY;

	public static int scaledSize;
	public static final byte PATH_RAILROAD = 0;
	public static final byte PATH_INTERSTATE = 1;

	private final Voronoi voronoi;
	private final int scale;
	private List<GraphEdge> edges;
	private final float[] sitePosX, sitePosY;
	private int realSize = 0;

	private final long seed;

	private int relX, relY;

	public InfiniteVoronoi(int x, int y, int size, int scale, String seed) {
		this.scale = scale;
		this.seed = 1l + seed.hashCode();
		scaledSize = size * scale;
		realSize = size;
		voronoi = new Voronoi(12f);
		
		final int total = size * size;
		sitePosX = new float[total];
		sitePosY = new float[total];

		rebuild(x, y);
	}

	public Cell[] getCells() {
		return voronoi.getCells();
	}

	public List<GraphEdge> getEdges() {
		return this.edges;
	}

	public List<GraphSegment> getEdgesInRegion(int left, int top, int size, int offset, int width,
			List<Vector2f[]> infra) {
		final List<GraphSegment> culledEdges = new ArrayList<GraphSegment>();
		for (final GraphEdge edge : edges) {
			List<Vector2f> col = edge.intersection(left, top, size);
			if (col.size() == 2) {
				culledEdges.add(new GraphSegment(col, edge));
			}
			// else {
			col.clear();
			col = edge.intersection(left - offset, top - offset, size + offset * 2);
			// }

			if (col.size() >= 2) {
				final Vector2f p1 = col.get(0);
				final Vector2f p2 = col.get(1);
				float dx = p1.y - p2.y;
				float dy = p2.x - p1.x;

				final double l = Math.sqrt(dx * dx + dy * dy);
				dx /= l;
				dy /= l;
				dx *= width;
				dy *= width;
				float ddx = p2.x - p1.x;
				float ddy = p2.y - p1.y;
				// l = Math.sqrt(ddx*ddx + ddy*ddy);
				ddx /= l;
				ddy /= l;
				final float is = InfiniteVoronoi.INTERSECTION_SIZE + OVERLAP;
				ddx *= is;
				ddy *= is;
				p1.x -= ddx;
				p1.y -= ddy;
				p2.x += ddx;
				p2.y += ddy;

				final Vector2f[] out = new Vector2f[] { new Vector2f(p1.x + dx, p1.y + dy),
						new Vector2f(p1.x - dx, p1.y - dy), new Vector2f(p2.x - dx, p2.y - dy),
						new Vector2f(p2.x + dx, p2.y + dy) };
				infra.add(out);
			}
		}

		return culledEdges;
	}

	public List<IntersectionGraphEdge> getIntersectionPts(int x, int z) {
		return voronoi.getIntersectionPts(x, z);
	}

	public Site[] getSites() {
		return voronoi.getSites();
	}

	private void rebuild(int x, int y) {
		relX = x;
		relY = y;
		minX = relX * scale - (int) (scaledSize / 2.5f);
		minY = relY * scale - (int) (scaledSize / 2.5f);

		// edges.clear();
		edges = voronoi.generateVoronoi(sitePosX, sitePosY, realSize, minX, minX + scaledSize, minY, minY + scaledSize);
	}

	public void update(float x, float y) {
		final int nx = (int) Math.floor(x / scale);
		final int ny = (int) Math.floor(y / scale);

		if (relX != nx || relY != ny) {
			/*
			 * List<GraphEdge> crossings = new ArrayList<GraphEdge>(); for(GraphEdge e :
			 * edges) { if (e.crossings != null) { crossings.add(e); } }
			 */
			rebuild(nx, ny);
			/*
			 * for(GraphEdge e : crossings) { if (!edges.contains(e)) { for(GraphEdge e2 :
			 * edges) { if (e.equals(e2)) { if (e2.crossings == null) e2.crossings = new
			 * ArrayList<Crossing>(); e2.crossings.addAll(e.crossings); break; } } } }
			 */
			System.gc();
		}
	}
}