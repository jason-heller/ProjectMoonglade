package gl.post;

import org.lwjgl.opengl.GL11;

import gl.fbo.FrameBuffer;
import shader.UniformSampler;

public class GaussianHBlur extends PostShader {
	private static final String VERTEX_SHADER = "gl/post/glsl/hblur_vertex.glsl";
	private static final String FRAGMENT_SHADER = "gl/post/glsl/gaussian.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");

	public GaussianHBlur() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, 1280 / 8, 720 / 8);
		storeAllUniformLocations(sampler);
	}

	@Override
	public void loadUniforms() {
	}

	public void render(FrameBuffer frameBuffer) {
		bindFbo();

		start();
		loadUniforms();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getTextureBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		unbindFbo();
	}
}