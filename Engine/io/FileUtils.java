package io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class FileUtils {
	private static int readInd;

	public static boolean[] getFlags(byte b) {
		final boolean[] flags = new boolean[8];
		byte j = 1;
		for (int i = 0; i < 8; i++) {
			flags[i] = (b & j) != 0;
			j *= 2;
		}

		return flags;
	}

	public static InputStream getInputStream(Class<?> c, String path) {
		return c.getResourceAsStream("/" + path);
	}

	public static InputStream getInputStream(String path) {
		return getInputStream(Class.class, path);
	}

	public static BufferedReader getReader(Class<?> c, String path) {
		try {
			final InputStreamReader isr = new InputStreamReader(getInputStream(c, path));
			final BufferedReader reader = new BufferedReader(isr);
			return reader;
		} catch (final Exception e) {
			System.err.println("Failed to reader for " + path);
			throw e;
		}
	}

	public static BufferedReader getReader(String path) throws Exception {
		return getReader(Class.class, path);
	}

	public static byte readByte(byte[] data) {
		return data[readInd++];
	}

	public static int readFloat(byte[] data) {
		return data[readInd++];
	}

	public static int readInt(byte[] data) {
		return data[readInd++];
	}

	public static Matrix4f readMatrix4f(DataInputStream is) throws IOException {
		final float[] matrixArray = new float[16];
		for (int m = 0; m < 16; m++) {
			matrixArray[m] = is.readFloat();
		}

		final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		buffer.clear();
		buffer.put(matrixArray);
		buffer.flip();
		final Matrix4f matrix = new Matrix4f();
		matrix.set(buffer);
		return matrix;
	}

	public static String readString(DataInputStream in) throws IOException {
		String str = "";
		final byte len = in.readByte();
		for (int i = 0; i < len; i++) {
			str += in.readChar();
		}

		return str;

	}

	public static Vector3f readVec3(DataInputStream in) throws IOException {
		return new Vector3f(in.readFloat(), in.readFloat(), in.readFloat());
	}

	public static void startReading() {
		readInd = 0;
	}
}
