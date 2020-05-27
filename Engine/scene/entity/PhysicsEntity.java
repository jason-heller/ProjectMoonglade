package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Application;
import core.Globals;
import core.Resources;
import core.Window;
import dev.Console;
import io.Input;
import map.Chunk;
import map.Terrain;
import scene.Scene;
import scene.overworld.Overworld;

public class PhysicsEntity extends Entity {
	
	private static final float DEFAULT_FRICTION = 3f;

	private static final float ALLOWABLE_STEP = .35f;
	
	private boolean grounded = false;
	private boolean previouslyGrounded = false;
	private boolean sliding = false;
	private final boolean submerged = false;
	private boolean climbing = false;

	public float maxSpeed = 25f, maxAirSpeed = 5f;
	public float friction = DEFAULT_FRICTION;
	public float airFriction = 0f;
	
	float width;
	float height;

	public boolean visible = true;
	
	public PhysicsEntity() {
		this(null, null);
	}

	public PhysicsEntity(String model, String diffuse) {
		position = new Vector3f();
		rotation = new Vector3f();
		velocity = new Vector3f();
		scale = 1f;
		visible = true;
		matrix = new Matrix4f();
		this.model = model == null ? null : Resources.getModel(model);
		this.diffuse = diffuse == null ? null : Resources.getTexture(diffuse);
		EntityControl.addEntity(this);
	}

	public void accelerate(Vector3f dir, float amount) {
		if (climbing) {
			velocity.y += amount * Window.deltaTime;

			velocity.x += dir.x * amount * Window.deltaTime;
			velocity.z += dir.z * amount * Window.deltaTime;
		} else {
			final float projVel = Vector3f.dot(velocity, dir); // Vector projection of Current velocity onto accelDir.
			float accelVel = amount * Window.deltaTime; // Accelerated velocity in direction of movment

			// If necessary, truncate the accelerated velocity so the vector projection does
			// not exceed max_velocity
			final float speedCap = submerged && !grounded ? maxSpeed * 2.8f : grounded ? maxSpeed : maxAirSpeed;
			// if (projVel + accelVel < -speedCap)
			// accelVel = -speedCap - projVel;

			if (projVel + accelVel > speedCap) {
				accelVel = speedCap - projVel;
			}

			velocity.x += dir.x * accelVel;
			velocity.y += dir.y * accelVel;
			velocity.z += dir.z * accelVel;
		}
	}

	private void collide(Terrain terrain) {
		Chunk chunk = terrain.getChunkAt(position.x, position.z);

		if (chunk == null) {
			return;
		}
		
		final float relx = position.x - (chunk.x * Chunk.CHUNK_SIZE);
		final float relz = position.z - (chunk.z * Chunk.CHUNK_SIZE);
		int tx = ((int) Math.floor(relx / Chunk.POLYGON_SIZE))*2 + 1;
		int tz = ((int) Math.floor(relz / Chunk.POLYGON_SIZE))*2 + 1;
		
		// TODO: Spaghetti code ahead, refactor
		
		// Top
		testWall(chunk, tx, tz - 1, tx + 1, tz - 1, 2);
		// Bottom
		testWall(chunk, tx, tz + 2, tx + 1, tz + 2, 3);
		// Left
		testWall(chunk, tx - 1, tz, tx - 1, tz + 1, 0);
		// Right
		testWall(chunk, tx + 2, tz, tx + 2, tz + 1, 1);
		
		//final float minHeight = chunk.getPolygon(position.x, position.z).barryCentric(position.x, position.z);
		final float w = width-.01f;
		final float minHeight = Math.max(
				terrain.getChunkAt(position.x+w, position.z-w).getPolygon(position.x+w, position.z-w).barryCentric(position.x+w, position.z-w), Math.max(
				terrain.getChunkAt(position.x-w, position.z-w).getPolygon(position.x-w, position.z-w).barryCentric(position.x-w, position.z-w), Math.max(
				terrain.getChunkAt(position.x-w, position.z+w).getPolygon(position.x-w, position.z+w).barryCentric(position.x-w, position.z+w), 
				terrain.getChunkAt(position.x+w, position.z+w).getPolygon(position.x+w, position.z+w).barryCentric(position.x+w, position.z+w))));
		
		
		if (position.y <= minHeight || position.y <= minHeight + ALLOWABLE_STEP && previouslyGrounded) {
			position.y = minHeight;
			grounded = true;
			velocity.y = 0;
		} else {
			grounded = false;
		}
		
		/*if (position.y <= minHeight || position.y <= minHeight + ALLOWABLE_STEP && previouslyGrounded) {
			if (position.y >= minHeight - ALLOWABLE_STEP) {
				position.y = minHeight;
				grounded = true;
				velocity.y = 0;
			}
		} else {
			grounded = false;
		}*/
	}

