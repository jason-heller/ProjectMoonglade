package scene.entity.object;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import geom.AABB;
import geom.Plane;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.PlayerEntity;
import scene.overworld.Overworld;
import util.MathUtil;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class BedEntity extends Entity {
	private float swingForce = 0f;
	
	public BedEntity(Vector3f position, float rot) {
		super();
		this.position.set(position);
		this.rotation.y = rot;
		this.persistency = 2;

		this.setModel(Resources.getModel("bed"));
		this.setDiffuse(Resources.getTexture("bed"));
		
		id = 5;
		
		aabb = new AABB(position.x, position.y+1f, position.z, .75f, 1f, .75f);
	}
	
	@Override
	public void update(Scene scene) {
		rotation.y += swingForce;
		
		Overworld ow = (Overworld)scene;
		PlayerEntity player = ow.getPlayer();
		
		aabb.setCenter(position.x, position.y+1, position.z);
		
		if (player.getAABB().intersects(aabb)) {
		}
		
		super.update(scene);
	}
	
	@Override
	public void tick(Scene scene) {
		
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
