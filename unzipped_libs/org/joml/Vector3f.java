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

/**
 * Contains the definition of a Vector comprising 3 floats and associated
 * transformations.
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Vector3f implements Serializable, Externalizable {

	public static final Vector3f ZERO = new Vector3f(0, 0, 0);
	public static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);
	public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

	public static Vector3f abs(Vector3f v) {
		return new Vector3f(Math.abs(v.x), Math.abs(v.y), Math.abs(v.z));
	}

	/**
	 * Adds v2 to v1. Does not modify v1 or v2
	 */
	public static Vector3f add(Vector3f v1, Vector3f v2) {
		final Vector3f dest = new Vector3f();
		dest.x = v1.x + v2.x;
		dest.y = v1.y + v2.y;
		dest.z = v1.z + v2.z;
		return dest;
	}

	/**
	 * Adds v2 to v1 and stores the results in dest. Does not modify v1 or v2
	 */
	public static void add(Vector3f v1, Vector3f v2, Vector3f dest) {
		dest.x = v1.x + v2.x;
		dest.y = v1.y + v2.y;
		dest.z = v1.z + v2.z;
	}

	/**
	 * Set this vector to be the cross of v1 and v2.
	 * 
	 * @return this
	 */
	/*
	 * public Vector3f cross(Vector3f v1, Vector3f v2) { return set(v1.y * v2.z -
	 * v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x); }
	 */

	public static Vector3f cross(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
	}

	/**
	 * Calculate the cross of v1 and v2 and store the results in dest.
	 */
	public static void cross(Vector3f v1, Vector3f v2, Vector3f dest) {
		dest.set(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
	}

	/**
	 * Return the distance between the start and end vectors.
	 */
	public static float distance(Vector3f start, Vector3f end) {
		return (float) Math.sqrt(distanceSquared(start, end));
	}

	public static float distanceSquared(Vector3f start, Vector3f end) {
		return (end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y)
				+ (end.z - start.z) * (end.z - start.z);
	}

	public static Vector3f div(Vector3f v, float scalar) {
		final Vector3f d = new Vector3f(v.x, v.y, v.z);
		d.x /= scalar;
		d.y /= scalar;
		d.z /= scalar;
		return d;
	}

	public static Vector3f div(Vector3f v1, Vector3f v2) {
		final Vector3f dest = new Vector3f();
		dest.x = v1.x / v2.x;
		dest.y = v1.y / v2.y;
		dest.z = v1.z / v2.z;
		return dest;
	}

	/**
	 * Return the dot product of the supplied v1 and v2 vectors.
	 */
	public static float dot(Vector3f v1, Vector3f v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	public static Vector3f lerp(Vector3f A, Vector3f B, float t) {
		return Vector3f.add(Vector3f.mul(A, t), Vector3f.mul(B, 1.f - t));
	}

	public static Vector3f mul(Vector3f v, float scalar) {
		final Vector3f d = new Vector3f(v);
		d.x *= scalar;
		d.y *= scalar;
		d.z *= scalar;
		return d;
	}

	/*
	 * Multiply the given Vector3f v by the scalar value, and store in dest. Does
	 * not modify v
	 */
	public static void mul(Vector3f v, float scalar, Vector3f dest) {
		dest.x = v.x * scalar;
		dest.y = v.y * scalar;
		dest.z = v.z * scalar;
	}

	/**
	 * Multiply Vector3f v by the given matrix mat and store the result in dest.
	 */
	public static void mul(Vector3f v, Matrix3f mat, Vector3f dest) {
		if (v != dest) {
			dest.x = mat.m00 * v.x + mat.m10 * v.y + mat.m20 * v.z;
			dest.y = mat.m01 * v.x + mat.m11 * v.y + mat.m21 * v.z;
			dest.z = mat.m02 * v.x + mat.m12 * v.y + mat.m22 * v.z;
		} else {
			dest.set(mat.m00 * v.x + mat.m10 * v.y + mat.m20 * v.z, mat.m01 * v.x + mat.m11 * v.y + mat.m21 * v.z,
					mat.m02 * v.x + mat.m12 * v.y + mat.m22 * v.z);
		}
	}

	/**
	 * Multiply Vector3f v by the given matrix mat and store the result in dest.
	 */
	public static void mul(Vector3f v, Matrix4f mat, Vector3f dest) {
		if (v != dest) {
			dest.x = mat.m00 * v.x + mat.m10 * v.y + mat.m20 * v.z;
			dest.y = mat.m01 * v.x + mat.m11 * v.y + mat.m21 * v.z;
			dest.z = mat.m02 * v.x + mat.m12 * v.y + mat.m22 * v.z;
		} else {
			dest.set(mat.m00 * v.x + mat.m10 * v.y + mat.m20 * v.z, mat.m01 * v.x + mat.m11 * v.y + mat.m21 * v.z,
					mat.m02 * v.x + mat.m12 * v.y + mat.m22 * v.z);
		}
	}

	public static Vector3f mul(Vector3f v1, Vector3f v2) {
		final Vector3f dest = new Vector3f();
		dest.x = v1.x * v2.x;
		dest.y = v1.y * v2.y;
		dest.z = v1.z * v2.z;
		return dest;
	}

	/**
	 * Multiply v1 by v2 component-wise and store the result into dest.
	 */
	public static void mul(Vector3f v1, Vector3f v2, Vector3f dest) {
		dest.x = v1.x * v2.x;
		dest.y = v1.y * v2.y;
		dest.z = v1.z * v2.z;
	}

	public static Vector3f negate(Vector3f v) {
		return new Vector3f(-v.x, -v.y, -v.z);
	}

	/**
	 * Normalize the original vector and store the results in dest.
	 */
	public static void normalize(Vector3f original, Vector3f dest) {
		final float d = original.length();
		dest.set(original.x / d, original.y / d, original.z / d);
	}

	public static Vector3f rotate(Vector3f vec, Vector3f axis, float thetaRadians) {
		final float sinTh = (float) Math.sin(thetaRadians);
		final float cosTh = (float) Math.cos(thetaRadians);
		final float dp = vec.dot(axis);

		final Vector3f out = new Vector3f();

		out.x = axis.x * dp * (1f - cosTh) + vec.x * cosTh + (-axis.z * vec.y + axis.y * vec.z) * sinTh;
		out.y = axis.y * dp * (1f - cosTh) + vec.y * cosTh + (axis.z * vec.x - axis.x * vec.z) * sinTh;
		out.z = axis.z * dp * (1f - cosTh) + vec.z * cosTh + (-axis.y * vec.x + axis.x * vec.y) * sinTh;
		return out;
	}

	public static Vector3f rotateVector(Vector3f vector, Quaternion quat) {
		final Quaternion vecQuat = new Quaternion(vector.x, vector.y, vector.z, 0.0f);

		final Quaternion quatNegate = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
		Quaternion.negate(quatNegate);

		final Quaternion resQuat = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
		Quaternion.mul(vecQuat, quatNegate, resQuat);
		Quaternion.mul(quat, resQuat, resQuat);

		return new Vector3f(resQuat.x, resQuat.y, resQuat.z);
	}

	public static Vector3f sub(Vector3f v1, Vector3f v2) {
		final Vector3f dest = new Vector3f();
		dest.x = v1.x - v2.x;
		dest.y = v1.y - v2.y;
		dest.z = v1.z - v2.z;
		return dest;
	}

	/**
	 * Subtracts v2 from v1 and stores the results in dest. Does not modify v1 or v2
	 */
	public static void sub(Vector3f v1, Vector3f v2, Vector3f dest) {
		dest.x = v1.x - v2.x;
		dest.y = v1.y - v2.y;
		dest.z = v1.z - v2.z;
	}

	public float x;

	public float y;

	public float z;

	public Vector3f() {
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f(float[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}

	public Vector3f(Vector3f clone) {
		this.x = clone.x;
		this.y = clone.y;
		this.z = clone.z;
	}

	public Vector3f(Vector4f vector) {
		x = vector.x;
		y = vector.y;
		z = vector.z;
	}

	public Vector3f abs() {
		return new Vector3f(Math.abs(x), Math.abs(y), Math.abs(z));
	}

	public Vector3f add(float _x, float _y, float _z) {
		x += _x;
		y += _y;
		z += _z;
		return this;
	}

	/**
	 * Adds the supplied vector to this one
	 * 
	 * @param v
	 * @return this
	 */
	public Vector3f add(Vector3f v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	public Vector3f average(Vector3f v1, Vector3f v2) {
		return new Vector3f((v1.x + v2.x) * 0.5f, (v1.y + v2.y) * 0.5f, (v1.z + v2.z) * 0.5f);
	}

	public void cap(float f) {
		// x = Math.min(Math.abs(x), f)*Math.signum(x);
		// y = Math.min(Math.abs(y), f)*Math.signum(y);
		// z = Math.min(Math.abs(z), f)*Math.signum(z);

		if (length() > f) {
			setLength(f);
		}
	}

	/**
	 * Set this vector to be the cross of itself and v.
	 * 
	 * @return this
	 */
	public Vector3f cross(Vector3f v) {
		return new Vector3f(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}

	/**
	 * Return the distance between this Vector and v.
	 */
	public float distance(Vector3f v) {
		return (float) Math.sqrt(
				(v.x - this.x) * (v.x - this.x) + (v.y - this.y) * (v.y - this.y) + (v.z - this.z) * (v.z - this.z));
	}

	public Vector3f div(float i) {
		x /= i;
		y /= i;
		z /= i;
		return this;
	}

	public Vector3f div(Vector3f v) {
		x /= v.x;
		y /= v.y;
		z /= v.z;
		return this;
	}

	/**
	 * Return the dot product of this vector and the supplied vector.
	 */
	public float dot(Vector3f v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public boolean isFacing(Vector3f v) {
		return dot(v) < 0f;
	}

	public boolean isZero() {
		// TODO Auto-generated method stub
		return x == 0 && y == 0 && z == 0;
	}

	/**
	 * Returns the length of this vector
	 */
	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	/*
	 * public static Vector3f rotate(Vector3f vec, Vector3f axis, float rotation) {
	 * Matrix4f matrix = new Matrix4f();
	 * 
	 * Vector3f pos = new Vector3f(vec);
	 * 
	 * matrix.m03 = pos.x; matrix.m13 = pos.y; matrix.m23 = pos.z;
	 * 
	 * Vector3f rot = new Vector3f(axis);
	 * 
	 * Matrix4f.rotation(rotation, rot, matrix);
	 * 
	 * return new Vector3f(matrix.m03, matrix.m13, matrix.m23); }
	 */

	/**
	 * Returns the length squared of this vector
	 */
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Set the components of this vector to be the component-wise maximum of this
	 * and the other vector.
	 *
	 * @param v the other vector
	 * @return this
	 */
	public Vector3f max(Vector3f v) {
		this.x = Math.max(x, v.x);
		this.y = Math.max(y, v.y);
		this.z = Math.max(z, v.z);
		return this;
	}

	/**
	 * Set the components of this vector to be the component-wise minimum of this
	 * and the other vector.
	 *
	 * @param v the other vector
	 * @return this
	 */
	public Vector3f min(Vector3f v) {
		this.x = Math.min(x, v.x);
		this.y = Math.min(y, v.y);
		this.z = Math.min(z, v.z);
		return this;
	}

	/**
	 * Multiply this Vector3f by the given scalar value.
	 * 
	 * @param scalar
	 * @return this
	 */
	public Vector3f mul(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * Multiply this Vector3f by the given rotation matrix mat.
	 * 
	 * @param mat
	 * @return this
	 */
	public Vector3f mul(Matrix3f mat) {
		mul(this, mat, this);
		return this;
	}

	/**
	 * Multiply this Vector3f by the given rotation matrix mat and store the result
	 * in <code>dest</code>.
	 * 
	 * @param mat
	 * @param dest
	 * @return this
	 */
	public Vector3f mul(Matrix3f mat, Vector3f dest) {
		mul(this, mat, dest);
		return this;
	}

	/**
	 * Multiply this Vector3f by the given matrix <code>mat</code>.
	 * 
	 * @param mat
	 * @return this
	 */
	public Vector3f mul(Matrix4f mat) {
		mul(this, mat, this);
		return this;
	}

	/**
	 * Multiply this Vector3f by the given matrix <code>mat</code> and store the
	 * result in <code>dest</code>.
	 * 
	 * @param mat
	 * @param dest
	 * @return this
	 */
	public Vector3f mul(Matrix4f mat, Vector3f dest) {
		mul(this, mat, dest);
		return this;
	}

	/**
	 * Multiply this Vector3f by another Vector3f
	 * 
	 * @param v
	 * @return this
	 */
	public Vector3f mul(Vector3f v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}

	/**
	 * Negate this vector.
	 * 
	 * @return this
	 */
	public Vector3f negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Normalize this vector.
	 * 
	 * @return this
	 */
	public Vector3f normalize() {
		final float d = length();
		x /= d;
		y /= d;
		z /= d;
		return this;
	}

	public Vector3f perpindicular() {
		return Vector3f.cross(this, new Vector3f(0, 1, 0));
	}

	public Vector3f randomize(float f) {
		x += (-f / 2 + Math.random()) * f;
		y += (-f / 2 + Math.random()) * f;
		z += (-f / 2 + Math.random()) * f;
		return this;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}

	public void rotate(Vector3f rot) {
		rotate(Vector3f.X_AXIS, rot.x);
		rotate(Vector3f.Y_AXIS, rot.y);
		rotate(Vector3f.Z_AXIS, rot.z);
	}

	public void rotate(Vector3f axis, float thetaRadians) {
		final float sinTh = (float) Math.sin(thetaRadians);
		final float cosTh = (float) Math.cos(thetaRadians);
		final float dp = dot(axis);

		x += axis.x * dp * (1f - cosTh) + x * cosTh + (-axis.z * y + axis.y * z) * sinTh;
		y += axis.y * dp * (1f - cosTh) + y * cosTh + (axis.z * x - axis.x * z) * sinTh;
		z += axis.z * dp * (1f - cosTh) + z * cosTh + (-axis.y * x + axis.x * y) * sinTh;
	}

	public Vector3f scale(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}

	/**
	 * Sets the x, y and z attributes to the supplied float values
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this
	 */
	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Set the x, y and z attributes to match the supplied vector.
	 * 
	 * @param v
	 * @return this
	 */
	public Vector3f set(Vector3f v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public void setLength(float f) {
		normalize();
		mul(f);
	}

	public Vector3f sub(float _x, float _y, float _z) {
		x -= _x;
		y -= _y;
		z -= _z;
		return this;
	}

	/**
	 * Subtracts the supplied vector from this one
	 * 
	 * @param v
	 * @return this
	 */
	public Vector3f sub(Vector3f v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	public float[] toFloats() {
		return new float[] { x, y, z };
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + "}";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
	}

	/**
	 * Set all components to zero.
	 * 
	 * @return this
	 */
	public Vector3f zero() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		return this;
	}

	/*
	 * public void rotate(float rx, int i, int j, int k) { double x, y, z; double u,
	 * v, w; x=vec.getX();y=vec.getY();z=vec.getZ();
	 * u=axis.getX();v=axis.getY();w=axis.getZ(); double xPrime = u*(u*x + v*y +
	 * w*z)*(1d - Math.cos(theta)) + x*Math.cos(theta) + (-w*y +
	 * v*z)*Math.sin(theta); double yPrime = v*(u*x + v*y + w*z)*(1d -
	 * Math.cos(theta)) + y*Math.cos(theta) + (w*x - u*z)*Math.sin(theta); double
	 * zPrime = w*(u*x + v*y + w*z)*(1d - Math.cos(theta)) + z*Math.cos(theta) +
	 * (-v*x + u*y)*Math.sin(theta); return new Vector(xPrime, yPrime, zPrime); }
	 */
}
