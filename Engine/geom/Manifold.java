package geom;

import org.joml.Vector3f;

public class Manifold {
	private Vector3f contact;
	private float depth;
	private Vector3f axis;
	
	public Manifold() {
		contact = null;
		depth = Float.MAX_VALUE;
		axis = new Vector3f();
	}

	public void setContact(Vector3f contact) {
		this.contact = contact;
	}

	public void setDepth(float depth) {
		this.depth = depth;
	}

	public void setAxis(Vector3f axis) {
		this.axis = axis;
	}

	public Vector3f getContact() {
		return contact;
	}

	public float getDepth() {
		return depth;
	}

	public Vector3f getAxis() {
		return axis;
	}
}
