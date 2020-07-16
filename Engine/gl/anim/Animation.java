package gl.anim;

import gl.anim.component.Keyframe;

public class Animation {

	private final float duration; // seconds
	private final Keyframe[] frames;

	public Animation(float duration, Keyframe[] frames) {
		this.frames = frames;
		this.duration = duration;
	}

	public float getDuration() {
		return duration;
	}

	public Keyframe[] getKeyframes() {
		return frames;
	}
}
