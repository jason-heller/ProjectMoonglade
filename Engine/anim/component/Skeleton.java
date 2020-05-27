package anim.component;

public class Skeleton {
	private final int numJoints;
	private final Joint rootJoint;

	public Skeleton(int numJoints, Joint rootJoint) {
		this.numJoints = numJoints;
		this.rootJoint = rootJoint;
	}

	public int getNumJoints() {
		return numJoints;
	}

	public Joint getRootJoint() {
		return rootJoint;
	}
}
