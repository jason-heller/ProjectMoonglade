package gl.shadow;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import gl.res.Model;
import gl.res.Texture;
import map.Chunk;
import scene.entity.Entity;

public class ShadowMeshRender {

	private ShadowShader shader;

	/**
	 * @param shader
	 *            - the simple shader program being used for the shadow render
	 *            pass.
	 * @param projectionViewMatrix
	 *            - the orthographic projection matrix multiplied by the light's
	 *            "view" matrix.
	 */
	protected ShadowMeshRender(ShadowShader shader) {
		this.shader = shader;
	}

	protected void render(Matrix4f projectionViewMatrix, List<Entity> entities, List<Chunk> chunks) {

		for (Entity entity : entities) {
			Model model = entity.getModel();
			model.bind(0, 1);
			entity.getDiffuse().bind(0);
			Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, entity.getMatrix(), null);
			shader.projectionViewMatrix.loadMatrix(mvpMatrix);
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(),
					GL11.GL_UNSIGNED_INT, 0);
		}
		
		shader.projectionViewMatrix.loadMatrix(projectionViewMatrix);
		//Resources.getTexture("default").bind(0);
		
		for (Chunk chunk : chunks) {//chunk.getGroundModel(), chunk.getWallModel(), 
			Model[] models = new Model[] {chunk.getBuilding().getModel(), chunk.getProps().getModel()};
			Texture[] textures = new Texture[] {Resources.getTexture("material"), Resources.getTexture("flora")};
			for(int i = 0; i < models.length; i++) {
				if (models[i] == null) continue;
				textures[i].bind(0);
				models[i].bind(0, 1);
				models[i].getIndexVbo().bind();
				GL11.glDrawElements(GL11.GL_TRIANGLES, models[i].getIndexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindVertexArray(0);
	}

}
