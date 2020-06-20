package scene.entity.utility;

import org.joml.Vector3f;

import scene.entity.Entity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class MarkerEntity extends Entity {

	public MarkerEntity(Vector3f position) {
		super("cube", "default");
		this.position.set(position);
	}

	@Override
	public void save(RunLengthOutputStream data) {

	}

	@Override
	public void load(RunLengthInputStream data) {

	}
}