	private void testWall(Chunk chunk, int rx1, int rz1, int rx2, int rz2, int n) {
		float y1 = chunk.heightmap[rx1][rz1];
		float y2 = chunk.heightmap[rx2][rz2];
	
		int x1 = ((rx1/2) * Chunk.POLYGON_SIZE) + (chunk.x * Chunk.CHUNK_SIZE);
		//int x2 = ((rx2/2) * Chunk.POLYGON_SIZE) + (chunk.x * Chunk.CHUNK_SIZE);
		int z1 = ((rz1/2) * Chunk.POLYGON_SIZE) + (chunk.z * Chunk.CHUNK_SIZE);
		//int z2 = ((rz2/2) * Chunk.POLYGON_SIZE) + (chunk.z * Chunk.CHUNK_SIZE);
		switch(n) {
		case 0:
			if (y1 == chunk.heightmap[rx1+1][rz1] && y2 == chunk.heightmap[rx2+1][rz2])
				return;
			
			if (y1 + ((y2 - y1) * ((position.z % Chunk.POLYGON_SIZE) / Chunk.POLYGON_SIZE)) <= position.y
					+ ALLOWABLE_STEP) {
				return;
			}
			if (position.x - width < x1) {
				position.x = x1 + width;
				velocity.x = 0;
			}
			break;
		case 1:
			if (y1 == chunk.heightmap[rx1-1][rz1] && y2 == chunk.heightmap[rx2-1][rz2])
				return;
			
			if (y1 + ((y2 - y1) * ((position.z % Chunk.POLYGON_SIZE) / Chunk.POLYGON_SIZE)) <= position.y
					+ ALLOWABLE_STEP) {
				return;
			}
			if (position.x + width > x1) {
				position.x = x1 - width;
				velocity.x = 0;
			}
			break;
		case 2:
			if (y1 == chunk.heightmap[rx1][rz2+1] && y2 == chunk.heightmap[rx2][rz2+1])
				return;
			
			if (y1 + ((y2 - y1) * ((position.x % Chunk.POLYGON_SIZE) / Chunk.POLYGON_SIZE)) <= position.y
					+ ALLOWABLE_STEP) {
				return;
			}
			if (position.z - width < z1) {
				position.z = z1 + width;
				velocity.z = 0;
			}
			break;
		case 3:
			if (y1 == chunk.heightmap[rx1][rz2-1] && y2 == chunk.heightmap[rx2][rz2- 1])
				return;
			
			if (y1 + ((y2 - y1) * ((position.z % Chunk.POLYGON_SIZE) / Chunk.POLYGON_SIZE)) <= position.y
					+ ALLOWABLE_STEP) {
				return;
			}
			if (position.z + width > z1) {
				position.z = z1 - width;
				velocity.z = 0;
			}
			break;
		}
	}

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isSliding() {
		return sliding;
	}

	public boolean isSubmerged() {
		return submerged;
	}

	public void jump(float height) {
		if (climbing) {
			velocity.x = -velocity.x;
			velocity.z = -velocity.z;
			velocity.y = height;
			climbing = false;
			grounded = false;
			sliding = false;

			previouslyGrounded = false;
		} else {
			velocity.y = height;
			grounded = false;
			sliding = false;
			previouslyGrounded = false;
		}
	}

	@Override
	public void update(Scene scene) {
		if (!submerged && !climbing) {
			velocity.y = Math.max(velocity.y - Globals.gravity * Window.deltaTime, Globals.maxGravity);
		}

		if (!climbing) {
			position.x += velocity.x * Window.deltaTime;
			position.z += velocity.z * Window.deltaTime;
		}

		position.y += velocity.y * Window.deltaTime;

		// Friction
		if (!sliding && previouslyGrounded || submerged) {
			final float speed = velocity.length();
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				if (submerged) {
					drop /= 2;
					grounded = false;
				}
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.mul(offset); // Scale the velocity based on friction.
			}
		} else if (climbing) {
			final float speed = Math.abs(velocity.y);
			if (speed != 0) {
				final float drop = speed * friction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.y *= offset;
				velocity.x = Math.signum(velocity.x) * velocity.y;
				velocity.z = Math.signum(velocity.z) * velocity.y;
			}
		}

		else if (airFriction != 0f && !sliding && !submerged) {
			final float speed = new Vector2f(velocity.x, velocity.z).length();
			if (speed != 0f) {
				final float drop = speed * airFriction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.set(velocity.x * offset, velocity.y, velocity.z * offset); // Scale the velocity based on
																					// friction.
			}
		}

		previouslyGrounded = grounded;

		collide(((Overworld) Application.scene).getEnviroment().getTerrain());

		super.update(scene);
	}
}
