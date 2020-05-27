package gl.fbo;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformInt;
import shader.UniformSampler;
import shader.UniformVec3;

public class FboShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "opengl/fbo/defaultFboVertex.glsl";
	private static final String FRAGMENT_SHADER = "opengl/fbo/defaultFboFragment.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformInt state = new UniformInt("state");
	public UniformFloat timer = new UniformFloat("timer");
	public UniformVec3 color = new UniformVec3("color");

	public FboShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(sampler, state, timer, color);
	}
}
