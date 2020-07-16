package gl.anim.render;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMat4Array;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class AnimationShader extends ShaderProgram {

	private static final int MAX_JOINTS = 25;

	private static final String VERTEX_SHADER = "gl/anim/render/anim.vert";
	private static final String FRAGMENT_SHADER = "gl/anim/render/anim.frag";

	protected UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformVec3 cameraPos = new UniformVec3("cameraPos");

	protected UniformMat4Array jointTransforms = new UniformMat4Array("jointTransforms", MAX_JOINTS);

	protected UniformSampler diffuse = new UniformSampler("diffuse");
	protected UniformSampler specular = new UniformSampler("specular");
	protected UniformFloat specularity = new UniformFloat("specularity");

	public AnimationShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normal", "in_jointIndices",
				"in_weights");
		super.storeAllUniformLocations(projectionViewMatrix, modelMatrix, diffuse, specular, lightDirection,
				jointTransforms, specularity, cameraPos);
		super.bindFragOutput(0, "out_color");
		super.bindFragOutput(1, "out_brightness");
	}
}
