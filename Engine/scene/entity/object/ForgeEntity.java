package scene.entity.object;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import geom.AABB;
import gl.Window;
import gl.particle.Particle;
import gl.particle.ParticleEmitter;
import gl.res.Texture;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class ForgeEntity extends Entity {
	private int[] input;
	private float[] timers;
	private int nextEmptySlot = 0;
	
	private final Texture prtTex = Resources.getTexture("small_particles");
	
	private final int TOTAL_SLOTS = 5;
	
	public ForgeEntity(Vector3f position, float rot) {
		super("forge", "entity_sheet1");
		this.position.set(position);
		this.persistency = 2;
		this.rotation.y = rot;
		
		input = new int[TOTAL_SLOTS];
		timers = new float[TOTAL_SLOTS];
		
		id = 6;
		
		//aabb = new AABB(position.x, position.y, position.z, .5f, .5f, .5f);
		aabb = new AABB(position.x, position.y, position.z, 1f, 1f, 1f);
		
		clickable = true;
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}
	
	@Override
	public void onClick(boolean lmb, Inventory inv) {

		if (nextEmptySlot == TOTAL_SLOTS) return;
		
		input[nextEmptySlot] = inv.getSelected();
		timers[nextEmptySlot] = 1;
		
		inv.consume(inv.getSelectionPos());
		
		for(; nextEmptySlot < TOTAL_SLOTS; nextEmptySlot++) {
			if (input[nextEmptySlot] == 0) {
				break;
			}
		}
	}

	@Override
	public void tick(Scene scene) {
		boolean active = false;
		float vx = (float)Math.sin(Math.toRadians(rotation.y));
		float vz = (float)Math.cos(Math.toRadians(rotation.y));
		
		for(int i = 0; i < TOTAL_SLOTS; i++) {
			if (timers[i] > 0) {
				timers[i] -= Window.deltaTime;
				active = true;
				
				if (timers[i] <= 0) {
					barfOutItem(input[i], vx, vz);
					input[i] = 0;
					
					if (i < nextEmptySlot) {
						nextEmptySlot = i;
					}
				}
			}
		}
		
		if (active) {
			// Particles
			for(int i = 0; i < 3; i++) {
				Vector3f pos = new Vector3f(position);
				pos.x += -.4f + (float)Math.random()*.8f;
				pos.y += .2f + (float)Math.random()*.8f;
				pos.z += -.4f + (float)Math.random()*.8f;
				
				new Particle(prtTex, pos, new Vector3f(vx/50f, 0f, vz/50f), 0f, 100, 0f, 0, .2f, 13, 23);
			}
		} else {
			
		}
	}
	
	private void barfOutItem(int item, float vx, float vz) {
		int output = Item.get("planks").id;
		switch(Item.get(item).getName()) {
		case "metal_mesh":
			output = Item.get("reclaimed_metal").id;
			break;
		case "reclaimed_metal":
			output = item;
			break;
		}
		
		ItemEntity itemEntity = new ItemEntity(position, output, 1);
		float speed = 3f + (float)Math.random()*2f;
		itemEntity.velocity.set(vx*speed, .4f, vz*speed);
		
		EntityHandler.addEntity(itemEntity);
	}

	@Override
	public void save(RunLengthOutputStream data) {
		data.writeFloat(position.x);
		data.writeFloat(position.y);
		data.writeFloat(position.z);
	}

	@Override
	public void load(RunLengthInputStream data) {
		position.set(data.readFloat(), data.readFloat(), data.readFloat());
	}
}
;