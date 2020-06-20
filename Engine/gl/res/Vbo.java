package gl.res;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class Vbo {

	public static Vbo create(int type) {
		final int id = GL15.glGenBuffers();
		return new Vbo(id, type);
	}

	private final int id;

	private final int type;

	private Vbo(int vboId, int type) {
		this.id = vboId;
		this.type = type;
	}

	public void bind() {
		GL15.glBindBuffer(type, id);
	}

	public void delete() {
		GL15.glDeleteBuffers(id);
	}

	public void storeData(byte[] data) {
		final ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}

	public void storeData(ByteBuffer data) {
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}

	public void storeData(float[] data) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}

	public void storeData(FloatBuffer data) {
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}

	public void storeData(int[] data) {
		final IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}

	public void storeData(IntBuffer data) {
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}

	public void storeDynamicData(float[] data) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferData(type, buffer, GL15.GL_DYNAMIC_DRAW);
	}
	
	public void storeStreamedData(float[] data) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferData(type, buffer, GL15.GL_STREAM_DRAW);
	}

	public void unbind() {
		GL15.glBindBuffer(type, 0);
	}

	public void updateData(float[] data) {
		updateData(0, data);
	}

	public void updateData(int index, float[] data) {
		bind();
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferSubData(type, index * 4, buffer);
		unbind();
	}

	public int getId() {
		return id;
	}
}
