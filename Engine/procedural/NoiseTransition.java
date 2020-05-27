package procedural;

import util.MathUtil;

public class NoiseTransition {
	public static float lowerBound = 1f;
	public static float upperBound = -1f;
	public static float transitionFactor = 0f;

	public static void setTransitionAttribs(float lower, float upper, float transition) {
		lowerBound = lower;
		upperBound = upper;
		transitionFactor = transition;
	}

	public static float blend(float n1, float n2) {
		float controlValue = n2;
		float alpha;
		float edgeFalloff = 0f;
		if (edgeFalloff > 0.0) {
			if (controlValue < (lowerBound - edgeFalloff)) {
				return n1;

			} else if (controlValue < (lowerBound + edgeFalloff)) {
				float lowerCurve = (lowerBound - edgeFalloff);
				float upperCurve = (lowerBound + edgeFalloff);
				alpha = cubic((controlValue - lowerCurve) / (upperCurve - lowerCurve));
				return MathUtil.lerp(n1, n2, alpha);

			} else if (controlValue < (upperBound - edgeFalloff)) {
				return n2;

			} else if (controlValue < (upperBound + edgeFalloff)) {
				float lowerCurve = (upperBound - edgeFalloff);
				float upperCurve = (upperBound + edgeFalloff);
				alpha = cubic((controlValue - lowerCurve) / (upperCurve - lowerCurve));
				return MathUtil.lerp(n2, n1, alpha);

			} else {
				return n1;
			}
		} else {
			if (controlValue < lowerBound || controlValue > upperBound) {
				return n1;
			} else {
				return n2;
			}
		}

	}

	private static float cubic(float a) {
		return (a * a * (3.0f - 2.0f * a));
	}
}
