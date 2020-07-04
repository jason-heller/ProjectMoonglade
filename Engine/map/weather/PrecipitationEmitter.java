package map.weather;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import geom.Frustum;
import geom.Plane;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleEmitter;

public class PrecipitationEmitter extends ParticleEmitter {

	private Vector3f origin, dropDirection;
	private float dripTimer = 0f;
	
	private float turnCompensation = 0f, lastYaw = 0f;
	private float speedCompensation = 0f;
	private Vector3f lastPos = new Vector3f();
	
	public PrecipitationEmitter() {
		super(Resources.getTexture("particles"), 50, .175f, 0f, 50, .4f);
		origin = new Vector3f();
		this.setOrigin(origin);
		dropDirection = new Vector3f(0, -1, 0);
		this.setTextureAtlasRange(1,1);
		this.setDirection(dropDirection, .0f);
	}

	public void update(Camera camera, float weather) {
		this.setDirection(dropDirection, (weather+.55f)/25f);
		dripTimer +=  Window.deltaTime;
		final float dropDelay = 1.01f + weather;
		if (dripTimer >= dropDelay) {
			
			turnCompensation = Math.abs(camera.getYaw() - lastYaw);
			lastYaw = camera.getYaw();
			speedCompensation = Vector3f.distance(lastPos, camera.getPosition())*25;
			lastPos.set(camera.getPosition());
			
			final Frustum frustum = camera.getFrustum();
			final Plane topPlane = frustum.getPlanes()[3];
			final Vector3f cameraLook = new Vector3f(camera.getDirectionVector());
			final Vector3f cameraStrafe = new Vector3f(-cameraLook.z, 0, cameraLook.x);

			if (topPlane.normal.y < -.1f) {
				int t = (topPlane.normal.y < -.4f) ? 1 : 4;
				this.setTextureAtlasRange(t,t);
				
				for(int i = -4; i <= 4; i += 2) {
					origin.set(camera.getPosition());
					origin.x -= 1f - (Math.random()*2f);
					origin.z -= 1f - (Math.random()*2f);
					origin.add(Vector3f.mul(cameraLook, 4.5f + ((float)Math.random() * 20f)));
					origin.add(Vector3f.mul(cameraStrafe, i));
					origin.set(topPlane.projectPoint(origin));
					origin.y++;
					
					for(float j = -1f; j < Math.max(turnCompensation, speedCompensation); j += 15f) {
						generateParticles(camera);
						origin.y -= 1f;
					}
					
				}
			} else {
				this.setTextureAtlasRange(5,5);
				for(int i = 0; i < 8; i++) {
					origin.set(camera.getPosition());
					origin.x -= 4f - (Math.random()*8f);
					origin.z -= 4f - (Math.random()*8f);
					origin.y += 5;
					
					for(float j = -1f; j < speedCompensation; j += 15f) {
						generateParticles(camera);
						origin.y -= 1f;
					}
				}
			}
			
			dripTimer -= dropDelay-(Math.random()/4f);
		}
	}
}
