package scene.entity;

public class TestEntity extends Entity {
	public TestEntity(float x, float y, float z) {
		super("cube", "default");
		this.position.set(x,y,z);
		this.position.sub(-.25f, -.25f, -.25f);
		this.scale = .2f;
	}
}
