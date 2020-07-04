package gl.terrain;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class TerrainShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/terrain/terrain.vert";
	private static final String FRAGMENT_SHADER = "gl/terrain/terrain.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformSampler terrainTexture = new UniformSampler("terrainTexture");

	public TerrainShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, lightDirection, terrainTexture);
		
		/*grass.loadTexUnit(0);
		bush.loadTexUnit(1);
		snow.loadTexUnit(2);
		sand.loadTexUnit(3);*/
	}
}
