/*
 * (C) Copyright 2015 Richard Greenlees

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package org.joml;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.nio.FloatBuffer;

/**
 * Contains the definition and functions for rotations expressed as
 * 4-dimensional vectors
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Quaternion implements Serializable, Externalizable {

	public static final Quaternion zero = new Quaternion(0, 0, 0, 1);

	/**
	 * Add q2 to q1 and store the results in dest. Does not modify q1 or q2
	 */
	public static void add(Quaternion q1, Quaternion q2, Quaternion dest) {
		dest.x = q1.x + q2.x;
		dest.y = q1.y + q2.y;
		dest.z = q1.z + q2.z;
		dest.w = q1.w + q2.w;
	}

	/**
	 * Conjugates a and stores the results in dest. Does not modify a
	 */
	public static void conjugate(Quaternion a, Quaternion dest) {
		dest.x = a.x;
		dest.y = -a.y;
		dest.z = -a.z;
		dest.w = -a.w;
	}

	/**
	 * Divides a by b and stores the results in dest. Does not modify a or b
	 */
	public static void div(Quaternion a, Quaternion b, Quaternion dest) {
		dest.x = a.x;
		dest.y = a.y;
		dest.z = a.z;
		dest.w = a.w;

		dest.invert();
		dest.mul(b);
	}

	/**
	 * Returns the dot product of a and b
	 */
	public static float dot(Quaternion a, Quaternion b) {
		return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
	}

	/**
	 * Extracts the rotation part of a transformation matrix and converts it to a
	 * quaternion using the magic of maths.
	 *
	 * More detailed explanation here:
	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
	 *
	 * @param matrix - the transformation matrix containing the rotation which this
	 *               quaternion shall represent.
	 */
	public static Quaternion fromMatrix(Matrix4f matrix) {
		float w, x, y, z;
		final float diagonal = matrix.m00 + matrix.m11 + matrix.m22;
		if (diagonal > 0) {
			final float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
			w = w4 / 4f;
			x = (matrix.m21 - matrix.m12) / w4;
			y = (matrix.m02 - matrix.m20) / w4;
			z = (matrix.m10 - matrix.m01) / w4;
		} else if (matrix.m00 > matrix.m11 && matrix.m00 > matrix.m22) {
			final float x4 = (float) (Math.sqrt(1f + matrix.m00 - matrix.m11 - matrix.m22) * 2f);
			w = (matrix.m21 - matrix.m12) / x4;
			x = x4 / 4f;
			y = (matrix.m01 + matrix.m10) / x4;
			z = (matrix.m02 + matrix.m20) / x4;
		} else if (matrix.m11 > matrix.m22) {
			final float y4 = (float) (Math.sqrt(1f + matrix.m11 - matrix.m00 - matrix.m22) * 2f);
			w = (matrix.m02 - matrix.m20) / y4;
			x = (matrix.m01 + matrix.m10) / y4;
			y = y4 / 4f;
			z = (matrix.m12 + matrix.m21) / y4;
		} else {
			final float z4 = (float) (Math.sqrt(1f + matrix.m22 - matrix.m00 - matrix.m11) * 2f);
			w = (matrix.m10 - matrix.m01) / z4;
			x = (matrix.m02 + matrix.m20) / z4;
			y = (matrix.m12 + matrix.m21) / z4;
			z = z4 / 4f;
		}
		return new Quaternion(x, y, z, w);
	}

	/**
	 * Finds the angle represented by q in degrees
	 */
	public static float getAngle(Quaternion q) {
		final float angle = 2.0f * (float) Math.acos(q.w);
		return angle <= Math.PI ? angle : 2.0f * (float) Math.PI - angle;
	}

	/**
	 * Generates a rotation matrix from this Quaternion and stores the results in
	 * dest
	 */
	public static void getMatrix(Quaternion quat, FloatBuffer dest) {
		final float q00 = 2.0f * quat.x * quat.x;
		final float q11 = 2.0f * quat.y * quat.y;
		final float q22 = 2.0f * quat.z * quat.z;

		final float q01 = 2.0f * quat.x * quat.y;
		final float q02 = 2.0f * quat.x * quat.z;
		final float q03 = 2.0f * quat.x * quat.w;

		final float q12 = 2.0f * quat.y * quat.z;
		final float q13 = 2.0f * quat.y * quat.w;

		final float q23 = 2.0f * quat.z * quat.w;

		dest.put(1.0f - q11 - q22);
		dest.put(q01 + q23);
		dest.put(q02 - q13);
		dest.put(0.0f);
		dest.put(q01 - q23);
		dest.put(1.0f - q22 - q00);
		dest.put(q12 + q03);
		dest.put(0.0f);
		dest.put(q02 + q13);
		dest.put(q12 - q03);
		dest.put(1.0f - q11 - q00);
		dest.put(0.0f);
		dest.put(0.0f);
		dest.put(0.0f);
		dest.put(1.0f);
	}

	/**
	 * Generates a rotation matrix from q and stores the results in dest
	 */
	public static void getMatrix(Quaternion q, Matrix4f dest) {
		final float q00 = 2.0f * q.x * q.x;
		final float q11 = 2.0f * q.y * q.y;
		final float q22 = 2.0f * q.z * q.z;

		final float q01 = 2.0f * q.x * q.y;
		final float q02 = 2.0f * q.x * q.z;
		final float q03 = 2.0f * q.x * q.w;

		final float q12 = 2.0f * q.y * q.z;
		final float q13 = 2.0f * q.y * q.w;

		final float q23 = 2.0f * q.z * q.w;

		dest.m00 = 1.0f - q11 - q22;
		dest.m01 = q01 + q23;
		dest.m02 = q02 - q13;
		dest.m03 = 0.0f;
		dest.m10 = q01 - q23;
		dest.m11 = 1.0f - q22 - q00;
		dest.m12 = q12 + q03;
		dest.m13 = 0.0f;
		dest.m20 = q02 + q13;
		dest.m21 = q12 - q03;
		dest.m22 = 1.0f - q11 - q00;
		dest.m30 = 0.0f;
		dest.m31 = 0.0f;
		dest.m32 = 0.0f;
		dest.m33 = 1.0f;
	}

	public static Quaternion interpolate(Quaternion a, Quaternion b, float blend) {
		final Quaternion result = new Quaternion(0, 0, 0, 1);
		final float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
		final float blendI = 1f - blend;
		if (dot < 0) {
			result.w = blendI * a.w + blend * -b.w;
			result.x = blendI * a.x + blend * -b.x;
			result.y = blendI * a.y + blend * -b.y;
			result.z = blendI * a.z + blend * -b.z;
		} else {
			result.w = blendI * a.w + blend * b.w;
			result.x = blendI * a.x + blend * b.x;
			result.y = blendI * a.y + blend * b.y;
			result.z = blendI * a.z + blend * b.z;
		}
		result.normalize();
		return result;
	}

	/**
	 * Inverts q and stores the results in dest. Does not modify q
	 */
	public static void invert(Quaternion q, Quaternion dest) {
		final float norm = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
		dest.x = q.x / norm;
		dest.y = -q.y / norm;
		dest.z = -q.z / norm;
		dest.w = -q.w / norm;
	}

	/**
	 * Return the length of q.
	 * 
	 * @return the length
	 */
	public static float length(Quaternion q) {
		return q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
	}

	/**
	 * Set <code>dest</code> to be a rotation leading <code>sourcePoint</code> to
	 * rotate to <code>destPoint</code>.
	 */
	public static void lookAt(Vector3f sourcePoint, Vector3f destPoint, Vector3f up, Vector3f forward,
			Quaternion dest) {
		float dirX = destPoint.x - sourcePoint.x;
		float dirY = destPoint.y - sourcePoint.y;
		float dirZ = destPoint.z - sourcePoint.z;

		float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);

		dirX /= length;
		dirY /= length;
		dirZ /= length;

		final float dot = forward.x * dirX + forward.y * dirY + forward.z * dirZ;

		if (Math.abs(dot - (-1.0f)) < 0.000001f) {
			dest.x = up.x;
			dest.y = up.y;
			dest.z = up.z;
			dest.w = (float) Math.PI;
			return;
		}

		if (Math.abs(dot - 1.0f) < 0.000001f) {
			dest.x = 0.0f;
			dest.y = 0.0f;
			dest.z = 0.0f;
			dest.w = 1.0f;
			return;
		}

		final float rotAngle = (float) Math.acos(dot);

		float rotAxisX = forward.y * dirZ - forward.z * dirY;
		float rotAxisY = forward.z * dirX - forward.x * dirZ;
		float rotAxisZ = forward.x * dirY - forward.y * dirX;

		length = (float) Math.sqrt(rotAxisX * rotAxisX + rotAxisY * rotAxisY + rotAxisZ * rotAxisZ);

		rotAxisX /= length;
		rotAxisY /= length;
		rotAxisZ /= length;

		dest.setAngleAxis(rotAngle, rotAxisX, rotAxisY, rotAxisZ);
	}

	/**
	 * Multiply a by b and store the results in dest.
	 */
	public static void mul(Quaternion a, Quaternion b, Quaternion dest) {
		if (a != dest && b != dest) {
			dest.x = a.x * b.x - a.y * b.y - a.z * b.z - a.w * b.w;
			dest.y = a.x * b.y + a.y * b.x + a.z * b.w - a.w * b.z;
			dest.z = a.x * b.z - a.y * b.w + a.z * b.x + a.w * b.y;
			dest.w = a.x * b.w + a.y * b.z - a.z * b.y + a.w * b.x;
		} else {
			dest.set(a.x * b.x - a.y * b.y - a.z * b.z - a.w * b.w, a.x * b.y + a.y * b.x + a.z * b.w - a.w * b.z,
					a.x * b.z - a.y * b.w + a.z * b.x + a.w * b.y, a.x * b.w + a.y * b.z - a.z * b.y + a.w * b.x);
		}
	}

	public static Quaternion negate(Quaternion q) {
		q.x = -q.x;
		q.y = -q.y;
		q.z = -q.z;
		return q;
	}

	/**
	 * Normalizes the supplied Quaternion source and stores the results in dest.
	 * Does not modify the source
	 */
	public static void normalize(Quaternion source, Quaternion dest) {
		final float norm = (float) Math
				.sqrt(source.x * source.x + source.y * source.y + source.z * source.z + source.w * source.w);

		dest.x = source.x / norm;
		dest.y = source.y / norm;
		dest.z = source.z / norm;
		dest.w = source.w / norm;
	}

	/**
	 * Spherical linear interpolation between the start and target Quaternions,
	 * using the specified alpha, and storing the results in dest. Neither the start
	 * or target are modified
	 */
	public static Quaternion slerp(Quaternion start, Quaternion target, float alpha) {
		final float dot = Math.abs(start.x * target.x + start.y * target.y + start.z * target.z + start.w * target.w);
		float scale1, scale2;

		if (1.0f - dot > 0.1) {
			final float angle = (float) Math.acos(dot);
			final float sinAngle = 1f / (float) Math.sin(angle);

			scale1 = (float) Math.sin((1f - alpha) * angle) * sinAngle;
			scale2 = (float) Math.sin(alpha * angle) * sinAngle;
		} else {
			scale1 = 1f - alpha;
			scale2 = alpha;
		}

		if (dot < 0.f) {
			scale2 = -scale2;
		}

		final Quaternion dest = new Quaternion();
		dest.x = scale1 * start.x + scale2 * target.x;
		dest.y = scale1 * start.y + scale2 * target.y;
		dest.z = scale1 * start.z + scale2 * target.z;
		dest.w = scale1 * start.w + scale2 * target.w;
		return dest;
	}

	public float x;

	public float y;

	public float z;

	public float w;

	public Quaternion() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
		w = 1.0f;
	}

	public Quaternion(AngleAxis4f axisAngle) {
		final float sin = (float) Math.sin(Math.toRadians(axisAngle.angle) / 2.0);
		final float cos = (float) Math.cos(Math.toRadians(axisAngle.angle) / 2.0);
		x = axisAngle.x * sin;
		x = axisAngle.y * sin;
		x = axisAngle.z * sin;
		w = cos;
	}

	public Quaternion(float newX, float newY, float newZ) {
		x = newX;
		y = newY;
		z = newZ;
		w = 1.0f;
	}

	public Quaternion(float newX, float newY, float newZ, float newW) {
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
	}

	public Quaternion(Quaternion source) {
		x = source.x;
		y = source.y;
		z = source.z;
		w = source.w;
	}

	/**
	 * Add <code>q2</code> to this quaternion.
	 * 
	 * @return this
	 */
	public Quaternion add(Quaternion q2) {
		x += q2.x;
		y += q2.y;
		z += q2.z;
		w += q2.w;
		return this;
	}

	/**
	 * Conjugates this Quaternion.
	 * 
	 * @return this
	 */
	public Quaternion conjugate() {
		y = -y;
		z = -z;
		w = -w;
		return this;
	}

	/**
	 * Divides this Quaternion by b.
	 * 
	 * @return this
	 */
	public Quaternion div(Quaternion b) {
		invert();
		mul(b);
		return this;
	}

	/**
	 * Divides this Quaternion by b and store the result in <code>dest</code>.
	 * 
	 * @param b    the {@link Quaternion} to divide this by
	 * @param dest will hold the result
	 * @return this
	 */
	public Quaternion div(Quaternion b, Quaternion dest) {
		div(this, b, dest);
		return this;
	}

	/**
	 * Returns the dot of this Quaternion and otherQuat
	 */
	public float dot(Quaternion otherQuat) {
		return this.x * otherQuat.x + this.y * otherQuat.y + this.z * otherQuat.z + this.w * otherQuat.w;
	}

	/**
	 * Finds the angle represented by this Quaternion in degrees
	 */
	public float getAngle() {
		final float angle = 2.0f * (float) Math.acos(w);
		return angle <= Math.PI ? angle : 2.0f * (float) Math.PI - angle;
	}

	/**
	 * Generate a rotation matrix from this Quaternion and store the result in
	 * <code>dest</code>.
	 * 
	 * @return this
	 */
	public Quaternion getMatrix(FloatBuffer dest) {
		getMatrix(this, dest);
		return this;
	}

	/**
	 * Generate a rotation matrix from this Quaternion and store the result in
	 * <code>dest</code>.
	 * 
	 * @return this
	 */
	public Quaternion getMatrix(Matrix4f dest) {
		getMatrix(this, dest);
		return this;
	}

	/**
	 * Set this Quaternion to the identity.
	 * 
	 * @return this
	 */
	public Quaternion identity() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
		w = 1.0f;
		return this;
	}

	/**
	 * Invert this Quaternion.
	 * 
	 * @return this
	 */
	public Quaternion invert() {
		final float norm = x * x + y * y + z * z + w * w;
		x = x / norm;
		y = -y / norm;
		z = -z / norm;
		w = -w / norm;
		return this;
	}

	/**
	 * Invert this Quaternion and store the result in <code>dest</code>.
	 * 
	 * @param dest will hold the result
	 * @return this
	 */
	public Quaternion invert(Quaternion dest) {
		invert(this, dest);
		return this;
	}

	/**
	 * Return the length of this quaternion.
	 * 
	 * @return the length
	 */
	public float length() {
		return x * x + y * y + z * z + w * w;
	}

	/**
	 * Set <code>dest</code> to be a rotation leading <code>sourcePoint</code> to
	 * rotate to <code>destPoint</code>.
	 * 
	 * @return this
	 */
	public Quaternion lookAt(Vector3f sourcePoint, Vector3f destPoint, Vector3f up, Vector3f forward) {
		lookAt(sourcePoint, destPoint, up, forward, this);
		return this;
	}

	/**
	 * Multiply this Quaternion by q.
	 * 
	 * @return this
	 */
	public Quaternion mul(Quaternion q) {
		mul(this, q, this);
		return this;
	}

	/**
	 * Normalize this Quaternion.
	 * 
	 * @return this
	 */
	public Quaternion normalize() {
		final float norm = (float) Math.sqrt(x * x + y * y + z * z + w * w);

		x /= norm;
		y /= norm;
		z /= norm;
		w /= norm;

		return this;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		w = in.readFloat();
	}

	/**
	 * Set this {@link Quaternion} to be equivalent to the given
	 * {@link AngleAxis4f}.
	 * 
	 * @param axisAngle the {@link AngleAxis4f}
	 * @return this
	 */
	public Quaternion set(AngleAxis4f axisAngle) {
		final float sin = (float) Math.sin(Math.toRadians(axisAngle.angle) / 2.0);
		final float cos = (float) Math.cos(Math.toRadians(axisAngle.angle) / 2.0);
		x = axisAngle.x * sin;
		x = axisAngle.y * sin;
		x = axisAngle.z * sin;
		w = cos;
		return this;
	}

	/**
	 * Set the x, y and z components of this Quaternion to the given values.
	 * 
	 * @return this
	 */
	public Quaternion set(float newX, float newY, float newZ) {
		x = newX;
		y = newY;
		z = newZ;
		return this;
	}

	/**
	 * Set this Quaternion to the given values.
	 * 
	 * @return this
	 */
	public Quaternion set(float newX, float newY, float newZ, float newW) {
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}

	/**
	 * Set this Quaternion to be a copy of q.
	 * 
	 * @return this
	 */
	public Quaternion set(Quaternion q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
		return this;
	}

	/**
	 * Set this Quaternion to be a representation of the supplied axis and angle (in
	 * degrees).
	 * 
	 * @return this
	 */
	public Quaternion setAngleAxis(float angle, float axisX, float axisY, float axisZ) {
		final float hangle = (float) Math.toRadians(angle / 2.0);
		final float sinAngle = (float) Math.sin(hangle);
		final float vLength = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

		x = axisX / vLength * sinAngle;
		y = axisY / vLength * sinAngle;
		z = axisZ / vLength * sinAngle;
		w = (float) Math.cos(hangle);

		return this;
	}

	/**
	 * Set this Quaternion to be a representation of the supplied axis and angle (in
	 * degrees).
	 * 
	 * @return this
	 */
	public Quaternion setAngleAxis(float angle, Vector3f axis) {
		return setAngleAxis(angle, axis.x, axis.y, axis.z);
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in degrees) with rotation
	 * order XYZ.
	 * <p>
	 * This method implements the solution outlined in <a href=
	 * "http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this
	 * stackexchange answer</a>.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesDegXYZ(float rotationAboutX, float rotationAboutY, float rotationAboutZ) {
		final float sx = (float) Math.sin(Math.toRadians(rotationAboutX) * 0.5);
		final float cx = (float) Math.cos(Math.toRadians(rotationAboutX) * 0.5);
		final float sy = (float) Math.sin(Math.toRadians(rotationAboutY) * 0.5);
		final float cy = (float) Math.cos(Math.toRadians(rotationAboutY) * 0.5);
		final float sz = (float) Math.sin(Math.toRadians(rotationAboutZ) * 0.5);
		final float cz = (float) Math.cos(Math.toRadians(rotationAboutZ) * 0.5);

		x = cx * cy * cz + sx * sy * sz;
		y = sx * cy * cz - cx * sy * sz;
		z = cx * sy * cz + sx * cy * sz;
		w = cx * cy * sz - sx * sy * cz;

		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in degrees) with rotation
	 * order XYZ.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesDegXYZ(Vector3f angles) {
		setEulerAnglesDegXYZ(angles.x, angles.y, angles.z);
		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in degrees) with rotation
	 * order ZYX.
	 * <p>
	 * This method implements the solution outlined in <a href=
	 * "http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this
	 * stackexchange answer</a>.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesDegZYX(float rotationAboutX, float rotationAboutY, float rotationAboutZ) {
		final float sx = (float) Math.sin(Math.toRadians(rotationAboutX) * 0.5);
		final float cx = (float) Math.cos(Math.toRadians(rotationAboutX) * 0.5);
		final float sy = (float) Math.sin(Math.toRadians(rotationAboutY) * 0.5);
		final float cy = (float) Math.cos(Math.toRadians(rotationAboutY) * 0.5);
		final float sz = (float) Math.sin(Math.toRadians(rotationAboutZ) * 0.5);
		final float cz = (float) Math.cos(Math.toRadians(rotationAboutZ) * 0.5);

		x = cx * cy * cz - sx * sy * sz;
		y = sx * cy * cz + cx * sy * sz;
		z = cx * sy * cz - sx * cy * sz;
		w = cx * cy * sz + sx * sy * cz;

		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in degrees) with rotation
	 * order ZYX.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesDegZYX(Vector3f angles) {
		setEulerAnglesDegZYX(angles.x, angles.y, angles.z);
		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in radians) with rotation
	 * order XYZ.
	 * <p>
	 * This method implements the solution outlined in <a href=
	 * "http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this
	 * stackexchange answer</a>.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesRadXYZ(float rotationAboutX, float rotationAboutY, float rotationAboutZ) {
		final float sx = (float) Math.sin(rotationAboutX * 0.5f);
		final float cx = (float) Math.cos(rotationAboutX * 0.5f);
		final float sy = (float) Math.sin(rotationAboutY * 0.5f);
		final float cy = (float) Math.cos(rotationAboutY * 0.5f);
		final float sz = (float) Math.sin(rotationAboutZ * 0.5f);
		final float cz = (float) Math.cos(rotationAboutZ * 0.5f);

		x = cx * cy * cz + sx * sy * sz;
		y = sx * cy * cz - cx * sy * sz;
		z = cx * sy * cz + sx * cy * sz;
		w = cx * cy * sz - sx * sy * cz;

		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in radians) with rotation
	 * order XYZ.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesRadXYZ(Vector3f angles) {
		setEulerAnglesRadXYZ(angles.x, angles.y, angles.z);
		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in radians) with rotation
	 * order ZYX.
	 * <p>
	 * This method implements the solution outlined in <a href=
	 * "http://gamedev.stackexchange.com/questions/13436/glm-euler-angles-to-quaternion#answer-13446">this
	 * stackexchange answer</a>.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesRadZYX(float rotationAboutX, float rotationAboutY, float rotationAboutZ) {
		final float sx = (float) Math.sin(rotationAboutX * 0.5f);
		final float cx = (float) Math.cos(rotationAboutX * 0.5f);
		final float sy = (float) Math.sin(rotationAboutY * 0.5f);
		final float cy = (float) Math.cos(rotationAboutY * 0.5f);
		final float sz = (float) Math.sin(rotationAboutZ * 0.5f);
		final float cz = (float) Math.cos(rotationAboutZ * 0.5f);

		x = cx * cy * cz - sx * sy * sz;
		y = sx * cy * cz + cx * sy * sz;
		z = cx * sy * cz - sx * cy * sz;
		w = cx * cy * sz + sx * sy * cz;

		return this;
	}

	/**
	 * Set this Quaternion from the supplied euler angles (in radians) with rotation
	 * order ZYX.
	 * 
	 * @return this
	 */
	public Quaternion setEulerAnglesRadZYX(Vector3f angles) {
		setEulerAnglesRadZYX(angles.x, angles.y, angles.z);
		return this;
	}

	/**
	 * Spherical linear interpolation between this Quaternion and the specified
	 * target, using the specified alpha.
	 * 
	 * @return this
	 */
	public Quaternion slerp(Quaternion target, float alpha) {
		slerp(this, target, alpha);
		return this;
	}

	public Matrix4f toRotationMatrix() {
		final Matrix4f matrix = new Matrix4f();
		final float xy = x * y;
		final float xz = x * z;
		final float xw = x * w;
		final float yz = y * z;
		final float yw = y * w;
		final float zw = z * w;
		final float xSquared = x * x;
		final float ySquared = y * y;
		final float zSquared = z * z;
		matrix.m00 = 1 - 2 * (ySquared + zSquared);
		matrix.m01 = 2 * (xy - zw);
		matrix.m02 = 2 * (xz + yw);
		matrix.m03 = 0;
		matrix.m10 = 2 * (xy + zw);
		matrix.m11 = 1 - 2 * (xSquared + zSquared);
		matrix.m12 = 2 * (yz - xw);
		matrix.m13 = 0;
		matrix.m20 = 2 * (xz - yw);
		matrix.m21 = 2 * (yz + xw);
		matrix.m22 = 1 - 2 * (xSquared + ySquared);
		matrix.m23 = 0;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		matrix.m33 = 1;
		return matrix;
	}

	@Override
	public String toString() {
		return "Quaternion { " + x + ", " + y + ", " + z + ", " + w + " }";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(w);
	}
}
