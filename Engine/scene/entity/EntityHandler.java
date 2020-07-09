package scene.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Application;
import gl.Camera;
import gl.entity.GenericMeshShader;
import gl.entity.item.ItemRender;
import gl.res.Model;
import gl.res.Texture;
import map.Chunk;
import map.Terrain;

public class EntityHandler {

	private static GenericMeshShader shader;
	private static ItemRender itemRender;
	private static Map<Texture, List<Entity>> entities;
	private static Map<Chunk, List<Entity>> byChunk;
	private static LinkedList<Entity> removalQueue, addQueue;
	public static int entityRadius = 3;

	public static void addEntity(Entity entity) {
		addQueue.add(entity);
	}

	public static void cleanUp() {
		shader.cleanUp();
		itemRender.cleanUp();
		EntityData.cleanUp();
	}

	public static void clearEntities() {
		entities.clear();
	}

	public static void init() {
		shader = new GenericMeshShader();
		entities = new HashMap<Texture, List<Entity>>();
		byChunk = new HashMap<Chunk, List<Entity>>();
		removalQueue = new LinkedList<Entity>();
		addQueue = new LinkedList<Entity>();
		itemRender = new ItemRender();
		
		EntityData.init();
	}

	public static void removeEntity(Entity entity) {
		if (entity.persistency != 3) {
			removalQueue.add(entity);
		}
	}

