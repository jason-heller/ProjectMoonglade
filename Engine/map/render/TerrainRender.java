package map.render;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import core.res.Model;
import core.res.Texture;
import dev.Debug;
import gl.Camera;
import map.Chunk;
import map.Terrain;
import procedural.terrain.TerrainGen;

public class TerrainRender {
	private final TerrainShader shader;
	private final Texture grass, snow, bush, sand;

	public TerrainRender() {
		shader = new TerrainShader();
		//, boolean nearest, boolean isTransparent,
		//boolean clampEdges, boolean mipmap, float offset
		grass = Resources.addTexture("grass", "terrain/ground_grass.png", true, false, false, true, 0f);
		bush = Resources.addTexture("bush", "terrain/ground_bush.png", true, false, false, true, 0f);
		snow = Resources.addTexture("snow", "terrain/ground_snow.png", true, false, false, true, 0f);
		sand = Resources.addTexture("sand", "terrain/ground_sand.png", true, false, false, true, 0f);
	}

	public void cleanUp() {
		Resources.removeTextureReference("grass");
		Resources.removeTextureReference("bush");
		Resources.removeTextureReference("snow");
		Resources.removeTextureReference("sand");
		shader.cleanUp();
	}

	public void render(Camera camera, Vector3f lightDir, Terrain chunks, float px, float py, float pz, float pw) {
		if (Debug.terrainWireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		shader.start();
		shader.grass.loadTexUnit(0);
		shader.bush.loadTexUnit(1);
		shader.snow.loadTexUnit(2);
		shader.sand.loadTexUnit(3);
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		shader.clipPlane.loadVec4(px,py,pz,pw);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		grass.bind(0);
		snow.bind(1);
		bush.bind(2);
		sand.bind(3);
		//Resources.getTexture("default").bind(1);
		
		
		for (final Chunk[] chunkBatch : chunks.get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk == null || chunk.isCulled()) {
					continue;
				}

				final Model model = chunk.getModel();
				
				if (model == null) continue;
				model.bind(0, 1, 2, 3);
				model.getIndexVbo().bind();
				// GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				model.unbind(0, 1, 2, 3);
				
				final Model wallModel = chunk.getWallModel();
				if (wallModel != null) {
					wallModel.bind(0, 1, 2, 3);
					wallModel.getIndexVbo().bind();
					GL11.glDrawElements(GL11.GL_TRIANGLES, wallModel.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					wallModel.unbind(0, 1, 2, 3);
				}
				
				//TODO :RREMOVE THIS
				final Model m = chunk.getBuilding().getModel();
				if (m != null) {
					m.bind(0, 1, 2);
					m.getIndexVbo().bind();
					GL11.glDrawElements(GL11.GL_TRIANGLES, m.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					m.unbind(0, 1, 2);
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
