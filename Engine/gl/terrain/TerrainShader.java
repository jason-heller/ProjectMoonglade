package gl.terrain;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformInt;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class TerrainShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/terrain/terrain.vert";
	private static final String FRAGMENT_SHADER = "gl/terrain/terrain.frag";

	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformSampler diffuse = new UniformSampler("terrainTexture");
	protected UniformSampler depthTexture = new UniformSampler("depthTexture");
	public UniformFloat shadowDistance = new UniformFloat("shadowDistance");
	public UniformInt pcf = new UniformInt("pcf");
	public UniformInt mapSize = new UniformInt("mapSize");
	
	public UniformMatrix toShadowSpace = new UniformMatrix("toShadowSpace");

	public TerrainShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals", "in_colors");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, lightDirection, diffuse, depthTexture, shadowDistance, pcf, mapSize, toShadowSpace);
	}
}
