package scene.entity.hostile;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import gl.particle.ParticleHandler;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import scene.Scene;
import scene.entity.EntityHandler;
import scene.entity.PlayerEntity;
import scene.entity.utility.ItemEntity;
import scene.entity.utility.LivingEntity;
import scene.overworld.Overworld;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import util.MathUtil;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class ZombieEntity extends LivingEntity {
	
	public ZombieEntity(Vector3f position) {
		super("test", "test", 15);
		this.position.set(position);
		animator.loop("test");
		clickable = true;
		this.persistency = 1;
		aabb.setBounds(.45f, 1.6f, .45f);
		
		this.spawnGroupMin = 1;
		this.spawnGroupVariation = 1;
		this.spawnRarity = 0;
	}
	
	@Override
	public void update(Scene scene) {
		this.animator.update();
		Overworld ow = (Overworld)scene;
		Vector3f playerPos = ow.getPlayer().position;
		Vector3f toPlayer = Vector3f.sub(playerPos, position);
		float len = toPlayer.length();
		if (len < .4f) {
			ow.getPlayer().hurt(1, this);
		}
		
		toPlayer.div(len);
		
		rotation.y = 90 + (float) Math.toDegrees(MathUtil.pointDirection(0, 0, velocity.x, velocity.z));
		super.update(scene);
		
		this.accelerate(toPlayer, 10f);
		
	}

	@Override
	protected void onClick(boolean lmb, Inventory inv) {
		if (!lmb) return;
		
		Overworld ow = (Overworld)Application.scene;
		PlayerEntity player = ow.getPlayer();
		
		if (this.invulnerabilityTimer == 0f) {

			AudioHandler.play("hit");
			ParticleHandler.addBurst("small_particles", 0, 3, position);
		}
		
		if (inv.getSelected() == Item.AXE) {
			this.hurt(4, player, .5f);
			
		} else {
			this.hurt(1, player, .5f);
		}
	}
	
	public void die() {
		if (Math.random() < .5) {
			EntityHandler.addEntity(new ItemEntity(position, "health", 1));
		}
		super.die();
	}
	
	@Override
	public void save(RunLengthOutputStream data) {

		System.out.println("SAVD");
	}

	@Override
	public void load(RunLengthInputStream data) {
		System.out.println("LOAD");
	}
	
	@Override
	public boolean spawnConditionsMet(Enviroment enviroment, Terrain terrain, Chunk chunk, float x, float z, int dx, float dy, int dz) {
		float waterHeight = chunk.getWaterHeight(dx, dz);
		
		if (dy < waterHeight)
			return false;
		
		return true;
	}
}
