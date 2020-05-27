package gl.post;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import gl.fbo.FrameBuffer;
import shader.UniformSampler;

public class CombineShader extends PostShader {
	private static final String FRAGMENT_SHADER = "gl/post/glsl/combine.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler hlSampler = new UniformSampler("highlightSampler");

	public CombineShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler, hlSampler);
	}

	@Override
	public void loadUniforms() {
		sampler.loadTexUnit(0);
		hlSampler.loadTexUnit(1);
	}

	public void render(FrameBuffer color, FrameBuffer highlight) {
		// bindFbo();

		start();
		loadUniforms();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, color.getTextureBuffer());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, highlight.getTextureBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		// unbindFbo();

		// FboUtils.resolve(this.getFbo());
	}
}