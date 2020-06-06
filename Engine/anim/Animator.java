package anim;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import anim.component.Joint;
import anim.component.JointTransform;
import anim.component.Keyframe;
import anim.component.Skeleton;
import anim.render.AnimationControl;
import core.Resources;
import core.res.Model;
import gl.Window;
import scene.entity.Entity;
import util.MathUtil;

public class Animator {

	// skeleton
	private final Joint rootJoint;
	private final int jointCount;

	private Animation animation;
	private float animationTime = 0;
	private float targetFrame = -1;
	private boolean isPaused = false;
	private boolean isPlaying = false;

	private final Entity gfx;
	private boolean isLooping = false;

	private int startFrame = -1, endFrame = -1;

	public Animator(Model model, Entity gfx) {
		final Skeleton skelly = model.getSkeleton();
		this.gfx = gfx;
		this.rootJoint = skelly.getRootJoint();
		this.jointCount = skelly.getNumJoints();
		rootJoint.calcInverseBindTransform(new Matrix4f());
		AnimationControl.add(gfx);

	}

	private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
		jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
		for (final Joint childJoint : headJoint.children) {
			addJointsToArray(childJoint, jointMatrices);
		}
	}

	/**
	 * This is the method where the animator calculates and sets those all-
	 * important "joint transforms" that I talked about so much in the tutorial.
	 *
	 * This method applies the current pose to a given joint, and all of its
	 * descendants. It does this by getting the desired local-transform for the
	 * current joint, before applying it to the joint. Before applying the
	 * transformations it needs to be converted from local-space to model-space (so
	 * that they are relative to the model's origin, rather than relative to the
	 * parent joint). This can be done by multiplying the local-transform of the
	 * joint with the model-space transform of the parent joint.
	 *
	 * The same thing is then done to all the child joints.
	 *
	 * Finally the inverse of the joint's bind transform is multiplied with the
	 * model-space transform of the joint. This basically "subtracts" the joint's
	 * original bind (no animation applied) transform from the desired pose
	 * transform. The result of this is then the transform required to move the
	 * joint from its original model-space transform to it's desired model-space
	 * posed transform. This is the transform that needs to be loaded up to the
	 * vertex shader and used to transform the vertices into the current pose.
	 *
	 * @param currentPose     - a map of the local-space transforms for all the
	 *                        joints for the desired pose. The map is indexed by the
	 *                        name of the joint which the transform corresponds to.
	 * @param joint           - the current joint which the pose should be applied
	 *                        to.
	 * @param parentTransform - the desired model-space transform of the parent
	 *                        joint for the pose.
	 */
	private void applyPoseToJoints(Map<Byte, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
		final Matrix4f currentLocalTransform = currentPose.get(joint.index);
		final Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
		for (final Joint childJoint : joint.children) {
			applyPoseToJoints(currentPose, childJoint, currentTransform);
		}
		Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
		joint.setAnimationTransform(currentTransform);
	}

	/**
	 * This method returns the current animation pose of the entity. It returns the
	 * desired local-space transforms for all the joints in a map, indexed by the
	 * name of the joint that they correspond to.
	 *
	 * The pose is calculated based on the previous and next keyframes in the
	 * current animation. Each keyframe provides the desired pose at a certain time
	 * in the animation, so the animated pose for the current time can be calculated
	 * by interpolating between the previous and next keyframe.
	 *
	 * This method first finds the preious and next keyframe, calculates how far
	 * between the two the current animation is, and then calculated the pose for
	 * the current animation time by interpolating between the transforms at those
	 * keyframes.
	 *
	 * @return The current pose as a map of the desired local-space transforms for
	 *         all the joints. The transforms are indexed by the name ID of the
	 *         joint that they should be applied to.
	 */
	private Map<Byte, Matrix4f> calculatePose() {
		final Keyframe[] frames = getPreviousAndNextFrames();
		final float progression = calculateProgression(frames[0], frames[1]);
		return interpolatePoses(frames[0], frames[1], progression);
	}

	/**
	 * Calculates how far between the previous and next keyframe the current
	 * animation time is, and returns it as a value between 0 and 1.
	 *
	 * @param previousFrame - the previous keyframe in the animation.
	 * @param nextFrame     - the next keyframe in the animation.
	 * @return A number between 0 and 1 indicating how far between the two keyframes
	 *         the current animation time is.
	 */
	private float calculateProgression(Keyframe previousFrame, Keyframe nextFrame) {
		final float totalTime = nextFrame.getTime() - previousFrame.getTime();
		final float currentTime = animationTime - previousFrame.getTime();
		return currentTime / totalTime;
	}

	public void destroy() {
		AnimationControl.remove(gfx);
	}

	public Matrix4f[] getJointTransforms() {
		final Matrix4f[] jointMatrices = new Matrix4f[jointCount];
		addJointsToArray(rootJoint, jointMatrices);
		return jointMatrices;
	}

	/**
	 * Finds the previous keyframe in the animation and the next keyframe in the
	 * animation, and returns them in an array of length 2. If there is no previous
	 * frame (perhaps current animation time is 0.5 and the first keyframe is at
	 * time 1.5) then the first keyframe is used as both the previous and next
	 * keyframe. The last keyframe is used for both next and previous if there is no
	 * next keyframe.
	 *
	 * @return The previous and next keyframes, in an array which therefore will
	 *         always have a length of 2.
	 */
	private Keyframe[] getPreviousAndNextFrames() {
		final Keyframe[] frames = animation.getKeyframes();
		Keyframe prevFrame = frames[0];
		Keyframe nextFrame = frames[0];

		for (int i = 1; i < frames.length; i++) {
			nextFrame = frames[i];
			if (nextFrame.getTime() > animationTime) {
				break;
			}
			prevFrame = frames[i];
		}

		return new Keyframe[] { prevFrame, nextFrame };
	}

	public Joint getRootJoint() {
		return rootJoint;
	}

	public Matrix4f getRootTransform() {
		return rootJoint.getAnimatedTransform();
	}

	public Entity getVisibleObject() {
		return gfx;
	}

	/**
	 * Calculates all the local-space joint transforms for the desired current pose
	 * by interpolating between the transforms at the previous and next keyframes.
	 *
	 * @param prev        - the previous keyframe in the animation.
	 * @param next        - the next keyframe in the animation.
	 * @param progression - a number between 0 and 1 indicating how far between the
	 *                    previous and next keyframes the current animation time is.
	 * @return The local-space transforms for all the joints for the desired current
	 *         pose. They are returned in a map, indexed by the name of the joint to
	 *         which they should be applied.
	 */
	private Map<Byte, Matrix4f> interpolatePoses(Keyframe prev, Keyframe next, float progression) {
		final Map<Byte, Matrix4f> currentPose = new HashMap<Byte, Matrix4f>();
		for (final Byte jointId : prev.getTransforms().keySet()) {
			final JointTransform prevTransform = prev.getTransforms().get(jointId);
			final JointTransform nextTransform = next.getTransforms().get(jointId);
			final JointTransform currentTransform = JointTransform.lerp(prevTransform, nextTransform, progression);
			currentPose.put(jointId, currentTransform.getMatrix());
		}
		return currentPose;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void loop(String animation) {
		targetFrame = -1;
		isLooping = true;
		start(animation, false);
		isPlaying = true;
	}

	public void pause() {
		isPaused = true;
	}

	public void setTargetFrame(float targetFrame) {
		this.targetFrame = targetFrame;

	}

	public void start(String animName) {
		start(animName, true, -1, -1);
	}

	public void start(String animName, boolean restartIfPlaying) {
		start(animName, restartIfPlaying, -1, -1);
	}

	public void start(String animName, boolean restartIfPlaying, int startFrame, int endFrame) {
		if (!isPlaying || isPlaying && restartIfPlaying) {
			animationTime = 0;
			targetFrame = -1;
			this.animation = Resources.getAnimation(animName);
			isPlaying = true;

			if (startFrame != -1) {
				this.startFrame = startFrame;
				this.endFrame = endFrame;
				animationTime = animation.getKeyframes()[startFrame].getTime();
			}
		}
	}

	public void stop() {
		isPlaying = false;
		isPaused = false;
		isLooping = false;
	}

	public void unpause() {
		isPaused = false;
	}

	public void update() {
		if (animation != null) {
			if (!isPaused && isPlaying) {
				if (targetFrame == -1) {
					animationTime = animationTime + Window.deltaTime;

					if (animationTime > animation.getDuration()) {
						if (!isLooping) {
							stop();
						} else {
							animationTime -= animation.getDuration();
						}
					}

					if (startFrame != -1) {
						final float endTime = this.animation.getKeyframes()[endFrame].getTime();
						if (animationTime > endTime) {
							if (!isLooping) {
								stop();
							} else {
								animationTime -= endTime - this.animation.getKeyframes()[startFrame].getTime();
							}
						}
					}
				} else {
					animationTime = MathUtil.lerp(animationTime, targetFrame, .1f);
				}
			}
			final Map<Byte, Matrix4f> currentPose = calculatePose();
			applyPoseToJoints(currentPose, getRootJoint(), new Matrix4f());
		}
	}
}
