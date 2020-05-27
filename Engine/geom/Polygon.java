package geom;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Polygon {
	public static Polygon transform(Polygon polyNotTransformed, Matrix4f matrix) {
		final Polygon poly = new Polygon(polyNotTransformed);
		poly.applyMatrix(matrix);
		return poly;
	}

	public Vector3f p1, p2, p3;
	public Vector3f normal;

	private Vector3f min, max;

	private final Plane plane;

	private Vector3f center;

	public Polygon(float p1_x, float p1_y, float p1_z, float p2_x, float p2_y, float p2_z, float p3_x, float p3_y,
			float p3_z) {
		this(new Vector3f(p1_x, p1_y, p1_z), new Vector3f(p2_x, p2_y, p2_z), new Vector3f(p3_x, p3_y, p3_z));
	}

	public Polygon(float[] p1, float[] p2, float[] p3) {
		this.p1 = new Vector3f(p1);
		this.p2 = new Vector3f(p2);
		this.p3 = new Vector3f(p3);

		normal = Vector3f.cross(Vector3f.sub(this.p2, this.p1), Vector3f.sub(this.p3, this.p1)).normalize();

		plane = new Plane(normal, this.p1);
	}

	public Polygon(Polygon poly) {
		this.p1 = new Vector3f(poly.p1);
		this.p2 = new Vector3f(poly.p2);
		this.p3 = new Vector3f(poly.p3);
		this.normal = new Vector3f(poly.normal);
		this.center = new Vector3f(poly.center);
		this.plane = poly.getPlane();
	}

	public Polygon(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;

		center = new Vector3f(p1.x, p1.y, p1.z);

		normal = Vector3f.cross(Vector3f.sub(p2, p1), Vector3f.sub(p3, p1)).normalize();
		plane = new Plane(normal, p1);

		min = new Vector3f(Math.min(p3.x, Math.min(p1.x, p2.x)), Math.min(p3.y, Math.min(p1.y, p2.y)),
				Math.min(p3.z, Math.min(p1.z, p2.z)));
		max = new Vector3f(Math.max(p3.x, Math.max(p1.x, p2.x)), Math.max(p3.y, Math.max(p1.y, p2.y)),
				Math.max(p3.z, Math.max(p1.z, p2.z)));
	}

	public void applyMatrix(Matrix4f matrix) {
		p1.mul(matrix);
		p2.mul(matrix);
		p3.mul(matrix);
		final Quaternion q = Quaternion.fromMatrix(matrix);
		q.normalize();
		Vector3f.rotateVector(normal, q);
		min = new Vector3f(Math.min(p3.x, Math.min(p1.x, p2.x)), Math.min(p3.y, Math.min(p1.y, p2.y)),
				Math.min(p3.z, Math.min(p1.z, p2.z)));
		max = new Vector3f(Math.max(p3.x, Math.max(p1.x, p2.x)), Math.max(p3.y, Math.max(p1.y, p2.y)),
				Math.max(p3.z, Math.max(p1.z, p2.z)));
	}

	public float barryCentric(float relx, float rely) {
		final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		final float l1 = ((p2.z - p3.z) * (relx - p3.x) + (p3.x - p2.x) * (rely - p3.z)) / det;
		final float l2 = ((p3.z - p1.z) * (relx - p3.x) + (p1.x - p3.x) * (rely - p3.z)) / det;
		final float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public boolean containsPoint(Vector3f point) {
		final Vector3f v0 = Vector3f.sub(p1, point);
		final Vector3f v1 = Vector3f.sub(p2, point);
		final Vector3f v2 = Vector3f.sub(p3, point);

		final float ab = v0.dot(v1);
		final float ac = v0.dot(v2);
		final float bc = v1.dot(v2);
		final float cc = v2.dot(v2);

		if (bc * ac - cc * ab < 0) {
			return false;
		}
		final float bb = v1.dot(v1);
		if (ab * bc - ac * bb < 0) {
			return false;
		}
		return true;
	}

	public Vector3f getMax() {
		return max;
	}

	public Vector3f getMin() {
		return min;
	}

	public Plane getPlane() {
		return plane;
	}

	public void mul(float f) {
		p1.mul(f);
		p2.mul(f);
		p3.mul(f);
	}

	public void transform(Matrix4f transform) {
		final Vector4f t = new Vector4f(p1.x, p1.y, p1.z, 1f);
		t.mul(transform);
		p1.set(t.x, t.y, t.z);

		t.set(p2.x, p2.y, p2.z, 1f);
		t.mul(transform);
		p2.set(t.x, t.y, t.z);

		t.set(p3.x, p3.y, p3.z, 1f);
		t.mul(transform);
		p3.set(t.x, t.y, t.z);

		normal = Vector3f.cross(Vector3f.sub(p2, p1), Vector3f.sub(p3, p1)).normalize();
		plane.normal = normal;
		plane.dist = Vector3f.dot(normal, p1);

		min = new Vector3f(Math.min(p3.x, Math.min(p1.x, p2.x)), Math.min(p3.y, Math.min(p1.y, p2.y)),
				Math.min(p3.z, Math.min(p1.z, p2.z)));
		max = new Vector3f(Math.max(p3.x, Math.max(p1.x, p2.x)), Math.max(p3.y, Math.max(p1.y, p2.y)),
				Math.max(p3.z, Math.max(p1.z, p2.z)));
	}

	public void translate(int i, int j, int k) {
		p1.add(i, j, k);
		p2.add(i, j, k);
		p3.add(i, j, k);
	}
}
