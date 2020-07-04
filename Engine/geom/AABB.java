package geom;

import org.joml.Vector3f;

import dev.Console;

public class AABB {
	private Vector3f center;
	private Vector3f bounds;
	private Vector3f min, max;

	public AABB(Vector3f center, Vector3f bounds) {
		this.center = center;
		this.bounds = bounds;
		calcMinMax();
	}

	public AABB(float x, float y, float z, float wid, float hei, float len) {
		this(new Vector3f(x, y, z), new Vector3f(wid, hei, len));
	}

	public void setCenter(float x, float y, float z) {
		this.center.set(x,y,z);
		calcMinMax();
	}
	
	public void setCenter(Vector3f vec) {
		this.center.set(vec);
		calcMinMax();
	}
	
	public void setBounds(float w, float h, float l) {
		this.bounds.set(w, h, l);
		calcMinMax();
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
	
	private void calcMinMax() {
		this.min = Vector3f.sub(center, bounds);
		this.max = Vector3f.add(center, bounds);
	}
	
	public void setMinMax(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
		this.min.set(minx,miny,minz);
		this.max.set(maxx,maxy,maxz);
		
		center.set((maxx+minx)/2f, (maxy+miny)/2f, (maxy+miny)/2f);
		bounds.set((maxx-minx)/2f, (maxy-miny)/2f, (maxy-miny)/2f);
	}
	
	public Vector3f getMin() {
		return min;
	}
	
	public Vector3f getMax() {
		return max;
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
	
	public Manifold collide(AABB other) {
		Manifold manifold = new Manifold();
		
		Vector3f omin = other.getMin();
		Vector3f omax = other.getMax();

		// X
		if (!axisTest(Vector3f.X_AXIS, min.x, max.x, omin.x, omax.x, manifold)) {
			return null;
		}

		// Yy
		if (!axisTest(Vector3f.Y_AXIS, min.y, max.y, omin.y, omax.y, manifold)) {
			return null;
		}

		// Z
		if (!axisTest(Vector3f.Z_AXIS, min.z, max.z, omin.z, omax.z, manifold)) {
			return null;
		}

		// Calculate Minimum Translation Vector (MTV) [normal * penetration]
		manifold.getAxis().normalize();
		
		// Multiply the penetration depth by itself plus a small increment
		// When the penetration is resolved using the MTV, it will no longer intersect
		manifold.setDepth((float)Math.sqrt(manifold.getDepth()) * 1.001f);
		
		return manifold;
	}
	
	public boolean intersects(AABB aabb) {
		if ( Math.abs(getCenter().x - aabb.getCenter().x) > (getBounds().x + aabb.getBounds().x) ) return false;
	    if ( Math.abs(getCenter().y - aabb.getCenter().y) > (getBounds().y + aabb.getBounds().y) ) return false;
	    if ( Math.abs(getCenter().z - aabb.getCenter().z) > (getBounds().z + aabb.getBounds().z) ) return false;
	 
	    // We have an overlap
	    return true;
	}

    private static boolean axisTest(Vector3f axis, float minA, float maxA, float minB, float maxB, Manifold manifold) {
        float axisLengthSquared = Vector3f.dot(axis, axis);

        if (axisLengthSquared < .0001f) {
            return true;
        }

        // Overlap ranges
        float d0 = (maxB - minA);   // Left side
        float d1 = (maxA - minB);   // Right side

        // No overlap, return
        if (d0 <= 0.0f || d1 <= 0.0f) {
            return false;
        }

        // Determine which side we overlap
        float overlap = (d0 < d1) ? d0 : -d1;

        // The mtd vector for that axis
        Vector3f sep = Vector3f.mul(axis, (overlap / axisLengthSquared));

        float sepLengthSquared = Vector3f.dot(sep, sep);

       if (sepLengthSquared < manifold.getDepth()) {
           manifold.setDepth(sepLengthSquared);
           manifold.setAxis(sep);
        }

        return true;
    }
	
	private static boolean axisTest(float rad, float p0, float p1) {
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

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false; // X
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false; // Y
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
			return false; // Z

		// EDGE 1
		Vector3f e1 = Vector3f.sub(v2, v1);
		ea = e1.abs();
		e_v0 = Vector3f.cross(e1, v0);
		e_v1 = Vector3f.cross(e1, v1);
		e_v2 = Vector3f.cross(e1, v2);

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v2.x))
			return false;
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v2.y))
			return false;
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v0.z, e_v1.z))
			return false;

		// EDGE 2
		Vector3f e2 = Vector3f.sub(v0, v2);
		ea = e2.abs();
		e_v0 = Vector3f.cross(e2, v0);
		e_v1 = Vector3f.cross(e2, v1);
		e_v2 = Vector3f.cross(e2, v2);

		if (axisTest(ea.z * bounds.y + ea.y * bounds.z, e_v0.x, e_v1.x))
			return false;
		
		if (axisTest(ea.z * bounds.x + ea.x * bounds.z, e_v0.y, e_v1.y))
			return false;
		if (axisTest(ea.y * bounds.x + ea.x * bounds.y, e_v1.z, e_v2.z))
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
}