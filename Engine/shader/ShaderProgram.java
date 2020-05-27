package shader;

import java.io.BufferedReader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import io.FileUtils;

public class ShaderProgram {

	private int programID;

	public ShaderProgram() {
	}

	public ShaderProgram(String vertexFile, String fragmentFile, String... inVariables) {
		final int vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		final int fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes(inVariables);
		GL20.glLinkProgram(programID);
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
	}

	public ShaderProgram(String[] vertGeomFragFiles, String... inVariables) {
		final int vertexShaderID = loadShader(vertGeomFragFiles[0], GL20.GL_VERTEX_SHADER);
		final int geometryShaderID = loadShader(vertGeomFragFiles[1], GL32.GL_GEOMETRY_SHADER);
		final int fragmentShaderID = loadShader(vertGeomFragFiles[2], GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, geometryShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes(inVariables);
		GL20.glLinkProgram(programID);
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, geometryShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(geometryShaderID);
		GL20.glDeleteShader(fragmentShaderID);
	}

	protected void bindAttributes(String[] inVariables) {
		for (int i = 0; i < inVariables.length; i++) {
			GL20.glBindAttribLocation(programID, i, inVariables[i]);
		}
	}

	protected void bindFragOutput(int attachment, String name) {
		GL30.glBindFragDataLocation(programID, attachment, name);
	}

	public void cleanUp() {
		stop();
		GL20.glDeleteProgram(programID);
	}

	public int getProgram() {
		return programID;
	}

	private int loadShader(String path, int type) {
		final StringBuilder shaderSource = new StringBuilder();
		try {
			final BufferedReader reader = FileUtils.getReader(path);
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line);
				shaderSource.append("\n");

			}
			reader.close();
		} catch (final Exception e) {
			System.err.println("Could not read file.");
			e.printStackTrace();
			System.exit(-1);
		}
		final int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader " + path);
			System.exit(-1);
		}
		return shaderID;
	}

	public void start() {
		GL20.glUseProgram(programID);
	}

	public void stop() {
		GL20.glUseProgram(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	protected void storeAllUniformLocations(Uniform... uniforms) {
		for (final Uniform uniform : uniforms) {
			uniform.storeUniformLocation(programID);
		}
		GL20.glValidateProgram(programID);
	}
}
