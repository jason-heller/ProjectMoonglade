package dev.tracers;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformVec3;

public class LineShader extends ShaderProgram {
	private static final String VERTEX_SHADER = "dev/tracers/tracerVertex.glsl";
	// private static final InnerFile GEOMETRY_SHADER = new
	// InnerFile("utils/tracers", "tracerGeometry.glsl");
	private static final String FRAGMENT_SHADER = "dev/tracers/tracerFragment.glsl";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformVec3 point1 = new UniformVec3("point1");
	public UniformVec3 point2 = new UniformVec3("point2");
	public UniformVec3 color = new UniformVec3("color");

	public LineShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "pos");
		super.storeAllUniformLocations(projectionViewMatrix, point1, point2, color);
	}
}
