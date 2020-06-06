package map.weather;

import org.joml.Vector3f;

import core.Resources;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleEmitter;

public class PrecipitationEmitter extends ParticleEmitter {

	private Vector3f origin, dropDirection;
	private float dripTimer = 0f;
	
	public PrecipitationEmitter() {
		super(Resources.getTexture("particles"), 48, .15f, 0f, 50, .4f);
		origin = new Vector3f();
		this.setOrigin(origin);
		dropDirection = new Vector3f(0, -1, 0);
		this.setTextureAtlasRange(1,1);
		this.setDirection(dropDirection, .2f);
	}

	public void update(Camera camera, float weather) {
		this.setDirection(dropDirection, (weather+.55f)/5f);
		dripTimer +=  Window.deltaTime;
		final float dropDelay = 1.01f + weather;
		if (dripTimer >= dropDelay) {
			for(int i = 0; i < 9; i++) {
				origin.set(camera.getPosition());
				origin.x -= 1f;
				origin.z -= 1f;
				origin.y += 3;
				
				origin.x += (i % 3);
				origin.z += (i / 3);
				
				generateParticles(camera);
			}
			
			dripTimer -= dropDelay-(Math.random()/4f);
		}
	}
}
