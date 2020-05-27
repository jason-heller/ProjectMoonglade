package shader;

import org.lwjgl.opengl.GL20;

public class UniformInt extends Uniform {

	private int currentValue;
	private boolean used = false;

	public UniformInt(String name) {
		super(name);
	}

	public void loadInt(int value) {
		if (!used || currentValue != value) {
			used = true;
			GL20.glUniform1i(super.getLocation(), value);
			currentValue = value;
		}
	}

}
