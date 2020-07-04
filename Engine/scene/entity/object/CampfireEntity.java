package scene.entity.object;

import org.joml.Vector3f;

import audio.Source;
import core.Resources;
import gl.particle.ParticleEmitter;
import scene.Scene;
import scene.entity.Entity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class CampfireEntity extends Entity {

	private boolean lit = true;
	private ParticleEmitter pe;

	public CampfireEntity(float x, float y, float z) {
		super("campfire", "campfire");
		this.scale = .25f;
		this.position = new Vector3f();
		position.x = x;
		position.y = y;
		position.z = z;
		this.velocity = new Vector3f();
		this.persistency = 2;
		
		//source.setAttenuation(1f, 128f, 192f);
		//source.setLooping(true);
		
		pe = new ParticleEmitter(Resources.getTexture("small_particles"),
				40, .01f, 0f, 300, .2f);
		pe.setTextureAtlasRange(13, 23);
		pe.setOrigin(position);
		pe.setDirection(Vector3f.Y_AXIS, .1f);
		pe.setSpeedError(.001f);
		pe.setScaleError(.2f);
		
	}
	
	@Override
	public void update(Scene scene) {
		if (lit) {
			pe.generateParticles(scene.getCamera());
		}
		
		super.update(scene);
	}
	
	public void ignite() {
		source.play("fire");
		lit = true;
	}
	
	public void extinquish() {
		lit = false;
		source.stop();
	}

	@Override
	public void tick(Scene scene) {
		
	}

	@Override
	public void save(RunLengthOutputStream data) {
	}

	@Override
	public void load(RunLengthInputStream data) {
	}
}
