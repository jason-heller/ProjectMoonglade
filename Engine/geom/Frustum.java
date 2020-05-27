package geom;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Frustum {

	private final Plane[] planes = { new Plane(), new Plane(), new Plane(), new Plane(), new Plane(), new Plane() };

	public boolean containsBoundingBox(Vector3f aabbMax, Vector3f aabbMin) {
		final Vector3f min = new Vector3f();

		for (int i = 0; i < 6; i++) {
			final Plane plane = planes[i];

			if (plane.normal.x > 0) {
				min.x = aabbMax.x;
			} else {
				min.x = aabbMin.x;
			}

			if (plane.normal.y > 0) {
				min.y = aabbMax.y;
			} else {
				min.y = aabbMin.y;
			}

			if (plane.normal.z > 0) {
				min.z = aabbMax.z;
			} else {
				min.z = aabbMin.z;
			}

			if (plane.normal.dot(min) + plane.dist > 0f) {
				continue;
			}

			return false;
			// if (plane.normal.dot(aabbMin) + plane.dist <= 0f)
			// return false;
			// if (plane.normal.dot(aabbMax) + plane.dist <= 0f)
			// return false;

		}

		return true;
	}

	public boolean containsPoint(Vector3f point) {
		for (final Plane plane : planes) {
			if (plane.classify(point, .0001f) == Plane.BEHIND) {
				return false;
			}
		}

		return true;
	}

	public boolean containsPointFast(int x, int y, int z) {
		final Vector3f pt = new Vector3f(x, y, z);
		if (planes[5].classify(pt, .1f) == Plane.BEHIND) {
			return false;
		}
		if (planes[2].classify(pt, .1f) == Plane.BEHIND) {
			return false;
		}
		if (planes[3].classify(pt, .1f) == Plane.BEHIND) {
			return false;
		}

		return true;
	}

	public Plane[] getPlanes() {
		return planes;
	}

	public void update(Matrix4f projViewMatrix) {

		final float[] projView = new float[16];
		projViewMatrix.get(projView, 0);

		planes[0].set(projView[3] - projView[0], projView[7] - projView[4], projView[11] - projView[8],
				projView[15] - projView[12]);
		planes[1].set(projView[3] + projView[0], projView[7] + projView[4], projView[11] + projView[8],
				projView[15] + projView[12]);
		planes[2].set(projView[3] + projView[1], projView[7] + projView[5], projView[11] + projView[9],
				projView[15] + projView[13]);
		planes[3].set(projView[3] - projView[1], projView[7] - projView[5], projView[11] - projView[9],
				projView[15] - projView[13]);
		planes[4].set(projView[3] - projView[2], projView[7] - projView[6], projView[11] - projView[10],
				projView[15] - projView[14]);
		planes[5].set(projView[3] + projView[2], projView[7] + projView[6], projView[11] + projView[10],
				projView[15] + projView[14]);

	}
}
