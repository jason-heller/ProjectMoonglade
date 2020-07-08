package gl.skybox.entity;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import core.Application;
import core.Resources;
import gl.Camera;
import scene.entity.skybox.SkyboxEntity;

public class SkyboxEntityRender {
	
	private SkyboxEntityShader shader;

	public SkyboxEntityRender() {
		Resources.addTexture("sun", "sky/sun.png");
		Resources.addTexture("moon", "sky/moon.png");
		
		shader = new SkyboxEntityShader();
	}

	public void render(Camera camera, Vector3f lightDirection, List<SkyboxEntity> list) {
		shader.start();
		
		shader.projectionViewMatrix.loadMatrix(buildProjViewMatrix(camera));
		shader.diffuse.loadTexUnit(0);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Resources.QUAD2D.bind(0, 1);

		for(SkyboxEntity entity : list) {
			shader.modelMatrix.loadMatrix(buildViewMatrix(entity.position, camera));
			entity.getDiffuse().bind(0);
			entity.update(Application.scene, lightDirection);
			
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		}

		Resources.QUAD2D.unbind(0, 1);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		shader.stop();
	}

	private Matrix4f buildProjViewMatrix(Camera camera) {
		Matrix4f matrix = new Matrix4f();
		matrix.rotateX(camera.getPitch());
		matrix.rotateY(camera.getYaw());
		return Matrix4f.mul(camera.getProjectionMatrix(), matrix, null);
	}

	private Matrix4f buildViewMatrix(Vector3f position, Camera camera) {
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.translate(position.x, position.y, position.z);
		Matrix4f viewMatrix = camera.getViewMatrix();
		modelMatrix.m00 = viewMatrix.m00;
	    modelMatrix.m01 = viewMatrix.m10;
	    modelMatrix.m02 = viewMatrix.m20;
	    modelMatrix.m10 = viewMatrix.m01;
	    modelMatrix.m11 = viewMatrix.m11;
	    modelMatrix.m12 = viewMatrix.m21;
	    modelMatrix.m20 = viewMatrix.m02;
	    modelMatrix.m21 = viewMatrix.m12;
	    modelMatrix.m22 = viewMatrix.m22;
	    modelMatrix.scale(.75f);
	    return modelMatrix;
	}	
	
	public void cleanUp() {

		Resources.getTexture("sun").delete();
		Resources.getTexture("moon").delete();
		shader.cleanUp();
	}
}
