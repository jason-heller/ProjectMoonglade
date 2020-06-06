package scene.overworld;

import scene.Scene;
import scene.entity.PhysicsEntity;
import scene.entity.PlayerControl;


public class PlayerEntity extends PhysicsEntity {

	public PlayerEntity(Scene scene) {
		super(null, null);
		position.set(scene.getCamera().getPosition());
		visible = false;
		PlayerControl.setEntity(this);
	}

	@Override
	public void update(Scene scene) {
		PlayerControl.update(scene);
		super.update(scene);
	}
}
