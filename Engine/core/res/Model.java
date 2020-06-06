package core.res;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import anim.component.Skeleton;
import dev.Console;

public class Model {
	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_INT = 4;

	public static Model create() {
		final int id = GL30.glGenVertexArrays();
		return new Model(id);
	}

	public final int id;
	private final List<Vbo> dataVbos = new ArrayList<Vbo>();
	private Vbo indexVbo;
	private int indexCount;
	private int vertexCount;
	private float[] vertices = null;

	private float height;
	public Vector3f min, max;
	
	private Skeleton skeleton;

	private Model(int id) {
		this.id = id;
	}

	private void bind() {
		GL30.glBindVertexArray(id);
	}

	public void bind(int... attributes) {
		bind();
		for (final int i : attributes) {
			GL20.glEnableVertexAttribArray(i);
		}
	}

	public void cleanUp() {
		GL30.glDeleteVertexArrays(id);
		for (final Vbo vbo : dataVbos) {
			vbo.delete();
		}
		if (indexVbo != null) {
			indexVbo.delete();
		}
	}

	public void createAttribute(int attribute, byte[] data, int attrSize) {
		final Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_BYTE, false, attrSize, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}
	
	public void createAttribute(int attribute, Vbo dataVbo, int attrSize) {
		dataVbo.bind();
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}

	public void createAttribute(int attribute, float[] data, int attrSize) {
		final Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);

		if (attribute == 0) {
			vertexCount = data.length / 3;
		}
	}

	public void createAttribute(int attribute, int[] data, int attrSize) {
		final Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}

	public void createDynamicAttribute(int attribute, float[] data, int attrSize) {
		final Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeDynamicData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}

	public void createIndexBuffer(int[] indices) {
		this.indexVbo = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		indexVbo.bind();
		indexVbo.storeData(indices);
		this.indexCount = indices.length;
	}
	
	public void setIndexBuffer(Vbo vbo, int indexCount) {
		this.indexVbo = vbo;
		this.indexCount = indexCount;
	}

	public void createIntAttribute(int attribute, int[] data, int attrSize) {
		final Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}

	public float getHeight() {
		return height;
	}

	public int getIndexCount() {
		return indexCount;
	}

	public Vbo getIndexVbo() {
		return indexVbo;
	}

	public Vbo getVbo(int i) {
		return dataVbos.get(i);
	}
	
	public int getNumVbos() {
		return dataVbos.size();
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public float[] getVertices() {
		return vertices;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setVertexData(int[] indices, float[] vertices) {
		this.vertices = new float[indices.length * 3];
		int j = 0;

		for (final int indice : indices) {
			this.vertices[j] = vertices[indice * 3];
			this.vertices[j + 1] = vertices[indice * 3 + 1];
			this.vertices[j + 2] = vertices[indice * 3 + 2];
			j += 3;
		}
	}

	private void unbind() {
		GL30.glBindVertexArray(0);
	}

	public void unbind(int... attributes) {
		for (final int i : attributes) {
			GL20.glDisableVertexAttribArray(i);
		}
		unbind();
	}

	public Skeleton getSkeleton() {
		return skeleton;
	}

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}
}
