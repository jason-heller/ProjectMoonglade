package scene.entity.utility;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.Source;
import core.Application;
import core.Resources;
import geom.AABB;
import gl.Window;
import map.Chunk;
import map.Terrain;
import map.building.BuildingTile;
import scene.Scene;
import scene.entity.Entity;
import scene.overworld.Overworld;

public abstract class PhysicsEntity extends Entity {
	
	private static final float DEFAULT_FRICTION = 3f;

	private static final float ALLOWABLE_STEP = .35f;
	
	private boolean grounded = false;
	private boolean previouslyGrounded = false;
	private boolean sliding = false;
	private boolean submerged = false;
	private boolean climbing = false;
	
	private static float gravity = 14f;
	private static float maxGravity = -20f;
	
	protected AABB aabb;

	public float maxSpeed = 25f, maxAirSpeed = 5f, maxWaterSpeed = 1f;
	public float friction = DEFAULT_FRICTION;
	public float airFriction = 0f;
	
	public float width;
	public float height;

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
		persistency = 3;

		aabb = new AABB(new Vector3f(), new Vector3f(width, height, width));
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
			final float speedCap;
			if (submerged) {
				speedCap = maxWaterSpeed;
			} else {
				speedCap = grounded ? maxSpeed : maxAirSpeed;
			}
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
		
		final float relx = position.x - chunk.realX;
		final float relz = position.z - chunk.realZ;
		int tx = ((int) Math.floor(relx / Chunk.POLYGON_SIZE));
		int tz = ((int) Math.floor(relz / Chunk.POLYGON_SIZE));
		
		submerged = false;
		final float waterHeight = chunk.getWaterHeight(tx, tz);
		if (waterHeight != Float.MIN_VALUE && waterHeight > position.y) {
			submerged = true;
		}
		
		tx = (tx*2)+1;
		tz = (tz*2)+1;
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
		
		collideWithBuildings(terrain);
	}
	
	private void collideWithBuildings(Terrain t) {
		float px = position.x;
		float py = position.y;
		float pz = position.z;
		AABB collider = new AABB(0, 0, 0, 1f, 1f, 1f);
		aabb.set(position.x, position.y + (height/2), position.z);

		for (float x = px - width; x <= px + width; x += 0.5f) {
			for (float y = py - 1f; y <= py + 1f; y += 0.5f) {
				for (float z = pz - width; z <= pz + width; z += 0.5f) {
					//Chunk chunk = t.getChunkAt(x, z);
					//Building building = chunk.getBuilding();
					
					float tx = (float)Math.round(x);
					float ty = (float)Math.round(y);
					float tz = (float)Math.round(z);
					BuildingTile tile = t.getTileAt(x, y, z);
					
					
					
					if (tile != null) {
						byte walls = tile.getWalls();

						if ((walls & 4) != 0 && py-1f < ty) {
							//collider.set(tx+.25f, ty+.75f, tz+.25f);
							//testBlock(collider);
							velocity.y = 0;
						}
						
						if ((walls & 8) != 0 && py <= ty) {
							//collider.set(tx+.25f, ty-.25f, tz+.25f);
							//testBlock(collider);
							velocity.y = 0;
							grounded = true;
							position.y = ty;
						}
						
						if ((walls & 1) != 0 && px > tx) {
							collider.set(tx-.5f, ty+.5f, tz+.5f);
							testBlock(collider);
						}
						
						if ((walls & 2) != 0 && px < tx + 1f) {
							collider.set(tx+1.5f, ty+.5f, tz+.5f);
							testBlock(collider);
						}
						
						if ((walls & 16) != 0 && pz > tz) {
							collider.set(tx+.5f, ty+.5f, tz-.5f);
							testBlock(collider);
						}
						
						if ((walls & 32) != 0 && pz < tz + 1f) {
							collider.set(tx+.5f, ty+.5f, tz+1.5f);
							testBlock(collider);
						}
					}
				}
			}
		}
		
		//position.set(aabb.getCenter().x, aabb.getCenter().y - (height/2f), aabb.getCenter().z);
	}
	
	private void testBlock(AABB other) {
		Vector3f escape = aabb.collide(other);
		if (escape == null)
			return;
		float adx = Math.abs(escape.x);
		float ady = Math.abs(escape.y);
		float adz = Math.abs(escape.z);
		if (adx < ady && adx < adz) { 
			position.x += escape.x;
			velocity.x = 0;
		} else {
			position.z += escape.z;
			velocity.z = 0;
		}
		
		aabb.set(position.x, position.y + (height/2), position.z);
	}

	private void testWall(Chunk chunk, int rx1, int rz1, int rx2, int rz2, int n) {
		float y1 = chunk.heightmap[rx1][rz1];
		float y2 = chunk.heightmap[rx2][rz2];
	
		float x1 = ((rx1/2) * Chunk.POLYGON_SIZE) + chunk.realX;
		//int x2 = ((rx2/2) * Chunk.POLYGON_SIZE) + (chunk.x * Chunk.CHUNK_SIZE);
		float z1 = ((rz1/2) * Chunk.POLYGON_SIZE) + chunk.realZ;
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
		aabb.set(position.x, position.y + (height/2), position.z);
		
		if (!submerged && !climbing) {
			velocity.y = Math.max(velocity.y - gravity * Window.deltaTime, maxGravity);
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
	
	@Override
	public void tick(Scene scene) {
		super.tick(scene);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	public Source getSource() {
		return source;
	}
}
