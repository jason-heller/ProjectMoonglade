package scene.overworld;

import core.Resources;
import scene.Scene;
import scene.entity.PhysicsEntity;
import scene.entity.PlayerControl;


public class PlayerEntity extends PhysicsEntity {

	private int jumpSfx;
	
	public PlayerEntity(Scene scene) {
		super(null, null);
		position.set(scene.getCamera().getPosition());
		visible = false;
		PlayerControl.setEntity(this);
	}

	@Override
	public void tick(Scene scene) {
		//super.tick(scene);
	}
	
	public void update(Scene scene) {
		if (this.getChunk() == null) return;
		PlayerControl.update(scene);
		super.tick(scene);
	}
	
	@Override
	public void jump(float height) {
		super.jump(height);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
