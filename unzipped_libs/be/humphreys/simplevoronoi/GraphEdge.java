/*
  Copyright 2011 James Humphreys. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY James Humphreys ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors and should not be interpreted as representing official policies, either expressed
or implied, of James Humphreys.
 */

package be.humphreys.simplevoronoi;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

public class GraphEdge {
	public float x1, y1, x2, y2;

	public int site1;
	public int site2;

	/*
	 * private boolean intersects(int rx1, int ry1, int rx2, int ry2) { // Find min
	 * and max X for the segment
	 * 
	 * float minX = x1; float maxX = x2;
	 * 
	 * if (x1 > x2) { minX = x2; maxX = x1; }
	 * 
	 * // Find the intersection of the segment's and rectangle's x-projections
	 * 
	 * if (maxX > rx2) { maxX = rx2; }
	 * 
	 * if (minX < rx1) { minX = rx1; }
	 * 
	 * if (minX > maxX) // If their projections do not intersect return false {
	 * return false; }
	 * 
	 * // Find corresponding min and max Y for min and max X we found before
	 * 
	 * float minY = y1; float maxY = y2;
	 * 
	 * float dx = x2 - x1;
	 * 
	 * if (Math.abs(dx) > 0.0000001) { float a = (y2 - y1) / dx; float b = y1 - a *
	 * x1; minY = a * minX + b; maxY = a * maxX + b; }
	 * 
	 * if (minY > maxY) { float tmp = maxY; maxY = minY; minY = tmp; }
	 * 
	 * // Find the intersection of the segment's and rectangle's y-projections
	 * 
	 * if (maxY > ry2) { maxY = ry2; }
	 * 
	 * if (minY < ry1) { minY = ry1; }
	 * 
	 * if (minY > maxY) // If Y-projections do not intersect return false { return
	 * false; }
	 * 
	 * return true; }
	 */

	public boolean equals(GraphEdge other) {
		if (x2 == other.x2 && y2 == other.y2) {
			return true;
		}

		return false;
		/*
		 * float bx = x2 - x1; float by = y2 - y1; float dx = other.x2 - other.x1; float
		 * dy = other.y2 - other.y1; float b_dot_d_perp = bx * dy - by * dx; if
		 * (b_dot_d_perp == 0) { return false; } float cx = other.x1 - x1; float cy =
		 * other.y1 - y1; float t = (cx * dy - cy * dx) / b_dot_d_perp; if (t < 0 || t >
		 * 1) { return false; } float u = (cx * by - cy * bx) / b_dot_d_perp; if (u < 0
		 * || u > 1) { return false; } return true;
		 */
	}

	public List<Vector2f> intersection(int x, int y, int size) {
		final List<Vector2f> pts = new ArrayList<Vector2f>();
		final int left = x, top = y, right = x + size, bottom = y + size;

		Vector2f l = null;
		l = intersectsLine(left, top, left, bottom);
		if (l != null) {
			pts.add(l);
		}
		l = intersectsLine(left, bottom, right, bottom);
		if (l != null) {
			pts.add(l);
		}

		l = intersectsLine(left, top, right, top);
		if (l != null) {
			pts.add(l);
		}
		l = intersectsLine(right, top, right, bottom);
		if (l != null) {
			pts.add(l);
		}

		if (x1 > left && x1 < right && y1 > top && y1 < bottom) {
			pts.add(new Vector2f(x1, y1));
		}
		if (x2 > left && x2 < right && y2 > top && y2 < bottom) {
			pts.add(new Vector2f(x2, y2));
		}

		return pts;
	}

	public Vector2f intersects(GraphEdge other) {
		return intersectsLine(other.x1, other.y1, other.x2, other.y2);
	}

	public Vector2f intersectsLine(float ox1, float oy1, float ox2, float oy2) {
		Vector2f result = null;

		final float s1_x = x2 - x1, s1_y = y2 - y1,

				s2_x = ox2 - ox1, s2_y = oy2 - oy1,

				s = (-s1_y * (x1 - ox1) + s1_x * (y1 - oy1)) / (-s2_x * s1_y + s1_x * s2_y),
				t = (s2_x * (y1 - oy1) - s2_y * (x1 - ox1)) / (-s2_x * s1_y + s1_x * s2_y);

		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			// Collision detected
			result = new Vector2f((int) (x1 + t * s1_x), (int) (y1 + t * s1_y));
		}

		return result;
	}
}
