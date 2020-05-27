package gl.post;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.fbo.FrameBuffer;

public class DefaultShader extends PostShader {
	protected static final String VERTEX_SHADER = "gl/post/glsl/vertex.glsl";
	protected static final String FRAGMENT_SHADER = "gl/post/glsl/fragment.glsl";

	public DefaultShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
	}

	@Override
	public void loadUniforms() {
	}

	public void render(FrameBuffer frameBuffer) {
		// bindFbo();

		start();
		loadUniforms();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getTextureBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		// unbindFbo();
	}
}
