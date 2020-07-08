package scene.entity.skybox;

import org.joml.Vector3f;

import scene.Scene;

public class RevolvingPlanetEntity extends SkyboxEntity {

	public RevolvingPlanetEntity(float distance, float yaw, float pitch, String texture) {
		super(distance, yaw, pitch, texture);
	}
	
	public void update(Scene scene, Vector3f lightDirection) {
		super.update(scene, lightDirection);
		Vector3f lightFlip = new Vector3f(-lightDirection.x, lightDirection.y, -lightDirection.z).normalize();
		position.set(Vector3f.mul(lightFlip, distance));
	}
}
