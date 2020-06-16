package scene.overworld;

import org.joml.Vector3f;

import core.Resources;
import gl.Window;
import gl.particle.Particle;
import scene.Scene;
import scene.entity.Entity;

public class FireflyEntity extends Entity {
	
	private final float flyYawDiff = .04f;
	private final float flyAltDiff = .01f;
	
	private float thinkTimer = 0f, hoverTimer = 0f, blinkTimer = 0f;
	private final float decisionTime = 6.28f;//12.56f;
	private float y = 0;

	public FireflyEntity(Scene scene) {
		super("firefly", "entity_sheet1");
		scale = .25f;
		blinkTimer = 3 + (float)Math.random() * 7;
	}

	@Override
	public void tick(Scene scene) {
		if (this.getChunk() == null) return;
		
		thinkTimer += Window.deltaTime;
		blinkTimer -= Window.deltaTime;
		
		if (blinkTimer < .3f && scale > 0) {
			this.scale = -scale;
			float life = 10f + ((float)Math.random()*20f);
			new Particle(Resources.getTexture("particles"), position, new Vector3f(), 0f, life, 0, 0, .5f, 2, 2);
		}
		
		if (blinkTimer <= 0) {
			this.scale = -scale;
			blinkTimer = 3 + (float)Math.random() * 7;
		}
		
		float floor = chunk.heightLookup((int)position.x-chunk.realX, (int)position.z-chunk.realZ);
		float hoverY = (float)Math.sin(hoverTimer);
		
		
		if (y > floor + 6) {
			y = floor + 6;
		}
		
		if (y+hoverY <= floor) {
			y = floor;
			velocity.zero();
			
			if (thinkTimer > decisionTime) {
				thinkTimer = 0;
				velocity.x = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
				velocity.z = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
				velocity.y = ((float)Math.random()*flyAltDiff);
			}
		} else if (thinkTimer > decisionTime) {
			velocity.x = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
			velocity.z = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
			velocity.y = (-flyAltDiff/2f) + ((float)Math.random()*flyAltDiff);
			
			thinkTimer = 0f;
		} else {
			hoverTimer += Window.deltaTime;
			position.y = y + hoverY;
		}

		position.x += velocity.x;
		position.z += velocity.z;
		y += velocity.y;
		super.tick(scene);
	}
}
