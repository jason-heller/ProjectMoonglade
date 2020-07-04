package gl.water;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class WaterShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/water/water.vert";
	private static final String FRAGMENT_SHADER = "gl/water/water.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformSampler dudv = new UniformSampler("dudv");
	protected UniformSampler water = new UniformSampler("water");
	public UniformVec3 color = new UniformVec3("color");
	public UniformFloat timer = new UniformFloat("timer");

	public WaterShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(projectionViewMatrix, color, dudv, water, timer);
	}
}
