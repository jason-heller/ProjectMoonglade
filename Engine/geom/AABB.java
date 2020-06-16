package geom;

import org.joml.Vector3f;

public class AABB {
	private Vector3f center;
	private Vector3f bounds;
	
	final Vector3f[] faces = new Vector3f[]
			{
				new Vector3f(-1,  0,  0),
				new Vector3f( 1,  0,  0),
				new Vector3f( 0, -1,  0),
				new Vector3f( 0,  1,  0),
				new Vector3f( 0,  0, -1),
				new Vector3f( 0,  0,  1),
			};

	public AABB(Vector3f center, Vector3f bounds) {
		this.center = center;
		this.bounds = bounds;
	}

	public AABB(float x, float y, float z, float wid, float hei, float len) {
		this.center = new Vector3f(x, y, z);
		this.bounds = new Vector3f(wid, hei, len);
	}

	public AABB(AABB aabb, int i, float f, int j) {
		this.bounds = aabb.bounds;
		this.center = new Vector3f(aabb.getX() + i, aabb.getY() + j, aabb.getZ() + j);
	}

	public void set(float x, float y, float z) {
		this.center.set(x,y,z);
	}
	
	public void set(Vector3f vec) {
		this.center = vec;
	}

	public float getX() {
		return center.x;
	}
	
	public float getY() {
		return center.y;
	}
	
	public float getZ() {
		return center.z;
	}
	
	public float getWidth() {
		return bounds.x;
	}
	
	public float getHeight() {
		return bounds.y;
	}
	
	public float getLength() {
		return bounds.z;
	}
	
	public Vector3f getCenter() {
		return center;
	}

	public Vector3f getBounds() {
		return bounds;
	}
	
	public String toString() {
		return center.toString() + " " + bounds.toString();
	}
	
	public float collide(Vector3f org, Vector3f dir) {
		// r.dir is unit direction vector of ray
		Vector3f dirfrac = new Vector3f();
		dirfrac.x = 1.0f / dir.x;
		dirfrac.y = 1.0f / dir.y;
		dirfrac.z = 1.0f / dir.z;
		// lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
		// r.org is origin of ray
		Vector3f lb = new Vector3f(getX()-getWidth(), getY()-getHeight(), getZ()-getLength());
		Vector3f rt = new Vector3f(getX()+getWidth(), getY()+getHeight(), getZ()+getLength());
		float t = Float.NaN;
		
		float t1 = (lb.x - org.x)*dirfrac.x;
		float t2 = (rt.x - org.x)*dirfrac.x;
		float t3 = (lb.y - org.y)*dirfrac.y;
		float t4 = (rt.y - org.y)*dirfrac.y;
		float t5 = (lb.z - org.z)*dirfrac.z;
		float t6 = (rt.z - org.z)*dirfrac.z;

		float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0) {
		    t = tmax;
		    return Float.NaN;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax) {
		    t = tmax;
		    return Float.NaN;
		}

