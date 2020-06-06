package scene.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Application;
import core.res.Model;
import core.res.Texture;
import gl.Camera;
import scene.entity.render.GenericMeshShader;

public class EntityControl {

	private static GenericMeshShader shader;
	private static Map<Texture, List<Entity>> entities;

	public static void addEntity(Entity obj) {
		if (entities.containsKey(obj.getDiffuse())) {
			entities.get(obj.getDiffuse()).add(obj);
		} else {
			final List<Entity> objs = new ArrayList<Entity>();
			objs.add(obj);
			entities.put(obj.getDiffuse(), objs);
		}
	}

	public static void cleanUp() {
		shader.cleanUp();
	}

	public static void clearEntities() {
		entities.clear();
	}

	public static void init() {
		shader = new GenericMeshShader();
		entities = new HashMap<Texture, List<Entity>>();
	}

	public static void removeEntity(Entity obj) {
		if (entities.containsKey(obj.getDiffuse())) {
			entities.get(obj.getDiffuse()).remove(obj);
		}

	}

	public static void render(Camera camera, Vector3f lightDir) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		shader.color.loadVec3(1, 1, 1);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		for (final Texture texture : entities.keySet()) {
			if (texture == null) {
				continue;
			}

			texture.bind(0);
			for (final Entity entity : entities.get(texture)) {
				final Model model = entity.getModel();

				if (model == null) {
					continue;
				}

				model.bind(0, 1, 2);
				shader.modelMatrix.loadMatrix(entity.getMatrix());

				GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				model.unbind(0, 1, 2);
			}
		}
	
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		shader.stop();
	}

	public static void render(Camera camera, Vector3f lightDir, Entity object) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		object.getDiffuse().bind(0);
		object.getModel().bind(0, 1, 2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawElements(GL11.GL_TRIANGLES, object.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	public static void update() {
		for (final Texture texture : entities.keySet()) {
			final List<Entity> batch = entities.get(texture);
			for (int j = 0, m = batch.size(); j < m; j++) {
				final Entity entity = batch.get(j);
				entity.update(Application.scene);
			}
		}
	}
	
	public static GenericMeshShader getShader() {
		return shader;
	}
}
