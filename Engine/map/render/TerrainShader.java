package map.render;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class TerrainShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "map/render/terrain.vert";
	private static final String FRAGMENT_SHADER = "map/render/terrain.frag";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec4 clipPlane = new UniformVec4("lightDirection");
	protected UniformSampler grass = new UniformSampler("grass");
	protected UniformSampler snow = new UniformSampler("snow");
	protected UniformSampler bush = new UniformSampler("bush");
	protected UniformSampler sand = new UniformSampler("sand");

	public TerrainShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, lightDirection, grass, snow, bush, sand);
		
		/*grass.loadTexUnit(0);
		bush.loadTexUnit(1);
		snow.loadTexUnit(2);
		sand.loadTexUnit(3);*/
	}
}
