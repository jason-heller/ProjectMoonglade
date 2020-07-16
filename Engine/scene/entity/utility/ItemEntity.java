package scene.entity.utility;

import org.joml.Vector3f;

import core.Resources;
import gl.Window;
import scene.Scene;
import scene.overworld.Overworld;
import scene.overworld.inventory.Item;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class ItemEntity extends PhysicsEntity {
	private int item;
	private int quantity;
	private float life = 0f;
	
	private static final float MAX_LIFE = 300;

	public ItemEntity(Vector3f position, int item, int quantity) {
		super(null, null);
		this.position.set(position);
		this.item = item;
		this.quantity = quantity;
		this.scale = .5f;
		this.persistency = 2;

		this.setModel(Resources.getModel("cube"));
		this.setDiffuse(Resources.getTexture("items"));// items

		id = 1;
	}
	
	public ItemEntity(Vector3f position, String name, int quantity) {
		this(position, Item.getId(name), quantity);
	}

	@Override
	public void update(Scene scene) {
		super.update(scene);
	}
	
	@Override
	public void tick(Scene scene) {
		
		this.rotation.y += Window.deltaTime*15f;
		
		Overworld ow = (Overworld)scene;
		Vector3f playerPos = ow.getPlayer().position;
		if (life > .25f && Vector3f.distanceSquared(position, playerPos) < 2) {
			destroy();
			//source.play("collect");
			ow.getInventory().addItem(item, quantity);
			return;
		}
		
		life += Window.deltaTime;
		if (life > MAX_LIFE) {
			destroy();
			return;
		}
	}
	
	@Override
	public void save(RunLengthOutputStream data) {
		data.writeFloat(position.x);
		data.writeFloat(position.y);
		data.writeFloat(position.z);
		data.writeInt(item);
		data.writeByte(quantity);
	}

	@Override
	public void load(RunLengthInputStream data) {
		position.set(data.readFloat(), data.readFloat(), data.readFloat());
		this.item = data.readInt();
		
		this.quantity = data.readByte();
	}
	
	public int getItem() {
		return item;
	}
}
