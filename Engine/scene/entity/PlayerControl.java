package scene.entity;

import org.joml.Vector3f;

import gl.Camera;
import io.Input;
import scene.Scene;
import util.MathUtil;

public class PlayerControl {
	private static PhysicsEntity entity;

	public static float jumpVelocity = 4f;
	public static float friction = 15f, airFriction = 0f;
	public static float maxSpeed = 12.5f, maxAirSpeed = 3.125f;
	public static float accelSpeed = 31.2f, airAccel = 1.56f, waterAccel = 5f;

	private static final float CAMERA_STANDING_HEIGHT = .59f;
	private static final float CAMERA_CROUCHING_HEIGHT = .375f;
	
	private static float cameraHeight = CAMERA_STANDING_HEIGHT;
	private static float height = .625f;
	private static float width = .23f;

	public static PhysicsEntity getEntity() {
		return entity;
	}

	private static void passPhysVars() {
		entity.maxSpeed = maxSpeed;
		entity.maxAirSpeed = maxAirSpeed;
		entity.friction = friction;
		entity.airFriction = airFriction;
		entity.width = width;
		entity.height = height;
	}

	public static void setEntity(PhysicsEntity entity) {
		PlayerControl.entity = entity;
	}

	public static void update(Scene scene) {
		passPhysVars();
		float speed = 0;
		final Camera cam = scene.getCamera();
		final float yaw = cam.getYaw();
		float direction = yaw;

		final boolean A = Input.isDown("walk_left"), D = Input.isDown("walk_right"), W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"), JUMP = Input.isDown("jump"), CTRL = Input.isDown("sneak");

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

			if (!entity.isGrounded()) {
				speed = airAccel;
			}

			direction *= Math.PI / 180f;
			getEntity().accelerate(new Vector3f(-(float) Math.sin(direction), 0, (float) Math.cos(direction)), speed);
		}

		final Camera camera = scene.getCamera();
		if (CTRL) {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_CROUCHING_HEIGHT, .16f);
		} else {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_STANDING_HEIGHT, .16f);
		}
		
		if (camera.getControlStyle() == Camera.FIRST_PERSON) {
			camera.getPosition().set(getEntity().position.x, getEntity().position.y + cameraHeight,
					getEntity().position.z);
		} else {
			entity.position.set(camera.getPosition());
			entity.velocity.zero();
		}
	}
}
