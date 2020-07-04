package gl.building;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class BuildingShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/building/building.vert";
	private static final String FRAGMENT_SHADER = "gl/building/building.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformSampler diffuse = new UniformSampler("diffuse");

	public BuildingShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals", "in_colors");
		super.storeAllUniformLocations(projectionViewMatrix, lightDirection, diffuse);
	}
}
