package io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

import anim.Animation;
import anim.component.JointTransform;
import anim.component.Keyframe;
import core.Resources;

public class AniFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports

	private static JointTransform createJointTransform(Matrix4f mat) {
		final Vector3f translation = new Vector3f(mat.m30, mat.m31, mat.m32);
		final Quaternion rotation = Quaternion.fromMatrix(mat);
		return new JointTransform(translation, rotation);
	}

	public static void extractAnimationData(String key, DataInputStream is) throws IOException {
		final short numKeyframes = is.readShort();
		final float animationDuration = is.readFloat();

		final Keyframe[] keyframes = new Keyframe[numKeyframes];

		for (int i = 0; i < numKeyframes; i++) {

			final float time = is.readFloat();
			final byte numTransforms = is.readByte();
			final Map<Byte, JointTransform> jointTransforms = new HashMap<Byte, JointTransform>();

			for (int j = 0; j < numTransforms; j++) {
				final byte index = is.readByte();
				final Matrix4f transform = FileUtils.readMatrix4f(is);

				jointTransforms.put(index, createJointTransform(transform));
			}

			keyframes[i] = new Keyframe(time, jointTransforms);
		}

		Resources.addAnimation(key, new Animation(animationDuration, keyframes));
	}

	public static void readAniFile(String key, String path) {
		DataInputStream is = null;
		try {
			final String fullUrl = "src/res/" + path;
			is = new DataInputStream(new FileInputStream(fullUrl));

			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION) {
				System.out.println("NOT RIGHT VERSION");
				return;
			}

			if (!fileExtName.equals("ANI")) {
				System.out.println("NOT AN ANI FILE");
				return;
			}

			final byte numAnimations = is.readByte();

			for (int i = 0; i < numAnimations; i++) {
				extractAnimationData(key, is);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
}
