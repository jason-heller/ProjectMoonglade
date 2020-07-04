package scene.entity;

import org.joml.Vector3f;

import gl.Camera;
import gl.Window;
import gl.post.PostProcessing;
import io.Input;
import scene.Scene;
import scene.entity.utility.PhysicsEntity;
import util.MathUtil;

public class PlayerHandler {
	private static PhysicsEntity entity;

	public static float jumpVelocity = 6f;
	public static float friction = 15f, airFriction = 0f;
	public static float maxSpeed = 80f, maxAirSpeed = 4.2f, maxWaterSpeed = 32f;
	public static float accelSpeed = 80f, airAccel = 1.56f, waterAccel = 32f;

	private static final float CAMERA_STANDING_HEIGHT = 1.6f;
	private static final float CAMERA_CROUCHING_HEIGHT = 1f;
	
	private static float cameraHeight = CAMERA_STANDING_HEIGHT;
	private static float height = 1.6f;
	private static float width = .5f;
	
	private static float walkSfxTimer = 0f;

	private static boolean disabled = false;

	public static PhysicsEntity getEntity() {
		return entity;
	}

	private static void passPhysVars() {
		entity.maxSpeed = maxSpeed;
		entity.maxAirSpeed = maxAirSpeed;
		entity.maxWaterSpeed = maxWaterSpeed;
		entity.friction = friction;
		entity.airFriction = airFriction;
		entity.width = width;
		entity.height = height;
		entity.getAABB().setBounds(width/2f, height/2f, width/2f);
	}

	public static void setEntity(PhysicsEntity entity) {
		PlayerHandler.entity = entity;
		enable();
	}

	public static void update(Scene scene) {
		passPhysVars();
		if (disabled) {
			return;
		}
		
		float speed = 0;
		final Camera cam = scene.getCamera();
		final float yaw = cam.getYaw();
		float direction = yaw;
		
		final boolean A = Input.isDown("walk_left"), D = Input.isDown("walk_right"), W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"), JUMP = Input.isDown("jump");
		final boolean CTRL = Input.isDown("sneak");
		
		PostProcessing.underwater = entity.isFullySubmerged();

		if (entity.isSubmerged()) {
			waterPhysics(scene);
		} else if (entity.isClimbing()) {
			climbingPhysics(scene);
		} else {
			// Handle game logic per tick, such as movement etc
			if (A && D) {
			} else if (A) {
				direction = yaw + 90;
				speed = accelSpeed;
			} else if (D) {
				direction = yaw - 90;
				speed = accelSpeed;
			}
	
			if (W && S) {
			} else if (S && !getEntity().isSliding()) {
				if (direction != yaw) {
					direction += 45 * (direction > yaw ? -1f : 1f);
				}
	
				speed = accelSpeed;
			} else if (W && !getEntity().isSliding()) {
	
				if (direction != yaw) {
					direction -= 45 * (direction > yaw ? -1f : 1f);
				} else {
					direction = yaw + 180;
				}
	
				speed = accelSpeed;
			}
	
			if ((getEntity().isGrounded() || getEntity().isSubmerged() && getEntity().velocity.y < 0) && JUMP) {
				getEntity().jump(jumpVelocity);
				if (CTRL) {
					getEntity().position.y += cameraHeight - CAMERA_CROUCHING_HEIGHT;
				}
			}
	
			if (speed != 0) {
				if (CTRL) {
					speed /= 2;
				}
	
				if (entity.isSubmerged()) {
					speed = waterAccel;
				}
				else if (!entity.isGrounded()) {
					speed = airAccel;
				}
	
				direction *= Math.PI / 180f;
				getEntity().accelerate(new Vector3f(-(float) Math.sin(direction), 0, (float) Math.cos(direction)), speed);
			}
		}
	
		final Camera camera = scene.getCamera();
		// TODO: Fps != 120 makes for bad times
		if (CTRL) {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_CROUCHING_HEIGHT, .16f);
		} else {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_STANDING_HEIGHT, .16f);
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			camera.getPosition().set(getEntity().position.x, getEntity().position.y + cameraHeight,
					getEntity().position.z);
			
			if (entity.isGrounded() && !entity.isSubmerged() && (W || A || S || D)) {
				if (walkSfxTimer >= .7f-entity.velocity.length()/9f) {
					walkSfxTimer = 0f;
				}
				
				if (walkSfxTimer == 0f) {
					entity.getSource().play("walk_grass");
				}
				
				walkSfxTimer += Window.deltaTime*.2f;
				
				
			} else {
				walkSfxTimer = 0f;
			}
		} else {
			entity.position.set(camera.getPosition());
			entity.velocity.zero();
		}
		
	}
	
	private static void climbingPhysics(Scene scene) {
		float pitch = scene.getCamera().getPitch();
		
		boolean A = Input.isDown("walk_left"),
				D = Input.isDown("walk_right"),
				W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"),
				JUMP = Input.isDown("jump");
		
		if ((W || A || D) && !S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? 1 : -1));
		} else if (S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? -1 : 1));
		}
		
		if (JUMP) {
			Vector3f dir = MathUtil.getDirection(scene.getCamera().getViewMatrix());
			entity.velocity.x = dir.x * entity.velocity.y;
			entity.velocity.z = dir.z * entity.velocity.y;
			entity.jump(jumpVelocity);
		}
	}
	
	private static void waterPhysics(Scene scene) {
		float speed = 0;
		Vector3f dir = MathUtil.getDirection(scene.getCamera().getViewMatrix());
		
		boolean A = Input.isDown("walk_left"),
				D = Input.isDown("walk_right"),
				W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"),
				JUMP = Input.isDown("jump");
		
		if (W && S) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (W && !S) {
			speed = 70;
			dir.negate();
		} else if (!W && S) {
			speed = 70;
		}
		
		
		
		float direction = scene.getCamera().getYaw();
		if (A && D) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (A && !D) {
			speed = waterAccel;
			direction += 90;
		} else if (!A && D) {
			speed = waterAccel;
			direction -= 90;
		}
		
		if (JUMP) {
			speed = 40;
			dir.set(0,1,0);
		}
		
		if (speed != 0) {
			if (direction != scene.getCamera().getYaw()) {
				direction *= Math.PI / 180f;
				entity.accelerate(new Vector3f(-(float)Math.sin(direction),0,(float)Math.cos(direction)), speed);
			} else {
				entity.accelerate(dir, speed);
				if (entity.isGrounded() && dir.y > 0.15 && speed > 0) {
					entity.position.y++;
				}
			}
		}
	}
	
	public static void disable() {
		disabled = true;
		entity.deactivated = true;
	}
	
	public static void enable() {
		disabled = false;
		entity.deactivated = false;
	}
}
