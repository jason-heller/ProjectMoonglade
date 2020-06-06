package gl.water;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;

public class WaterShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/water/waterVertex.glsl";
	private static final String FRAGMENT_SHADER = "gl/water/waterFragment.glsl";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformSampler reflection = new UniformSampler("reflection");
	protected UniformSampler refraction = new UniformSampler("refraction");
	protected UniformSampler dudv = new UniformSampler("dudv");
	//public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformFloat timer = new UniformFloat("timer");

	public WaterShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(projectionViewMatrix, reflection, refraction, dudv, timer);
	}
}
