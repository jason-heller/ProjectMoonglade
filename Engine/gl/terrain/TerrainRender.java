package gl.terrain;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.building.BuildingRender;
import gl.res.Model;
import gl.res.Texture;
import map.Chunk;
import map.Terrain;
import procedural.terrain.GenTerrain;

public class TerrainRender {
	private final TerrainShader shader;
	private final Texture grass, fauna;

	public TerrainRender() {
		BuildingRender.loadAssets();
		shader = new TerrainShader();
		//, boolean nearest, boolean isTransparent,
		//boolean clampEdges, boolean mipmap, float offset
		grass = Resources.addTexture("grass", "terrain/ground_grass.png", true, false, false, true, 0f);
		//grass = Resources.addTexture("grass", "terrain/ground_grass.png", true, false, false, false, 0f);
		fauna = Resources.addTexture("fauna", "terrain/fauna.png", true, true, false, true, 0f);
	}

	public void cleanUp() {
		Resources.removeTextureReference("grass");
		Resources.removeTextureReference("fauna");
		shader.cleanUp();
		BuildingRender.cleanUp();
	}

	public void render(Camera camera, Vector3f lightDir, Vector3f selectionPt, byte facing, Terrain chunks) {
		if (Debug.terrainWireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		BuildingRender.render(camera, lightDir, selectionPt, facing, chunks);
		
		shader.start();
		shader.diffuse.loadTexUnit(0);
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		grass.bind(0);
		//Resources.getTexture("default").bind(1);
		for (final Chunk[] chunkBatch : chunks.get()) {
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
		
		fauna.bind(0);
		
		for (final Chunk[] chunkBatch : chunks.get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk == null || chunk.isCulled()) {
					continue;
				}
				
				final Model model = chunk.getChunkEntities().getModel();
				if (model != null) {
					model.bind(0, 1, 2, 3);
					model.getIndexVbo().bind();
					GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					model.unbind(0, 1, 2, 3);
				}
			}
		}

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		shader.stop();
		
		if (Debug.terrainWireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
}
