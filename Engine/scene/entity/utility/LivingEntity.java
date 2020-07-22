package scene.entity.utility;

public abstract class LivingEntity extends PhysicsEntity {
	public LivingEntity(String model, String diffuse, int baseHp) {
		super(model, diffuse);
		setHurtable(true);
		hp = baseHp;
		persistency = 1;
	}
}
