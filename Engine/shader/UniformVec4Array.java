package shader;

import org.joml.Vector4f;

public class UniformVec4Array extends Uniform {

	private final UniformVec4[] uniforms;

	public UniformVec4Array(String name, int size) {
		super(name);
		uniforms = new UniformVec4[size];
		for (int i = 0; i < size; i++) {
			uniforms[i] = new UniformVec4(name + "[" + i + "]");
		}
	}

	public void loadVec4(int index, float x, float y, float z, float w) {
		uniforms[index].loadVec4(x, y, z, w);
	}

	public void loadVec4(Vector4f[] vecs) {
		for (int i = 0; i < vecs.length; i++) {
			uniforms[i].loadVec4(vecs[i]);
		}
	}

	@Override
	protected void storeUniformLocation(int programID) {
		for (final UniformVec4 matrixUniform : uniforms) {
			matrixUniform.storeUniformLocation(programID);
		}
	}
}
