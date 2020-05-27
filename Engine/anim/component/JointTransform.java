package anim.component;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

public class JointTransform {

	private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
		final float x = start.x + (end.x - start.x) * progression;
		final float y = start.y + (end.y - start.y) * progression;
		final float z = start.z + (end.z - start.z) * progression;
		return new Vector3f(x, y, z);
	}

	public static JointTransform lerp(JointTransform frameA, JointTransform frameB, float progression) {
		final Vector3f pos = interpolate(frameA.position, frameB.position, progression);
		final Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
		return new JointTransform(pos, rot);
	}

	private final Vector3f position;

	private final Quaternion rotation;

	/*
	 * public static JointTransform lerp(JointTransform frameA, JointTransform
	 * frameB, float progression) { Vector3f pos = Vector3f.lerp(frameA.position,
	 * frameB.position, progression); Quaternion rot =
	 * Quaternion.interpolate(frameA.rotation, frameB.rotation, progression); return
	 * new JointTransform(pos, rot); }
	 */

	public JointTransform(Vector3f position, Quaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	public Matrix4f getMatrix() {
		final Matrix4f matrix = new Matrix4f();
		matrix.translate(position);
		Matrix4f.mul(matrix, rotation.toRotationMatrix(), matrix);
		return matrix;
	}
}
