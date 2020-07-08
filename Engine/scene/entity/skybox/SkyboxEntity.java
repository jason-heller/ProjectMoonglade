package scene.entity.skybox;

import org.joml.Vector3f;

import core.Resources;
import gl.res.Texture;
import scene.Scene;

public class SkyboxEntity {
	
	public Vector3f position = new Vector3f();
	protected float distance, yaw, pitch;
	private Texture texture;

	public SkyboxEntity(float distance, float yaw, float pitch, String texture) {
		this.texture = Resources.getTexture(texture);
		this.distance = distance;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public void update(Scene scene, Vector3f lightDirection) {
		
	}

	public Texture getDiffuse() {
		return texture;
	}
	
}
