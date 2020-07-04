package gl.water;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import core.Resources;
import dev.Console;
import gl.Camera;
import gl.Render;
import gl.Window;
import gl.res.Model;
import map.Chunk;
import map.Enviroment;

public class WaterRender {
	private WaterShader shader;
	private float timer = 0;
	
	public WaterRender() {
		// Todo, this should always be loaded prob
		Resources.addTexture("water", "water/water.png");
		Resources.addTexture("dudv", "water/dudv.png");
		
		shader = new WaterShader();
	}
	
	public void render(Camera camera, Enviroment env) {
		timer += Window.deltaTime;
		shader.start();
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		shader.dudv.loadTexUnit(0);
		shader.water.loadTexUnit(1);
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		Resources.getTexture("dudv").bind(0);
		Resources.getTexture("water").bind(1);
		
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.timer.loadFloat(timer);
		
		Vector3f waterColor = Vector3f.lerp(env.getClosestBiome().waterColor, env.getSkybox().getTopColor(), .75f);
		shader.color.loadVec3(waterColor);
		
		for(Chunk[] chunks : env.getTerrain().get()) {
			for(Chunk chunk : chunks) {
				Model model = chunk.getWaterModel();
				if (model != null && !chunk.isCulled()) {
					
					model.bind(0,1);
					model.getIndexVbo().bind();
					GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				}
			}
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		
		shader.stop();
	}
	
	public void cleanUp() {
		Resources.removeTextureReference("water");
		Resources.removeTextureReference("dudv");
	}
}