	public static void render(Camera camera, Vector3f lightDir) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		shader.color.loadVec4(1, 1, 1, 1);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		for (final Texture texture : entities.keySet()) {
			if (texture == null || texture == itemRender.getTexture()) {
				continue;
			}

			texture.bind(0);

			for (final Entity entity : entities.get(texture)) {
				final Model model = entity.getModel();

				if (model == null || entity.deactivated) {
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
		shader.stop();
		
		List<Entity> items = entities.get(itemRender.getTexture());
		if (items != null) {
			itemRender.render(camera, lightDir, items);
		}
	}

	public static void renderViewmodel(Camera camera, Vector3f lightDir, Model model, Texture texture, Matrix4f modelMatrix) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.lightDirection.loadVec3(lightDir);
		shader.color.loadVec4(1, 1, 1, 1);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		model.bind(0, 1, 2);
		shader.modelMatrix.loadMatrix(modelMatrix);
		texture.bind(0);
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		model.unbind(0, 1, 2);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void render(Camera camera, Vector3f lightDir, Entity entity) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		entity.getDiffuse().bind(0);
		entity.getModel().bind(0, 1, 2);
		shader.modelMatrix.loadMatrix(entity.getMatrix());
		GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void update(Terrain terrain) {
		for(Entity entity : removalQueue) {
			if (entities.containsKey(entity.getDiffuse())) {
				entities.get(entity.getDiffuse()).remove(entity);
			}
			
			if (entity.getChunk() != null) {
				List<Entity> list = byChunk.get(entity.getChunk());
				if (list != null) {
					list.remove(entity);
				}
			}
		}
		removalQueue.clear();
		
		for(Entity entity : addQueue) {
			if (entities.containsKey(entity.getDiffuse())) {
				entities.get(entity.getDiffuse()).add(entity);
			} else {
				final List<Entity> ents = new ArrayList<Entity>();
				ents.add(entity);
				entities.put(entity.getDiffuse(), ents);
			}
		}
		addQueue.clear();
		
		for (final Texture texture : entities.keySet()) {
			final List<Entity> batch = entities.get(texture);
			for (int j = 0, m = batch.size(); j < m; j++) {
				final Entity entity = batch.get(j);
				if (entity.deactivated)
					continue;
				
				entity.update(Application.scene);
			}
		}
	}

	public static void tick(Terrain terrain) {
		for (final Texture texture : entities.keySet()) {
			final List<Entity> batch = entities.get(texture);
			for (int j = 0, m = batch.size(); j < m; j++) {
				final Entity entity = batch.get(j);
				boolean cont = handleEntityChunkMap(entity, terrain);
				if (cont || entity.deactivated)
					continue;
				
				entity.tick(Application.scene);
			}
		}
	}
	
	public static void onChunkUnload(Chunk chunk) {
		List<Entity> entities = byChunk.get(chunk);
		if (entities != null) {
			for(Entity entity : entities) {
				removeEntity(entity);
			}
		}
		
		byChunk.remove(chunk);
	}
	
	private static boolean handleEntityChunkMap(Entity entity, Terrain terrain) {
		boolean cont = false;
		Chunk chunk = terrain.getChunkAt(entity.position.x, entity.position.z);
		
		if (chunk != null && chunk != entity.getChunk()) {
			if (entity.getChunk() != null) {
				removeChunkAssignment(entity);
			}
			
			entity.setChunk(chunk);
			if (entity.persistency != 3) {
				assignToChunk(entity);
			}
			
			final int size = Terrain.size;
			final int halfSize = size / 2;
			entity.deactivated = true;
			
			if (Math.abs(chunk.arrX - halfSize) < entityRadius && Math.abs(chunk.arrZ - halfSize) < entityRadius) {
				entity.deactivated = false;
			}
			
		} else if (chunk == null) {
			/*if (entity.getChunk() != null) {
				removeChunkAssignment(entity);
			}
			entity.setChunk(null);*/
			cont = true;
		}
		return cont;
	}
	
	private static void assignToChunk(Entity entity) {
		if (entity.getPersistency() == 3) return;
		
		Chunk chunk = entity.getChunk();
		if (byChunk.containsKey(chunk)) {
			byChunk.get(chunk).add(entity);
		} else {
			final List<Entity> ents = new ArrayList<Entity>();
			ents.add(entity);
			byChunk.put(chunk, ents);
		}
		chunk.editFlags |= 0x08;
	}
	
	private static void removeChunkAssignment(Entity entity) {
		Chunk chunk = entity.getChunk();
		List<Entity> entities = byChunk.get(chunk);
		if (entities != null) {
			entities.remove(entity);
			
			if (entities.isEmpty()) {
				chunk.editFlags = (byte) (chunk.editFlags - (byte)(chunk.editFlags & 0x08));
			}
		}
	}

	public static GenericMeshShader getShader() {
		return shader;
	}

	public static List<Entity> getAllEntitiesInChunk(Chunk chunk) {
		return byChunk.get(chunk);
	}

	/*public static void shiftX(Terrain t, int dx) {
		final int halfSize = Globals.chunkRenderDist/2;
		final int rad = Globals.chunkEntityRadius;
		final int i1 = dx == 1 ? halfSize - rad : halfSize + rad;
		final int i2 = dx == 1 ? halfSize + rad : halfSize - rad;
		
		for (int j = halfSize-rad; j <= halfSize+rad; j++) {
			chunkActivation(t.get(i1, j), true);
			chunkActivation(t.get(i2, j), false);
		}
	}
	
	public static void shiftY(Terrain t, int dz) {
		final int halfSize = Globals.chunkRenderDist/2;
		final int rad = Globals.chunkEntityRadius;
		final int j1 = dz == 1 ? halfSize - rad : halfSize + rad;
		final int j2 = dz == 1 ? halfSize + rad : halfSize - rad;
		
		for (int i = halfSize-rad; i <= halfSize+rad; i++) {
			chunkActivation(t.get(i, j1), true);
			chunkActivation(t.get(i, j2), false);
		}
	}*/
	
	private static void chunkDeactivation(Chunk chunk, boolean activate) {
		List<Entity> ents = byChunk.get(chunk);
		if (ents != null) {
			for(Entity ent : ents) {
				ent.deactivated = activate;
			}
		}
	}

	public static void setActivation(Terrain t) {
		final int size = Terrain.size;
		final int halfSize = size / 2;
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (Math.abs(i - halfSize) < entityRadius && Math.abs(j - halfSize) < entityRadius) {
					chunkDeactivation(t.get(i, j), false);
				} else {
					chunkDeactivation(t.get(i, j), true);
				}
			}
		}
	}

	public static ItemRender getItemRender() {
		return itemRender;
	}
}
