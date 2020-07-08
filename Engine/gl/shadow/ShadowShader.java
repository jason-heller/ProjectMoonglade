package gl.shadow;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;

public class ShadowShader extends ShaderProgram {
	
	private static final String VERTEX_FILE = "gl/shadow/shadow.vert";
	private static final String FRAGMENT_FILE = "gl/shadow/shadow.frag";
	
	public UniformMatrix projectionViewMatrix = new UniformMatrix("projViewModelMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");

	protected ShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE, "in_vertices", "in_uvs");
		super.storeAllUniformLocations(projectionViewMatrix, diffuse);
	}
}
