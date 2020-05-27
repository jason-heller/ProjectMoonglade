package gl.post;

import shader.UniformSampler;

public class BrightnessShader extends PostShader {
	private static final String FRAGMENT_SHADER = "gl/post/glsl/brightness.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");

	public BrightnessShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler);
	}

	@Override
	public void loadUniforms() {
	}
}