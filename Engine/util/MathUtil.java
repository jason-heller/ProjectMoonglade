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

		return (float) -Math.toDegrees(Math.atan2(dy, dx));
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
		}/*
		int dx, dy, p, x, y;
		dx = x1 - x1;
		dy = y1 - y1;
		x = x1;
		y = y1;
		p = 2 * dy - dx;
		while (x < x2) {
			if (p >= 0) {
				points.add(new Vector2f(x,y));
				y = y + 1;
				p = p + 2 * dy - 2 * dx;
			} else {
				points.add(new Vector2f(x,y));
				p = p + 2 * dy;
			}
			x = x + 1;
		}*/
		return points;
	}
}
