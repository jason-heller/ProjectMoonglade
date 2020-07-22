package scene.entity;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import core.Resources;
import geom.AABB;
import gl.Camera;
import gl.particle.ParticleEmitter;
import scene.Scene;
import scene.entity.utility.LivingEntity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;


public class PlayerEntity extends LivingEntity {
	
	private ParticleEmitter pe;
	private boolean disable = false;
	private float lastHeight = 0;

	public PlayerEntity(Scene scene) {
		super(null, null, 10);
		position.set(scene.getCamera().getPosition());
		visible = false;
		PlayerHandler.setEntity(this);
		persistency = 3;
		
		pe = new ParticleEmitter(Resources.getTexture("small_particles"),
				40, .02f, .002f, 300, .04f);
		pe.setTextureAtlasRange(0, 4);
		pe.setOrigin(scene.getCamera().getPosition());
		pe.setSpeedError(.002f);
		pe.setScaleError(.02f);
	}

	@Override
	public void update(Scene scene) {
		if (this.getChunk() == null || disable) return;
		PlayerHandler.update(scene);
		super.update(scene);
		
		if (scene.getCamera().getControlStyle() != Camera.SPECTATOR) {
			if (!grounded) {
				if (previouslyGrounded) {
					lastHeight = position.y;
				} else {
					lastHeight = Math.max(lastHeight, position.y);
				}
			}
			else if (grounded && !previouslyGrounded && !submerged) {
				float fallHeight = lastHeight - position.y;
				
				if (fallHeight >= 9) {
					this.hurt((int)((fallHeight-4) / 5), null, 1f);
				}
				
				lastHeight = 0;
			} else if (submerged) {
				lastHeight = 0;
			}
		} else {
			lastHeight = 0;
		}
	}
	
	@Override
	public void die() {
		AudioHandler.play("player_die");
		this.disable = true;
	}
	
	@Override
	public void jump(float height) {
		super.jump(height);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	@Override
	public void save(RunLengthOutputStream data) {
	}

	@Override
	public void load(RunLengthInputStream data) {
	}

	public AABB getAABB() {
		return aabb;
	}
	
	@Override
	public void hurt(int damage, Entity attacker, float invulnerabilityTime) {
		if (this.invulnerabilityTimer == 0f && isHurtable()) {
			Camera camera = Application.scene.getCamera();
			
			Vector3f attackDir;
			if (attacker == null) {
				attackDir = new Vector3f(-1f + (float)Math.random()*2f, -1f + (float)Math.random()*2f, -1f + (float)Math.random()*2f);
			} else {
				attackDir = Vector3f.sub(attacker.position, position).normalize();
			}
			camera.flinch(attackDir, 10);
			
			pe.setDirection(new Vector3f(attackDir).negate(), .05f);
			
			for(int i = 0; i < 8; i++) {
				
				pe.generateParticles(camera);
			}
			
			super.hurt(damage, attacker, invulnerabilityTime);
			
			if (hp > 0) {
				AudioHandler.play("player_hurt");
			}
		}
	}

	public boolean isDisabled() {
		return disable;
	}

	public void setDisabled(boolean disable) {
		this.disable = disable;
	}

}