		t = tmin;
		return t;
	}
	
	public Vector3f collide(AABB other) {
		if (!intersects(other))
			return null;

		float dx, dy, dz;
		if (center.x > other.center.x) {
			dx = (other.center.x + other.bounds.x) - (center.x - bounds.x);
		} else {
			dx = (other.center.x - other.bounds.x) - (center.x + bounds.x);
		}

		if (center.y < other.center.y) {
			dy = (other.center.y + other.bounds.y) - (center.y - bounds.y);
		} else {
			dy = (other.center.y - other.bounds.y) - (center.y + bounds.y);
		}

		if (center.z > other.center.z) {
			dz = (other.center.z + other.bounds.z) - (center.z - bounds.z);
		} else {
			dz = (other.center.z - other.bounds.z) - (center.z + bounds.z);
		}

		return new Vector3f(dx, dy, dz);
	}

	public boolean intersects(AABB aabb) {
		if ( Math.abs(getCenter().x - aabb.getCenter().x) > (getBounds().x + aabb.getBounds().x) ) return false;
	    if ( Math.abs(getCenter().y - aabb.getCenter().y) > (getBounds().y + aabb.getBounds().y) ) return false;
	    if ( Math.abs(getCenter().z - aabb.getCenter().z) > (getBounds().z + aabb.getBounds().z) ) return false;
	 
	    // We have an overlap
	    return true;
	}
	
	/*public Manifold collide(AABB box) {
		Vector3f mina, maxa, minb, maxb;
		Vector3f normal = null;
		float depth = 0;
		//int face = 0;
		
		mina = new Vector3f(getX()-getWidth(), getY()-getHeight(), getZ()-getLength());
		maxa = new Vector3f(getX()+getWidth(), getY()+getHeight(), getZ()+getLength());
		minb = new Vector3f(box.getX()-box.getWidth(), box.getY()-box.getHeight(), box.getZ()-box.getLength());
		maxb = new Vector3f(box.getX()+box.getWidth(), box.getY()+box.getHeight(), box.getZ()+box.getLength());
		
		float distances[] = new float[]
		{
				maxb.x - mina.x,
				maxa.x - minb.x,
				maxb.y - mina.y,
				maxa.y - minb.y,
				maxb.z - mina.z,
				maxa.z - minb.z
		};
		
		for(int i = 0; i < 6; i++) {
			if(distances[i] < 0.0f) 
				return null;

			if((i == 0) || (distances[i] < depth)) {
				//face = i;
				normal = faces[i];
				depth = distances[i];
				
			}
		}
		
		return new Manifold(normal, depth);
	}*/
	
	private static boolean AXISTEST(float rad, float p0, float p1) {
		return (Math.min(p0, p1) > rad || Math.max(p0, p1) < -rad);
	}

	private static boolean directionTest(float a, float b, float c, float bounds) {
		return (Math.min(Math.min(a, b), c) > bounds || Math.max(
				Math.max(a, b), c) < -bounds);
	}

	/**
	 * Checks an overlap between a triangle's plane and an AABB.
	 * 
	 * @param normal
	 *            - the triangle's normal.
	 * @param d
	 *            - plane equation (normal.x + d = 0)
	 * @param bounds
	 *            - the bounds of the AABB
	 *            
	 * @returns true is overlapping, false otherwise
	 */
	public static boolean planeBoxOverlap(Vector3f normal, float d,
			Vector3f bounds) {
		// vmin is the AABB's bounds, with some axis flipped depending on the direction of the normal
		Vector3f vmin = new Vector3f((normal.x > 0.0) ? -bounds.x : +bounds.x,
				(normal.y > 0.0) ? -bounds.y : +bounds.y,
				(normal.z > 0.0) ? -bounds.z : +bounds.z);

		if (Vector3f.dot(normal, vmin) + d > 0.0)
			return false;
		Vector3f vmax = Vector3f.negate(vmin);
		if (Vector3f.dot(normal, vmax) + d >= 0.0)
			return true;
		return false;
	}

	public boolean collide(Polygon tri) {
		// Use separating axis theorem to test overlap between triangle and box.
		// Need to test for overlap in these directions:
		// 1) the {x,y,z}-directions (actually, since we use the AABB of the
		// triangle we do not even need to test these)
		// 2) normal of the triangle
		// 3) crossproduct(edge from triangle, {x,y,z}-direction). This gives
		// 3x3=9 more tests.

		// Move everything so that the boxcenter is in (0,0,0).
		Vector3f v0 = Vector3f.sub(tri.p1, center);
		Vector3f v1 = Vector3f.sub(tri.p2, center);
		Vector3f v2 = Vector3f.sub(tri.p3, center);

		// Bullet 3:
		// Test the 9 tests first (this was faster).

		Vector3f ea = new Vector3f();
		Vector3f e_v0 = new Vector3f();
		Vector3f e_v1 = new Vector3f();
		Vector3f e_v2 = new Vector3f();
		
		// EDGE 0
		Vector3f e0 = Vector3f.sub(v1, v0);
		ea = e0.abs();
		e_v0 = Vector3f.cross(e0, v0);
		e_v1 = Vector3f.cross(e0, v1);
		e_v2 = Vector3f.cross(e0, v2);

		if (AXISTEST(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false; // X
		if (AXISTEST(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false; // Y
		if (AXISTEST(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
			return false; // Z

		// EDGE 1
		Vector3f e1 = Vector3f.sub(v2, v1);
		ea = e1.abs();
		e_v0 = Vector3f.cross(e1, v0);
		e_v1 = Vector3f.cross(e1, v1);
		e_v2 = Vector3f.cross(e1, v2);

		if (AXISTEST(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false;
		if (AXISTEST(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false;
		if (AXISTEST(ea.y * bounds.x + ea.x * bounds.y, e_v0.z, e_v1.z))
			return false;

		// EDGE 2
		Vector3f e2 = Vector3f.sub(v0, v2);
		ea = e2.abs();
		e_v0 = Vector3f.cross(e2, v0);
		e_v1 = Vector3f.cross(e2, v1);
		e_v2 = Vector3f.cross(e2, v2);

		if (AXISTEST(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v1.x))
			return false;
		
		if (AXISTEST(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v1.y))
			return false;
		if (AXISTEST(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
			return false;
		
		// Bullet 1:
		// First test overlap in the {x,y,z}-directions.
		// Find min, max of the triangle each direction, and test for overlap in
		// that
		// direction -- this is equivalent to testing a minimal AABB around the
		// triangle against the AABB.
		if (directionTest(v0.x, v1.x, v2.x, bounds.x))
			return false; // Test in X-direction.
		
		if (directionTest(v0.y, v1.y, v2.y, bounds.y))
			return false; // Test in Y-direction.
		
		if (directionTest(v0.z, v1.z, v2.z, bounds.z))
			return false; // Test in Z-direction.
		
		// Bullet 2:
		// Test if the box intersects the plane of the triangle. Compute plane
		// equation of triangle: normal*x+d=0.
		Vector3f normal = Vector3f.cross(e0, e1);
		float d = -Vector3f.dot(normal, v0); // plane eq: normal.x+d=0
		if (!planeBoxOverlap(normal, d, bounds))
			return false;
		
		
		return true; // box and triangle overlaps
	}

	public boolean collide(Vector3f point) {
		return (point.x >= center.x-bounds.x && point.x <= center.x+bounds.x) &&
		         (point.y >= center.y-bounds.y && point.y <= center.y+bounds.y) &&
		         (point.z >= center.z-bounds.z && point.z <= center.z+bounds.z);
	}
	
	/*
	// This is a direct port of Christer Ericson's code yo java.
	// c is a [x, y, z] vector containing the center of the box
	// e is a [x, y, z] vector containing half the dimensions of the box
	// _v is a [[x0, y0, z0],[x1, y1, z1],[x2, y2, z2]] vector containing the
	// vertices of the triangle
	// translation is a [x, y, z] vector and will be set to the least
	// translation vector that solves the collision
	// this code implements the separating axis theorem for a box and a triangle
	// and was written with help from the book "Real-Time Collision Detection"
	// by Christer Ericson
	// the main difference from the code presented in the mentioned book is the
	// normalization of separating axes
	// and calculation of the least translation vector
	public Vector3f triangleCollision(Triangle t) {
		float[] c = new float[] { center.x, center.y, center.z };
		float[] e = new float[] { bounds.x, bounds.y, bounds.z };
		float[][] _v = new float[][] { { t.p1.x, t.p1.y, t.p1.z },
				{ t.p2.x, t.p2.y, t.p2.z }, { t.p3.x, t.p3.y, t.p3.z } };
		float distance = 99999;
		Vector3f axis = new Vector3f(0,0,0);
		float[] v0 = new float[] { _v[0][0] - c[0], _v[0][1] - c[1],
				_v[0][2] - c[2] };
		float[] v1 = new float[] { _v[1][0] - c[0], _v[1][1] - c[1],
				_v[1][2] - c[2] };
		float[] v2 = new float[] { _v[2][0] - c[0], _v[2][1] - c[1],
				_v[2][2] - c[2] };
		float[] f0 = new float[] { v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2] };
		float[] f1 = new float[] { v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2] };
		float[] f2 = new float[] { v0[0] - v2[0], v0[1] - v2[1], v0[2] - v2[2] };
		
		
		
		float a00l = (float) Math.sqrt(f0[2] * f0[2] + f0[1] * f0[1]);
		if (a00l == 0) return null;
		float a00y = -f0[2] / a00l;
		float a00z = f0[1] / a00l;
		float a00r = e[1] * Math.abs(a00y) + e[2] * Math.abs(a00z);
		float a00p0 = v0[1] * a00y + v0[2] * a00z;
		float a00p1 = v1[1] * a00y + v1[2] * a00z;
		float a00p2 = v2[1] * a00y + v2[2] * a00z;
		float a00min = Math.min(a00p0, Math.min(a00p1, a00p2));
		float a00max = Math.max(a00p0, Math.max(a00p1, a00p2));
		if (a00min > a00r)
			return null;
		if (a00max < -a00r)
			return null;
		else {
			if (a00min < -a00r) {
				distance = -(a00max + a00r);
			} else {
				distance = a00r - a00min;
			}
			axis = new Vector3f ( 0, a00y, a00z );
		}

		
		float a01l = (float) Math.sqrt(f1[2] * f1[2] + f1[1] * f1[1]);
		float a01y = -f1[2] / a01l;
		float a01z = f1[1] / a01l;
		float a01r = e[1] * Math.abs(a01y) + e[2] * Math.abs(a01z);
		float a01p0 = v0[1] * a01y + v0[2] * a01z;
		float a01p1 = v1[1] * a01y + v1[2] * a01z;
		float a01p2 = v2[1] * a01y + v2[2] * a01z;
		float a01min = Math.min(a01p0, Math.min(a01p1, a01p2));
		float a01max = Math.max(a01p0, Math.max(a01p1, a01p2));
		if (a01min > a01r)
			return null;
		if (a01max < -a01r)
			return null;
		else {
			float newDistance;
			if (a01min < -a01r) {
				newDistance = -(a01max + a01r);
			} else {
				newDistance = a01r - a01min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, a01y, a01z );
			}
		}
		float a02l = (float) Math.sqrt(f2[2] * f2[2] + f2[1] * f2[1]);
		float a02y = -f2[2] / a02l;
		float a02z = f2[1] / a02l;
		float a02r = e[1] * Math.abs(a02y) + e[2] * Math.abs(a02z);
		float a02p0 = v0[1] * a02y + v0[2] * a02z;
		float a02p1 = v1[1] * a02y + v1[2] * a02z;
		float a02p2 = v2[1] * a02y + v2[2] * a02z;
		float a02min = Math.min(a02p0, Math.min(a02p1, a02p2));
		float a02max = Math.max(a02p0, Math.max(a02p1, a02p2));
		if (a02min > a02r)
			return null;
		if (a02max < -a02r)
			return null;
		else {
			float newDistance;
			if (a02min < -a02r) {
				newDistance = -(a02max + a02r);
			} else {
				newDistance = a02r - a02min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, a02y, a02z );
			}
		}
		float a10l = (float) Math.sqrt(f0[2] * f0[2] + f0[0] * f0[0]);
		float a10x = f0[2] / a10l;
		float a10z = -f0[0] / a10l;
		float a10r = e[0] * Math.abs(a10x) + e[2] * Math.abs(a10z);
		float a10p0 = v0[0] * a10x + v0[2] * a10z;
		float a10p1 = v1[0] * a10x + v1[2] * a10z;
		float a10p2 = v2[0] * a10x + v2[2] * a10z;
		float a10min = Math.min(a10p0, Math.min(a10p1, a10p2));
		float a10max = Math.max(a10p0, Math.max(a10p1, a10p2));
		if (a10min > a10r)
			return null;
		if (a10max < -a10r)
			return null;
		else {
			float newDistance;
			if (a10min < -a10r) {
				newDistance = -(a10max + a10r);
			} else {
				newDistance = a10r - a10min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a10x, 0, a10z );
			}
		}
		float a11l = (float) Math.sqrt(f1[2] * f1[2] + f1[0] * f1[0]);
		float a11x = f1[2] / a11l;
		float a11z = -f1[0] / a11l;
		float a11r = e[0] * Math.abs(a11x) + e[2] * Math.abs(a11z);
		float a11p0 = v0[0] * a11x + v0[2] * a11z;
		float a11p1 = v1[0] * a11x + v1[2] * a11z;
		float a11p2 = v2[0] * a11x + v2[2] * a11z;
		float a11min = Math.min(a11p0, Math.min(a11p1, a11p2));
		float a11max = Math.max(a11p0, Math.max(a11p1, a11p2));
		if (a11min > a11r)
			return null;
		if (a11max < -a11r)
			return null;
		else {
			float newDistance;
			if (a11min < -a11r) {
				newDistance = -(a11max + a11r);
			} else {
				newDistance = a11r - a11min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a11x, 0, a11z );
			}
		}
		float a12l = (float) Math.sqrt(f2[2] * f2[2] + f2[0] * f2[0]);
		float a12x = f2[2] / a12l;
		float a12z = -f2[0] / a12l;
		float a12r = e[0] * Math.abs(a12x) + e[2] * Math.abs(a12z);
		float a12p0 = v0[0] * a12x + v0[2] * a12z;
		float a12p1 = v1[0] * a12x + v1[2] * a12z;
		float a12p2 = v2[0] * a12x + v2[2] * a12z;
		float a12min = Math.min(a12p0, Math.min(a12p1, a12p2));
		float a12max = Math.max(a12p0, Math.max(a12p1, a12p2));
		if (a12min > a12r)
			return null;
		if (a12max < -a12r)
			return null;
		else {
			float newDistance;
			if (a12min < -a12r) {
				newDistance = -(a12max + a12r);
			} else {
				newDistance = a12r - a12min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a12x, 0, a12z );
			}
		}
		float a20l = (float) Math.sqrt(f0[1] * f0[1] + f0[0] * f0[0]);
		float a20x = -f0[1] / a20l;
		float a20y = f0[0] / a20l;
		float a20r = e[0] * Math.abs(a20x) + e[1] * Math.abs(a20y);
		float a20p0 = v0[0] * a20x + v0[1] * a20y;
		float a20p1 = v1[0] * a20x + v1[1] * a20y;
		float a20p2 = v2[0] * a20x + v2[1] * a20y;
		float a20min = Math.min(a20p0, Math.min(a20p1, a20p2));
		float a20max = Math.max(a20p0, Math.max(a20p1, a20p2));
		if (a20min > a20r)
			return null;
		if (a20max < -a20r)
			return null;
		else {
			float newDistance;
			if (a20min < -a20r) {
				newDistance = -(a20max + a20r);
			} else {
				newDistance = a20r - a20min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a20x, a20y, 0 );
			}
		}
		float a21l = (float) Math.sqrt(f1[1] * f1[1] + f1[0] * f1[0]);
		float a21x = -f1[1] / a21l;
		float a21y = f1[0] / a21l;
		float a21r = e[0] * Math.abs(a21x) + e[1] * Math.abs(a21y);
		float a21p0 = v0[0] * a21x + v0[1] * a21y;
		float a21p1 = v1[0] * a21x + v1[1] * a21y;
		float a21p2 = v2[0] * a21x + v2[1] * a21y;
		float a21min = Math.min(a21p0, Math.min(a21p1, a21p2));
		float a21max = Math.max(a21p0, Math.max(a21p1, a21p2));
		if (a21min > a21r)
			return null;
		if (a21max < -a21r)
			return null;
		else {
			float newDistance;
			if (a21min < -a21r) {
				newDistance = -(a21max + a21r);
			} else {
				newDistance = a21r - a21min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a21x, a21y, 0 );
			}
		}
		float a22l = (float) Math.sqrt(f2[1] * f2[1] + f2[0] * f2[0]);
		float a22x = -f2[1] / a22l;
		float a22y = f2[0] / a22l;
		float a22r = e[0] * Math.abs(a22x) + e[1] * Math.abs(a22y);
		float a22p0 = v0[0] * a22x + v0[1] * a22y;
		float a22p1 = v1[0] * a22x + v1[1] * a22y;
		float a22p2 = v2[0] * a22x + v2[1] * a22y;
		float a22min = Math.min(a22p0, Math.min(a22p1, a22p2));
		float a22max = Math.max(a22p0, Math.max(a22p1, a22p2));
		if (a22min > a22r)
			return null;
		if (a22max < -a22r)
			return null;
		else {
			float newDistance;
			if (a22min < -a22r) {
				newDistance = -(a22max + a22r);
			} else {
				newDistance = a22r - a22min;
			}
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( a22x, a22y, 0 );
			}
		}
		
		//System.out.println(axis[0] + "|" + axis[1] + "|" + axis[2]);
		float b0max = Math.max(v0[0], Math.max(v1[0], v2[0]));
		if (b0max < -e[0])
			return null;
		else {
			float newDistance = -(e[0] + b0max);
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 1, 0, 0 );
			}
		}
		float b0min = Math.min(v0[0], Math.min(v1[0], v2[0]));
		if (b0min > e[0])
			return null;
		else {
			float newDistance = b0min - e[0];
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( -1, 0, 0 );
			}
		}
		float b1max = Math.max(v0[1], Math.max(v1[1], v2[1]));
		if (b1max < -e[1])
			return null;
		else {
			float newDistance = -(e[1] + b1max);
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, 1, 0 );
			}
		}
		float b1min = Math.min(v0[1], Math.min(v1[1], v2[1]));
		if (b1min > e[1])
			return null;
		else {
			float newDistance = b1min - e[1];
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, -1, 0 );
			}
		}
		float b2max = Math.max(v0[2], Math.max(v1[2], v2[2]));
		if (b2max < -e[2])
			return null;
		else {
			float newDistance = -(e[2] + b2max);
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, 0, 1 );
			}
		}
		float b2min = Math.min(v0[2], Math.min(v1[2], v2[2]));
		if (b2min > e[2])
			return null;
		else {
			float newDistance = b2min - e[2];
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( 0, 0, -1 );
			}
		}
		float[] pn = new float[] { -f0[2] * f1[1] + f0[1] * f1[2],
				f0[2] * f1[0] - f0[0] * f1[2], -f0[1] * f1[0] + f0[0] * f1[1] };
		float pnl = (float) Math.sqrt(pn[0] * pn[0] + pn[1] * pn[1] + pn[2]	* pn[2]);
		pn[0] /= pnl;
		pn[1] /= pnl;
		pn[2] /= pnl;
		float pd = v0[0] * pn[0] + v0[1] * pn[1] + v0[2] * pn[2];
		float pr = e[0] * Math.abs(pn[0]) + e[1] * Math.abs(pn[1]) + e[2]
				* Math.abs(pn[2]);
		if (Math.abs(pd) > pr)
			return null;
		else {
			float newDistance = -pr - pd;
			if (Math.abs(newDistance) < Math.abs(distance)) {
				distance = newDistance;
				axis = new Vector3f ( pn[0], pn[1], pn[2] );
			}
		}
		distance *= -1;
		Vector3f translation = new Vector3f(axis.x * distance, axis.y * distance, axis.z * distance);
		//System.out.println(axis[0] + "|" + axis[1] + "|" + axis[2]);
		
		return new Vector3f(translation);
	}
	 */
}