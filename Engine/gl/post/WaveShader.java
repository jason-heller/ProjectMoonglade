package gl.post;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import core.Application;
import gl.fbo.FrameBuffer;
import scene.overworld.Overworld;
import shader.UniformFloat;
import shader.UniformSampler;
import shader.UniformVec3;

public class WaveShader extends PostShader {
	private static final String FRAGMENT_SHADER = "gl/post/glsl/wave.glsl";

	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformFloat timer = new UniformFloat("timer");
	public UniformVec3 color = new UniformVec3("color");

	public WaveShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
		storeAllUniformLocations(sampler, timer, color);
	}

	@Override
	public void loadUniforms() {
		if (!(Application.scene instanceof Overworld)) {
			return;
		}
		Overworld ow = (Overworld)Application.scene;
		Vector3f waterColor = ow.getEnviroment().getClosestBiome().waterColor;		// God is dead, my global variables killed him
		this.timer.loadFloat(PostProcessing.getPostProcessingTimer());
		this.color.loadVec3(waterColor);
	}

	public void render(FrameBuffer frameBuffer) {
		// bindFbo();

		start();
		loadUniforms();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getTextureBuffer());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		stop();

		// unbindFbo();
	}
}