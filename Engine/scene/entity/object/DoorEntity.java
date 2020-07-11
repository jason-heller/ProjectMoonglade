package scene.entity.object;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import geom.AABB;
import geom.Plane;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.PlayerEntity;
import scene.overworld.Overworld;
import scene.overworld.inventory.Inventory;
import util.MathUtil;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class DoorEntity extends Entity {
	private float swingForce = 0f, initPos = 0, endPos = 90;
	
	public DoorEntity(Vector3f position, float rotation) {
		super();
		this.position.set(position);
		this.rotation.y = rotation;
		this.initPos = rotation;
		this.endPos = rotation + 90;
		this.persistency = 2;

		this.setModel(Resources.getModel("door"));
		this.setDiffuse(Resources.getTexture("entity_sheet1"));
		
		id = 3;
		
		aabb = new AABB(position.x, position.y+1f, position.z, .75f, 2f, .75f);
		
		clickable = true;
	}
	
	@Override
	public void onClick(boolean lmb, Inventory inv) {
		Overworld ow = (Overworld)Application.scene;
		PlayerEntity player = ow.getPlayer();
		Vector3f playerPos = player.position;
		
		Plane plane = new Plane(position, MathUtil.eulerToVectorDeg(this.rotation.y, 0f));
		float dist = (float) plane.signedDistanceTo(playerPos)*4f;
		/*if (dist >= 0) {
			swingForce = 2;
		} else {
			swingForce = -2;
		}*/
		swingForce = dist;
	}
	
	@Override
	public void update(Scene scene) {
		rotation.y += swingForce;
		
		Overworld ow = (Overworld)scene;
		PlayerEntity player = ow.getPlayer();
		
		aabb.setCenter(position.x, position.y+1, position.z);
		
		if (player.getAABB().intersects(aabb)) {
			Vector3f playerPos = player.position;
			Plane plane = new Plane(position, MathUtil.eulerToVectorDeg(this.rotation.y, 0f));

			swingForce = (float) plane.signedDistanceTo(playerPos)*4f;
		}
		
		if (rotation.y < initPos) {
			swingForce = Math.abs(swingForce/2f);
		}
		
		if (rotation.y > endPos) {
			swingForce = -Math.abs(swingForce/2f);
		}
		
		if (swingForce > 0) {
			swingForce -= .25f;
			
			if (swingForce < 0)
				swingForce = 0;
		}
		else if (swingForce < 0) {
			swingForce += .25f;
			
			if (swingForce > 0)
				swingForce = 0;
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
		data.writeFloat(rotation.y);
	}

	@Override
	public void load(RunLengthInputStream data) {
		position.set(data.readFloat(), data.readFloat(), data.readFloat());
		this.rotation.y = data.readFloat();
	}
}
