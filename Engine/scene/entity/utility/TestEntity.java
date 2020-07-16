package scene.entity.utility;

import org.joml.Vector3f;

import gl.Window;
import scene.Scene;
import scene.entity.Entity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class TestEntity extends Entity {

	public TestEntity(Vector3f position) {
		super("test", "default");
		this.position.set(position);
		animator.loop("test");
	}
	
	@Override
	public void update(Scene scene) {
		this.animator.update();
		this.position.z += Window.deltaTime;
		super.update(scene);
	}

	@Override
	public void save(RunLengthOutputStream data) {

	}

	@Override
	public void load(RunLengthInputStream data) {

	}
}
