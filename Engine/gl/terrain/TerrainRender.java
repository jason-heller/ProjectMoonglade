package gl.terrain;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.building.BuildingRender;
import gl.res.Model;
import gl.res.Texture;
import gl.shadow.ShadowBox;
import gl.shadow.ShadowRender;
import map.Chunk;
import map.Terrain;
import scene.entity.Entity;
import scene.entity.EntityHandler;

public class TerrainRender {
	private final TerrainShader shader;
	private final Texture grass, flora;
	
	public static float FLORA_TEX_ATLAS_SIZE;
	
	public static boolean renderProps = true;
	
	private ShadowRender shadowRender;

	public TerrainRender() {
		BuildingRender.loadAssets();
		shader = new TerrainShader();
		grass = Resources.addTexture("grass", "terrain/ground_grass.png");//, true, false, false, true, 0f
		flora = Resources.addTexture("flora", "terrain/flora.png", true, true, false, true, 0f);
		FLORA_TEX_ATLAS_SIZE = 1f / (flora.size / 32f);
		
		shadowRender = new ShadowRender();
	}

	public void cleanUp() {
		Resources.removeTextureReference("grass");
		Resources.removeTextureReference("flora");
		shader.cleanUp();
		BuildingRender.cleanUp();
		shadowRender.cleanUp();
	}

	public void render(Camera camera, Vector3f lightDir, Vector3f selectionPt, byte facing, Terrain terrain) {
		shadowRenderPass(camera, terrain, lightDir);
		
		BuildingRender.render(camera, lightDir, selectionPt, facing, terrain);
		
		shader.start();
		
		shader.shadowDistance.loadFloat(ShadowBox.shadowDistance);
		shader.pcf.loadInt(ShadowRender.pcfCount);
		shader.mapSize.loadInt(ShadowRender.shadowMapSize);
		
		shader.diffuse.loadTexUnit(0);
		shader.depthTexture.loadTexUnit(1);
		shader.toShadowSpace.loadMatrix(shadowRender.getToShadowSpaceMatrix());
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(camera.getViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		
		grass.bind(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowRender.getShadowMap());

		for (final Chunk[] chunkBatch : terrain.get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk == null || chunk.isCulled() || chunk.getState() != Chunk.LOADED) {
					continue;
				}

				Model model = chunk.getGroundModel();
				
				if (model == null) continue;
				model.bind(0, 1, 2, 3);
				model.getIndexVbo().bind();
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				model.unbind(0, 1, 2, 3);
				
				model = chunk.getWallModel();
				if (model != null) {
					model.bind(0, 1, 2, 3);
					model.getIndexVbo().bind();
					GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					model.unbind(0, 1, 2, 3);
				}
			}
		}
		
		if (renderProps) {
		
			flora.bind(0);
			
			for (final Chunk[] chunkBatch : terrain.get()) {
				for (final Chunk chunk : chunkBatch) {
					if (chunk == null || chunk.isCulled()) {
						continue;
					}
					
					final Model model = chunk.getProps().getModel();
					if (model != null) {
						model.bind(0, 1, 2, 3);
						model.getIndexVbo().bind();
						GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
						model.unbind(0, 1, 2, 3);
					}
				}
			}
		}

		GL30.glBindVertexArray(0);
		shader.stop();
	}

	private void shadowRenderPass(Camera camera, Terrain terrain, Vector3f lightDir) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		List<Entity> entities = new ArrayList<Entity>();
		
		for(Chunk[] stripe : terrain.get()) {
			for(Chunk chunk : stripe) {
				if (!chunk.isCulled()) {
					chunks.add(chunk);
					List<Entity> chunkEntities = EntityHandler.getAllEntitiesInChunk(chunk);
					if (chunkEntities != null) {
						entities.addAll(chunkEntities);
					}
				}
			}
		}
		
		shadowRender.render(camera, entities, chunks, lightDir);
	}
}
