package gl.particle;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import gl.Camera;
import gl.res.Model;
import gl.res.Texture;

class ParticleRenderer {
	
	private static final float[] VERTICES = new float[] {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	private static final int MAX_PARTICLES = 5000;
	private static final int INSTANCE_DATA_LENGTH = 21;
	private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * INSTANCE_DATA_LENGTH);

	private int vbo;
	
	private ParticleShader shader;
	private Model model;
	private int pointer = 0;
	
	public ParticleRenderer() {
		model = Model.create();
		model.bind();
		model.createAttribute(0, VERTICES, 2);
		model.unbind();
		
		shader = new ParticleShader();
		vbo = createEmptyVbo(INSTANCE_DATA_LENGTH * MAX_PARTICLES);
		addInstancedAttrib(model.id, vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
		addInstancedAttrib(model.id, vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
		addInstancedAttrib(model.id, vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
		addInstancedAttrib(model.id, vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
		addInstancedAttrib(model.id, vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
		addInstancedAttrib(model.id, vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
	}
	
	public void render(Map<Texture, List<Particle>> particles, Camera camera) {
		shader.start();
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		GL30.glBindVertexArray(model.id);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);

		for(Texture texture : particles.keySet()) {
			bindTexture(texture);
			List<Particle> particleList = particles.get(texture);
			pointer = 0;
			float[] vboData = new float[particleList.size() * INSTANCE_DATA_LENGTH];
			for (Particle part : particleList) {
				buildModelviewMatrix(part.getPosition(), part.getRotation(), part.getScale(), camera.getViewMatrix(), vboData);
				updateTextureCoords(part, vboData);
			}
			updateVbo(vbo, vboData, buffer);
			
			GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, particleList.size());
		}
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(true);
		GL20.glUseProgram(0);
	}
	
	private void buildModelviewMatrix(Vector3f pos, float rot, float scale, Matrix4f viewMatrix, float[] vboData) {
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.translate(pos);
		modelMatrix.m00 = viewMatrix.m00;
	    modelMatrix.m01 = viewMatrix.m10;
	    modelMatrix.m02 = viewMatrix.m20;
	    modelMatrix.m10 = viewMatrix.m01;
	    modelMatrix.m11 = viewMatrix.m11;
	    modelMatrix.m12 = viewMatrix.m21;
	    modelMatrix.m20 = viewMatrix.m02;
	    modelMatrix.m21 = viewMatrix.m12;
	    modelMatrix.m22 = viewMatrix.m22;
	    modelMatrix.rotate((float)Math.toRadians(rot), new Vector3f(0,0,1));
	    modelMatrix.scale(scale);
	    Matrix4f mvMatrix = new Matrix4f();
	    Matrix4f.mul(viewMatrix, modelMatrix, mvMatrix);
	    storeModelViewMatrix(mvMatrix, vboData);
	}
	
	private void storeModelViewMatrix(Matrix4f matrix, float[] vboData) {
		vboData[pointer++] = matrix.m00;
		vboData[pointer++] = matrix.m01;
		vboData[pointer++] = matrix.m02;
		vboData[pointer++] = matrix.m03;
		vboData[pointer++] = matrix.m10;
		vboData[pointer++] = matrix.m11;
		vboData[pointer++] = matrix.m12;
		vboData[pointer++] = matrix.m13;
		vboData[pointer++] = matrix.m20;
		vboData[pointer++] = matrix.m21;
		vboData[pointer++] = matrix.m22;
		vboData[pointer++] = matrix.m23;
		vboData[pointer++] = matrix.m30;
		vboData[pointer++] = matrix.m31;
		vboData[pointer++] = matrix.m32;
		vboData[pointer++] = matrix.m33;
	}
	
	private void updateTextureCoords(Particle particle, float[] vboData) {
		vboData[pointer++] = particle.getTextureOffset1().x;
		vboData[pointer++] = particle.getTextureOffset1().y;
		vboData[pointer++] = particle.getTextureOffset2().x;
		vboData[pointer++] = particle.getTextureOffset2().y;
		vboData[pointer++] = particle.getBlend();
	}
	
	private void bindTexture(Texture texture) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		texture.bind(0);

		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA,texture.isTransparent() ? GL14.GL_FUNC_SUBTRACT : GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		shader.numRows.loadFloat(texture.getTextureAtlasRows());
	}
	
	public static int createEmptyVbo(int numFloats) {
		int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 4 * numFloats, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vbo;
	}
	
	public static void addInstancedAttrib(int vao, int vbo, int attrib, int dataSize, int stride, int offset) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vao);
		
		GL20.glVertexAttribPointer(attrib, dataSize, GL11.GL_FLOAT, false, stride*4, offset*4);
		GL33.glVertexAttribDivisor(attrib, 1);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	public static void updateVbo(int vbo, float[] data, FloatBuffer buffer) {

		buffer.clear();
		buffer.put(data);
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void cleanup() {
		GL15.glDeleteBuffers(vbo);
		GL30.glDeleteVertexArrays(model.id);
		shader.cleanUp();
	}
}
