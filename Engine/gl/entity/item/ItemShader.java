package gl.entity.item;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class ItemShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "gl/entity/item/item.vert";
	private static final String FRAGMENT_SHADER = "gl/entity/item/item.frag";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec3 uv = new UniformVec3("uv");

	public ItemShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_vertices", "in_uvs");
		super.storeAllUniformLocations(viewMatrix, projectionMatrix, diffuse, lightDirection, uv);
	}
}
