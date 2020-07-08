package gl.skybox.entity;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class SkyboxEntityShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/skybox/entity/skyent.vert";
	private static final String FRAGMENT_SHADER = "gl/skybox/entity/skyent.frag";

	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	//public UniformVec3 lightDirection = new UniformVec3("lightDirection");

	public SkyboxEntityShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs");
		super.storeAllUniformLocations(modelMatrix, projectionViewMatrix, diffuse);
	}
}
