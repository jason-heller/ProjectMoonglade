package scene.entity;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import gl.Camera;
import gl.Window;
import gl.post.PostProcessing;
import io.Input;
import map.Enviroment;
import map.Material;
import procedural.biome.BiomeData;
import procedural.biome.BiomeVoronoi;
import scene.Scene;
import scene.entity.utility.PhysicsEntity;
import scene.overworld.Overworld;
import util.MathUtil;

public class PlayerHandler {
	private static PhysicsEntity entity;

	public static float jumpVelocity = 8f;
	public static float friction = 15f, airFriction = 0f;
	public static float maxSpeed = 80f, maxAirSpeed = 4f, maxWaterSpeed = 32f;
	public static float accelSpeed = 80f, airAccel = 3f, waterAccel = 32f;

	private static final float CAMERA_STANDING_HEIGHT = 1.6f;
	private static final float CAMERA_CROUCHING_HEIGHT = 1f;
	
	private static float cameraHeight = CAMERA_STANDING_HEIGHT;
	private static float height = 1.6f;
	private static float width = .45f;
	
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
				if (walkSfxTimer >=.085f) {
					walkSfxTimer = 0f;
				}
				
				if (walkSfxTimer == 0f) {
					if (entity.getStoodOnMaterial() == Material.NONE) {
						Enviroment env = ((Overworld)scene).getEnviroment();
						BiomeVoronoi biomeVoronoi = env.getBiomeVoronoi();
						BiomeData biomeCellData = biomeVoronoi.getDataAt(entity.position.x, entity.position.z);
						switch(biomeCellData.getMainBiome().getName()) {
						case "Snowy Pine Forest":
						case "Cold Mountain":
							AudioHandler.play("walk_snow");
							break;
						case "Chaparrel":
						case "Desert":
							AudioHandler.play("walk_dirt");
							break;
						case "Marsh":
						case "Mangrove Forest":
						case "Swamp":
							AudioHandler.play("walk_mud");
							break;
						default:
							AudioHandler.play("walk_grass");
						}
					} else {
						switch(entity.getStoodOnMaterial()) {
						case SHEET_METAL:
						case METAL_MESH:
							AudioHandler.play("walk_metal");
							break;
						case PLANKS:
						case PALM_PLANKS:
						case CYPRESS_PLANKS:
							AudioHandler.play("walk_wood");
							break;
						case CONCRETE:
						case STONE_WALL:
						case STONE_BRICK:
							AudioHandler.play("walk_asphalt");
							break;
						default:
							AudioHandler.play("walk_grass");
						}
					}
				}
				
				walkSfxTimer += Window.deltaTime*.2f;
				
				
			} else {
				walkSfxTimer = 0f;
			}
		} else {
			Vector3f oldCamPos = camera.getPrevPosition();
			entity.velocity.set(Vector3f.sub(camera.getPosition(), oldCamPos).div(Window.deltaTime));
			entity.position.set(oldCamPos.x, oldCamPos.y - cameraHeight, oldCamPos.z);
			entity.previouslyGrounded = true;
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
			Vector3f dir = new Vector3f(scene.getCamera().getDirectionVector());
			entity.velocity.x = dir.x * entity.velocity.y;
			entity.velocity.z = dir.z * entity.velocity.y;
			entity.jump(jumpVelocity);
		}
	}
	
	private static void waterPhysics(Scene scene) {
		float forwardSpeed = 0, strafeSpeed = 0;
		
		boolean A = Input.isDown("walk_left"),
				D = Input.isDown("walk_right"),
				W = Input.isDown("walk_forward"),
				S = Input.isDown("walk_backward"),
				JUMP = Input.isDown("jump"),
				CROUCH = Input.isDown("sneak");
		
		if (A && D) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (A && !D) {
			strafeSpeed = 60;
		} else if (!A && D) {
			strafeSpeed = -60;
		}
		
		if (W && S) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (W && !S) {
			forwardSpeed = -60;
			
		} else if (!W && S) {
			forwardSpeed = 60;
		}
		
		if (Input.isPressed(Keyboard.KEY_R) && entity.isFullySubmerged()) {
			forwardSpeed = -4000;
		}
		
		if (JUMP && !CROUCH) {
			entity.accelerate(Vector3f.Y_AXIS, 60);
		} else if (!JUMP && CROUCH) {
			entity.accelerate(Vector3f.Y_AXIS, -60);
		}
		
		final Vector3f forward = MathUtil.getDirection(scene.getCamera().getViewMatrix());
		final float yawRad = (float) Math.toRadians(scene.getCamera().getYaw());
		final Vector3f strafe = new Vector3f(-(float) Math.sin(yawRad), 0, (float) Math.cos(yawRad)).perpindicular();
		
		if (!entity.isFullySubmerged()) {
			forward.y = Math.max(forward.y, 0f);
			strafe.y = Math.max(strafe.y, 0f);
		}
		
		entity.accelerate(forward, forwardSpeed);
		entity.accelerate(strafe, strafeSpeed);
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
