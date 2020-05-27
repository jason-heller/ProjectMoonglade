package gl.particle;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;

public class ParticleShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/particle/particle.vert";
	private static final String FRAGMENT_SHADER = "gl/particle/particle.frag";

	protected UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	protected UniformFloat numRows = new UniformFloat("numRows");

	public ParticleShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		super.storeAllUniformLocations(projectionMatrix, numRows);
	}

}
