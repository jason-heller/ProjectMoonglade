package anim.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import anim.Animator;
import gl.Camera;
import scene.Scene;
import scene.entity.Entity;

// TODO: Rework shaders & animation code
public class AnimationControl {

	private static AnimationShader shader;
	private static ArrayList<Entity> entityBatch;

	public static void add(Entity e) {
		entityBatch.add(e);
	}

	public static void cleanUp() {
		shader.cleanUp();
	}

	public static void init() {
		shader = new AnimationShader();
		entityBatch = new ArrayList<Entity>();
	}

	public static void remove(Entity e) {
		entityBatch.remove(e);
	}

	public static void render(Camera camera, Scene scene) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());

		for (final Entity visObj : entityBatch) {
			final Animator animator = visObj.getAnimator();
			animator.update();
			visObj.getDiffuse().bind(0);
			shader.specularity.loadFloat(0f);

			shader.diffuse.loadTexUnit(0);
			shader.specular.loadTexUnit(1);

			visObj.getModel().bind(0, 1, 2, 3, 4);
			shader.modelMatrix.loadMatrix(visObj.getMatrix());
			shader.jointTransforms.loadMatrixArray(animator.getJointTransforms());
			GL11.glDrawElements(GL11.GL_TRIANGLES, visObj.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			visObj.getModel().unbind(0, 1, 2, 3, 4);
		}
		shader.stop();
	}

	public static void renderViewmodel(Scene scene, Entity animatedModel) {
		final Camera camera = scene.getCamera();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());

		animatedModel.getDiffuse().bind(0);
		animatedModel.getModel().bind(0, 1, 2, 3, 4);
		animatedModel.getDiffuse().bind(0);
		shader.specularity.loadFloat(0f);

		shader.diffuse.loadTexUnit(0);
		shader.specular.loadTexUnit(1);
		shader.modelMatrix.loadMatrix(animatedModel.getMatrix());
		shader.jointTransforms.loadMatrixArray(animatedModel.getAnimator().getJointTransforms());
		GL11.glDrawElements(GL11.GL_TRIANGLES, animatedModel.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		animatedModel.getModel().unbind(0, 1, 2, 3, 4);

		shader.stop();
	}

}
