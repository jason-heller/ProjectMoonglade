package gl.water;

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
		
		shader.reflection.loadTexUnit(0);
		shader.refraction.loadTexUnit(1);
		shader.dudv.loadTexUnit(2);
		//shader.offset.loadVec4(px, py, pz, pw);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Render.reflection.getTextureBuffer());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Render.refraction.getTextureBuffer());
		
		Resources.getTexture("dudv").bind(2);
		
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.timer.loadFloat(timer);
		
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
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		shader.stop();
	}
	
	public void cleanUp() {
		Resources.removeTextureReference("water");
	}
}
