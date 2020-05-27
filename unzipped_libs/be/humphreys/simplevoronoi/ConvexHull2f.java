package be.humphreys.simplevoronoi;

public class ConvexHull2f {

	private static float distToLine(float px, float py, float x1, float y1, float x2, float y2) {
		final float A = px - x1; // position of point rel one end of line
		final float B = py - y1;
		final float C = x2 - x1; // vector along line
		final float D = y2 - y1;
		final float E = -D; // orthogonal vector
		final float F = C;

		final float dot = A * E + B * F;
		final float len_sq = E * E + F * F;

		return (float) Math.sqrt(dot * dot / len_sq);
	}

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

	private static boolean pointInsideLine(float px, float py, float startx, float starty, float endx, float endy) {
		final float vx = startx - px;
		final float vy = starty - py;
		final float ex = startx - endx;
		final float ey = starty - endy;

		return ex * vy - ey * vx >= 0f;
	}

	private float[] x;

	private float[] y;

	public ConvexHull2f() {
		x = new float[0];
		y = new float[0];
	}

	public void addVertex(float x1, float y1) {
		final int index = x.length;

		final float[] nx = new float[index + 1];
		final float[] ny = new float[index + 1];

		System.arraycopy(x, 0, nx, 0, index);
		System.arraycopy(y, 0, ny, 0, index);

		x = nx;
		y = ny;

		nx[index] = x1;
		ny[index] = y1;
	}

	public boolean closestDist(float px, float py) {
		final boolean lastResult = pointInsideLine(px, py, x[0], y[0], x[1], y[1]);

		final int len = x.length - 1;
		for (int i = 1; i < len; i++) {
			if (lastResult != pointInsideLine(px, py, x[i], y[i], x[i + 1], y[i + 1])) {
				return false;
			}
		}

		return pointInsideLine(px, py, x[len], y[len], x[0], y[0]);
	}

	public boolean containsPoint(float px, float py) {
		final boolean lastResult = pointInsideLine(px, py, x[0], y[0], x[1], y[1]);

		final int len = x.length - 1;
		for (int i = 1; i < len; i++) {
			if (lastResult != pointInsideLine(px, py, x[i], y[i], x[i + 1], y[i + 1])) {
				return false;
			}
		}

		return pointInsideLine(px, py, x[len], y[len], x[0], y[0]);
	}

	public float distFromEdge(float px, float py) {
		float lastResult = distToLine(px, py, x[0], y[0], x[1], y[1]);

		final int len = x.length - 1;
		for (int i = 1; i < len; i++) {
			final float newResult = distToLine(px, py, x[i], y[i], x[i + 1], y[i + 1]);
			if (lastResult > newResult) {
				lastResult = newResult;
			}
		}

		final float newResult = distToLine(px, py, x[len], y[len], x[0], y[0]);
		if (lastResult > newResult) {
			lastResult = newResult;
		}

		return lastResult;
	}

	public boolean intersectsBox(float bx, float by, float bs) {
		final boolean lastResult = lineInRect(bx, by, bs, x[0], y[0], x[1], y[1]);

		final int len = x.length - 1;
		for (int i = 1; i < len; i++) {
			if (lastResult != lineInRect(bx, by, bs, x[i], y[i], x[i + 1], y[i + 1])) {
				return false;
			}
		}

		return lineInRect(bx, by, bs, x[len], y[len], x[0], y[0]);
	}
}
