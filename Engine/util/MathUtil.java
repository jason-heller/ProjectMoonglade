package util;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class MathUtil {
	public static final double TAU = Math.PI * 2.0;

	public static float angleLerp(float start, float end, float amount) {
		start += 360;
		end += 360;
		start %= 360;
		end %= 360;

		if (end - start < 180) {
			return lerp(start, end, amount);
		} else {
			return lerp(start + 360, end, amount);
		}
	}

	public static float barycentric(float x, float y, Vector3f p1, Vector3f p2, Vector3f p3) {
		final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		final float l1 = ((p2.z - p3.z) * (x - p3.x) + (p3.x - p2.x) * (y - p3.z)) / det;
		final float l2 = ((p3.z - p1.z) * (x - p3.x) + (p1.x - p3.x) * (y - p3.z)) / det;
		final float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public static float clamp(float f, float min, float max) {
		return Math.min(Math.max(f, min), max);
	}

	public static List<float[]> clipTriangle(float[][] clippingPlane, float[][] triangle) {
		final List<float[]> allOutput = new ArrayList<float[]>();
		List<float[]> output;
		final int len = clippingPlane.length;
		for (int i = 0; i < len; i++) {

			final int len2 = triangle.length;
			final float[][] input = triangle;
			output = new ArrayList<>(len2);

			final float[] A = clippingPlane[(i + len - 1) % len];
			final float[] B = clippingPlane[i];

			for (int j = 0; j < len2; j++) {

				final float[] P = input[(j + len2 - 1) % len2];
				final float[] Q = input[j];

				if (isInside(A, B, Q)) {
					if (!isInside(A, B, P)) {
						output.add(intersection(A, B, P, Q));
					}
					output.add(Q);
				} else if (isInside(A, B, P)) {
					output.add(intersection(A, B, P, Q));
				}
			}

			allOutput.addAll(output);
		}

		return allOutput;
	}

	public static Vector3f eulerToVectorDeg(float yaw, float pitch) {
		return eulerToVectorRad((float) Math.toRadians(yaw), (float) Math.toRadians(pitch));
	}

	public static Vector3f eulerToVectorRad(float yaw, float pitch) {
		final float xzLen = (float) Math.cos(pitch);
		return new Vector3f(
				-xzLen * (float) Math.sin(yaw),
				(float) Math.sin(pitch),
				xzLen * (float) Math.cos(yaw));
	}

	public static float fastSqrt(float f) {
		return Float.intBitsToFloat((Float.floatToIntBits(f) - (1 << 52) >> 1) + (1 << 61));
	}

	public static Vector3f getDirection(Matrix4f matrix) {
		final Matrix4f inverse = new Matrix4f();
		matrix.invert(inverse);

		return new Vector3f(inverse.m20, inverse.m21, inverse.m22);
	}

	private static float[] intersection(float[] a, float[] b, float[] p, float[] q) {
		final float A1 = b[1] - a[1];
		final float B1 = a[0] - b[0];
		final float C1 = A1 * a[0] + B1 * a[1];

		final float A2 = q[1] - p[1];
		final float B2 = p[0] - q[0];
		final float C2 = A2 * p[0] + B2 * p[1];

		final float det = A1 * B2 - A2 * B1;
		final float x = (B2 * C1 - B1 * C2) / det;
		final float y = (A1 * C2 - A2 * C1) / det;

		return new float[] { x, y };
	}

	private static boolean isInside(float[] a, float[] b, float[] c) {
		return (a[0] - c[0]) * (b[1] - c[1]) > (a[1] - c[1]) * (b[0] - c[0]);
	}

	public static float lerp(float s, float t, float amount) {
		return s * (1f - amount) + t * amount;
	}

	public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
		final Vector3f forward = new Vector3f(center).sub(eye).normalize();
		final Vector3f side = new Vector3f(forward).cross(up).normalize();
		up = new Vector3f(side).cross(forward);

		final Matrix4f matrix = new Matrix4f();
		matrix.m00 = side.x;
		matrix.m01 = side.y;
		matrix.m02 = side.z;
		matrix.m10 = up.x;
		matrix.m11 = up.y;
		matrix.m12 = up.z;
		matrix.m20 = -forward.x;
		matrix.m21 = -forward.y;
		matrix.m22 = -forward.z;
		return matrix;
	}

	public static float pointDirection(float x1, float y1, float x2, float y2) {
		float dx, dy;
		dy = y2 - y1;
		dx = x2 - x1;

		return (float) -(Math.atan2(dy, dx));
	}

	public static float pointDirection(float x1, float y1, float z1, float x2, float y2, float z2) {
		final float dist = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		return (float) Math.atan2(z2 - z1, dist);
	}

	public static Vector3f reflect(Vector3f vector, Vector3f normal) {
		// 2*(V dot N)*N - V
		final float dp = 2f * Vector3f.dot(vector, normal);
		final Vector3f s = Vector3f.mul(normal, dp);
		return Vector3f.sub(vector, s);
	}

	public static float sCurveLerp(float s, float t, float amount) {
		final float amtSqr = amount * amount;
		return lerp(s, t, -2 * (amtSqr * amount) + 3 * amtSqr);
	}

	public static Vector3f vectorToEuler(Vector3f v) {
		return new Vector3f((float) Math.atan(v.y / v.x), (float) Math.acos(v.z / v.length()), 0f);
	}

	public static List<Vector2f> bresenham(int x1, int y1, int x2, int y2) {
		List<Vector2f> points = new ArrayList<Vector2f>();
		boolean flip = (x1 > x2);
		
		if (flip) {
			int hx = x1;
			int hy = y1;
			
			x1 = x2;
			y1 = y2;
			x2 = hx;
			y2 = hy;
		}
		
		int t = 2 * (y2 - y1);
		int slopeErr = t - (x2 - x1);

		for (int x = x1, y = y1; x <= x2; x++) {
			if (flip)
				points.add(0, new Vector2f(x, y));
			else
				points.add(new Vector2f(x, y));

			slopeErr += t;

			if (slopeErr >= 0) {
				y++;
				slopeErr -= 2 * (x2 - x1);
			}
		}
		return points;
	}
	
	public static List<int[]> bresenham(float x1, float y1, float x2, float y2) {
		List<int[]> points = new ArrayList<int[]>();
		
		int x = (int) Math.floor(x1);
		int y = (int) Math.floor(y1);
		double dx = x2 - x1;
		double dy = y2 - y1;
		int stepX = (int) Math.signum(dx);
		int stepY = (int) Math.signum(dy);
		
		// Straight dist to first vertical boundary
		double xOffset = (x2 > x1) ? Math.ceil(x1) - x1 : x1 - Math.floor(x1);
		// Straight dist to first horiz boundary
		double yOffset = (y2 > y1) ? Math.ceil(y1) - y1 : y1 - Math.floor(y1);
		
		double theta = Math.atan2(-dy, dx);
		// 'time' until boundary reached
		double tx = xOffset / Math.cos(theta);
		double ty = yOffset / Math.sin(theta);
		// delta time for movement
		double deltaX  = 1.0 / Math.cos(theta);
		double deltaY = 1.0 / Math.sin(theta);

		double manhattanDist = Math.abs(Math.floor(x2) - Math.floor(x1)) + Math.abs(Math.floor(y2) - Math.floor(y1));
		
		for (int t = 0; t <= manhattanDist; t++) {
			points.add(new int[] {x, y});
			if (Math.abs(tx) < Math.abs(ty)) {
				tx += deltaX;
				x += stepX;
			} else {
				ty += deltaY;
				y += stepY;
			}
		}
		return points;
	}

	final static Vector3f[] boxNormals = new Vector3f[] {
			new Vector3f(1,0,0),
			new Vector3f(0,1,0),
			new Vector3f(0,0,1),
			new Vector3f(-1,0,0),
			new Vector3f(0,-1,0),
			new Vector3f(0,0,-1)
	};
	
	public static Vector3f rayBoxEscapeNormal(Vector3f origin, Vector3f dir, float tx, float ty, float tz, float size) {
		final Vector3f topLeft = new Vector3f(tx,ty+size,tz);
		final Vector3f btmRight = new Vector3f(tx+size,ty,tz+size);
		float shortest = Float.MAX_VALUE;
		Vector3f outputNormal = new Vector3f();
		
		shortest = testAgainst(outputNormal, shortest, boxNormals[5], topLeft, origin, dir);
		shortest = testAgainst(outputNormal, shortest, boxNormals[4], btmRight, origin, dir);
		shortest = testAgainst(outputNormal, shortest, boxNormals[3], topLeft, origin, dir);
		shortest = testAgainst(outputNormal, shortest, boxNormals[2], btmRight, origin, dir);
		shortest = testAgainst(outputNormal, shortest, boxNormals[1], topLeft, origin, dir);
		shortest = testAgainst(outputNormal, shortest, boxNormals[0], btmRight, origin, dir);
		return outputNormal;
	}
	
	private static float testAgainst(Vector3f output, float originalDistance, Vector3f planeNormal, Vector3f planeOrigin, Vector3f rayOrigin, Vector3f rayNormal) {
		float dist = Float.MAX_VALUE;
		
		float d = planeNormal.dot(rayNormal);
		if (Math.abs(d) > .00001f) {
			dist = (Vector3f.sub(planeOrigin, rayOrigin).dot(planeNormal)) / d;
		}
		
		if (dist == Float.MAX_VALUE || dist < 0) {
			return originalDistance;
		}

		if (dist < originalDistance) {
			output.set(planeNormal);
			return dist;
		}
		
		return originalDistance;
	}
/*		final Vector3f topLeft = new Vector3f(tx,ty+size,tz);
		final Vector3f btmRight = new Vector3f(tx+size,ty,tz+size);
		float longest = 0;
		Vector3f outputNormal = null;
		
		longest = testAgainst(outputNormal, longest, boxNormals[5], btmRight, origin, dir, size);
		longest = testAgainst(outputNormal, longest, boxNormals[4], btmRight, origin, dir, size);
		longest = testAgainst(outputNormal, longest, boxNormals[3], btmRight, origin, dir, size);
		longest = testAgainst(outputNormal, longest, boxNormals[2], topLeft, origin, dir, size);
		longest = testAgainst(outputNormal, longest, boxNormals[1], topLeft, origin, dir, size);
		longest = testAgainst(outputNormal, longest, boxNormals[0], topLeft, origin, dir, size);
		return outputNormal;
	}

	private static float testAgainst(Vector3f output, float originalDistance, Vector3f planeNormal, Vector3f planeOrigin, Vector3f rayOrigin, Vector3f rayNormal, float boxSize) {
		Plane plane = new Plane(planeOrigin, planeNormal);
		Vector3f intersectionPoint = plane.rayIntersection(new Vector3f(rayOrigin), new Vector3f(rayNormal));
		if (intersectionPoint == null) {
			return originalDistance;
		}

		if (Math.abs(intersectionPoint.x - planeOrigin.x) > boxSize+.05f ||
				Math.abs(intersectionPoint.y - planeOrigin.y) > boxSize+.05f ||
				Math.abs(intersectionPoint.z - planeOrigin.z) > boxSize+.05f) {
			return originalDistance;
		}
		
		float dist = Vector3f.distanceSquared(rayOrigin, intersectionPoint);
		if (dist > originalDistance) {
			output=(planeNormal);
			Console.log("yehaw",planeNormal);
			return dist;
		}
		
		return originalDistance;
	}*/
}
