package scene.entity;

import geom.AABB;
import scene.Scene;
import scene.entity.utility.PhysicsEntity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;


public class PlayerEntity extends PhysicsEntity {

	private int jumpSfx;
	
	public PlayerEntity(Scene scene) {
		super(null, null);
		position.set(scene.getCamera().getPosition());
		visible = false;
		PlayerControl.setEntity(this);
	}

	@Override
	public void update(Scene scene) {
		if (this.getChunk() == null) return;
		PlayerControl.update(scene);
		super.update(scene);
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
}
