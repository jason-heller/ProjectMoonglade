package gl.post;

import shader.UniformFloat;
import shader.UniformSampler;

public class WaveShader extends PostShader {
	private static final String FRAGMENT_SHADER = "gl/post/glsl/wave.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformFloat timer = new UniformFloat("timer");

	public WaveShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler, timer);
	}

	@Override
	public void loadUniforms() {
		this.timer.loadFloat(PostProcessing.getPostProcessingTimer());
	}
}