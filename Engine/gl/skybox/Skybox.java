package gl.skybox;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import core.Resources;
import core.Window;
import core.res.Model;
import core.res.Texture;
import gl.Camera;
import map.Enviroment;
import util.MathUtil;

// This handles the logic between the player's character and the game
public class Skybox {
	private static final float SIZE = 32;

	private static final float CYCLE_INTEVAL = Enviroment.DAY_LENGTH / 4f;
	private final SkyboxShader shader;
	private final Model box;
	private float alpha;

	private final Vector3f topColor = new Vector3f(), bottomColor = new Vector3f();

	private final Vector3f[] SKY_COLOR = new Vector3f[] {
			// Morning
			new Vector3f(255 / 255f, 153 / 255f, 153 / 255f), new Vector3f(255 / 255f, 51 / 255f, 153 / 255f),
			// Day
			new Vector3f(102 / 255f, 178 / 255f, 255 / 255f), new Vector3f(0 / 255f, 102 / 255f, 104 / 255f),
			// Evening
			new Vector3f(255 / 255f, 178 / 255f, 102 / 255f), new Vector3f(255 / 255f, 0 / 255f, 127 / 255f),
			// Night
			new Vector3f(51 / 255f, 0 / 255f, 102 / 255f), new Vector3f(25 / 255f, 0 / 255f, 51 / 255f),
			// Copy of morning
			new Vector3f(255 / 255f, 153 / 255f, 153 / 255f), new Vector3f(255 / 255f, 51 / 255f, 153 / 255f) };

	private final float[] BG_ALPHAS = new float[] { .25f, 0f, .5f, 1f, .25f };

	private final Texture starTexture;

	private float rotation;

	public Skybox() {
		this.shader = new SkyboxShader();
		this.box = CubeGenerator.generateCube(SIZE);
		/*
		 * skyboxTexture = new CubemapTexture(new String[] { "sky/stars.png",
		 * "sky/stars.png", "sky/stars.png", "sky/stars.png", "sky/stars.png",
		 * "sky/stars.png"
		 * 
		 * });
		 */
		starTexture = Resources.addTexture("skybox", "sky/stars.png", GL13.GL_TEXTURE_CUBE_MAP, true, 0);

		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}

	/**
	 * Delete the shader when the game closes.
	 */
	public void cleanUp() {
		starTexture.delete();
		shader.cleanUp();
	}

	private void getColors(int gameTime, Vector3f tint, float influence) {
		final int currentCycle = (int) (gameTime / CYCLE_INTEVAL) * 2;
		final float cycleDuration = gameTime % CYCLE_INTEVAL / CYCLE_INTEVAL;

		alpha = MathUtil.lerp(BG_ALPHAS[currentCycle / 2], BG_ALPHAS[currentCycle / 2 + 1], cycleDuration);
		topColor.set(Vector3f.lerp(SKY_COLOR[currentCycle + 2], SKY_COLOR[currentCycle], cycleDuration));
		bottomColor.set(Vector3f.lerp(SKY_COLOR[currentCycle + 3], SKY_COLOR[currentCycle + 1], cycleDuration));
	
		topColor.set(Vector3f.lerp(topColor, Vector3f.mul(topColor, tint), influence));
		bottomColor.set(Vector3f.lerp(bottomColor, Vector3f.mul(bottomColor, tint), influence));
	}

	/**
	 * Starts the shader, loads the projection-view matrix to the uniform variable,
	 * and sets some OpenGL state which should be mostly self-explanatory.
	 *
	 * @param camera - the scene's camera.
	 */
	private void prepare(Camera camera) {
		shader.start();
		final Matrix4f matrix = new Matrix4f(camera.getViewMatrix());
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		// matrix.rotateX(camera.getPitch());
		matrix.rotateY(rotation);
		rotation += Window.deltaTime / 2f;
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(matrix);
		/*
		 * OpenGlUtils.disableBlending(); OpenGlUtils.enableDepthTesting(true);
		 * OpenGlUtils.cullBackFaces(true); OpenGlUtils.antialias(false);
		 */
	}

	public void render(Camera camera, int time, Vector3f tint, Vector3f weatherColor) {
		prepare(camera);
		box.bind(0);
		getColors(time, tint, .2f);
		starTexture.bind(0);
		shader.bgAlpha.loadFloat(alpha);
		shader.topColor.loadVec3(topColor);
		shader.bottomColor.loadVec3(bottomColor);
		shader.weatherColor.loadVec3(weatherColor);
		GL11.glDrawElements(GL11.GL_TRIANGLES, box.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		box.unbind(0);
		shader.stop();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

}
