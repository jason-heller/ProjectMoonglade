package map.building;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.Resources;
import core.res.Model;
import core.res.Texture;
import gl.Camera;
import map.Chunk;
import map.Terrain;
import scene.entity.EntityControl;
import scene.entity.render.GenericMeshShader;

public class BuildingRender {
	private static Texture materialTexture = null;
	private static Matrix4f ZERO_MATRIX = new Matrix4f();
	
	public static int materialTextureScale = 32;
	public static float materialAtlasSize;
	
	public static void loadMaterialTexture() {
		if (materialTexture != null) {
			materialTexture.delete();
		}
		materialTexture = Resources.addTexture("materials", "material/materials.png");
		materialAtlasSize = 1f / (materialTexture.size / materialTextureScale);
	}
	
	public static void render(Camera camera, Vector3f lightDir, Terrain terrain) {
		GenericMeshShader shader = EntityControl.getShader();

		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(lightDir);
		shader.modelMatrix.loadMatrix(ZERO_MATRIX);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		materialTexture.bind(0);
		for (int i = 0; i < Terrain.size; i++) {
			for (int j = 0; j < Terrain.size; j++) {
				Chunk chunk = terrain.get(i, j);
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
		GL30.glBindVertexArray(0);

		shader.stop();
	}
}
