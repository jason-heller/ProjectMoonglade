package scene.entity.utility;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.Source;
import core.Application;
import core.Resources;
import geom.AABB;
import geom.Manifold;
import gl.Window;
import map.Chunk;
import map.Terrain;
import map.tile.Tile;
import scene.Scene;
import scene.entity.Entity;
import scene.overworld.Overworld;

public abstract class PhysicsEntity extends Entity {
	
	private static final float DEFAULT_FRICTION = 3f;

	private static final float ALLOWABLE_STEP = .35f;
	
	private boolean grounded = false;
	private boolean previouslyGrounded = false;
	private boolean sliding = false;
	private boolean submerged = false, fullySubmerged = false;
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
		
		if (chunk == null) return;
		
		final float relx = position.x - chunk.realX;
		final float relz = position.z - chunk.realZ;
		int tx = ((int) Math.floor(relx / Chunk.POLYGON_SIZE));
		int tz = ((int) Math.floor(relz / Chunk.POLYGON_SIZE));
		
		submerged = false;
		fullySubmerged = false;
		final float waterHeight = chunk.getWaterHeight(tx, tz);
		if (waterHeight != Float.MIN_VALUE && waterHeight > position.y) {
			submerged = true;
			
			if (waterHeight > position.y + height) {
				fullySubmerged = true;
			}
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
		aabb.setCenter(position.x, position.y + (height/2), position.z);

		Tile tile;
		
		for(float i = position.x - 1f; i <= position.x + 1f; i += 1f) {
			for(float j = position.z - 1f; j <= position.z + 1f; j += 1f) {
				for(float k = position.y + height; k >= position.y-1; k -= 1f) {
					Chunk chunkPtr = t.getChunkAt(i, j);
					if (chunkPtr == null) continue;
					
					float tx = (float)Math.floor(i);
					float tz = (float)Math.floor(j);
					float ty = (float)Math.floor(k);
					
					tile = chunkPtr.getBuilding().getTileAt(tx - chunkPtr.realX, ty, tz - chunkPtr.realZ);
					
					if (tile != null) {
						testTile(tile, position.x, position.y, position.z,
								tx, ty, tz, (k != position.y+height && i == position.x && j == position.z));
					}
				}
			}
		}
		
		/*Chunk chunkPtr = t.getChunkAt(position.x, position.z);
		
		float tx = (float)Math.floor(position.x);
		float tz = (float)Math.floor(position.z);
		float ty = (float)Math.floor(position.y-1f);
		
		tile = chunkPtr.getBuilding().getTileAt(tx - chunkPtr.realX, ty, tz - chunkPtr.realZ);
		
		if (tile != null) {
			testTile(tile, position.x, position.y, position.z,
					tx, ty, tz, true, true);
		}*/
	}
	
	protected void addTile(Tile[] tiles, Tile tileAt) {
		if (tileAt == null) return;
		for(int i = 0; i < 4; i++) {
			if (tiles[i] == tileAt) {
				return;
			}
			
			if (tiles[i] == null) {
				tiles[i] = tileAt;
				return;
			}
		}
	}

	private void testTile(Tile tile, float px ,float py , float pz, float tx, float ty, float tz, boolean stick) {
		byte walls = tile.getWalls();
		byte slope = tile.getSlope();
		int slopeFactor = walls >> 6;
		AABB tileBounds = new AABB(0,0,0,0,0,0);

		if (walls != 0) {
			if (tile.isSolid(2) && (walls & 4) != 0/* && py > ty+2-height && !isFloor*/) {
				tileBounds.setMinMax(tx, ty+.95f, tz, tx+1, ty+1, tz+1);
				aabbCollide(tileBounds);
			}
			
			if ((tile.isSolid(3)/* || tile.isSolid(2)*/) && (walls & 8) != 0/*  && (py <= ty+.05f || this.previouslyGrounded) && isFloor*/) {
				tileBounds.setMinMax(tx, ty, tz, tx+1, ty+.05f, tz+1);
				aabbCollide(tileBounds);
			}
			
			if (tile.isSolid(0) && (walls & 1) != 0/* && px < tx+width*/) {
				tileBounds.setMinMax(tx-.05f, ty, tz, tx+.05f, ty+1, tz+1);
				aabbCollide(tileBounds);
			}
			
			if (tile.isSolid(1) && (walls & 2) != 0/* && px > (tx + 1f) - width*/) {
				tileBounds.setMinMax(tx+.95f, ty, tz, tx+1.05f, ty+1, tz+1);
				aabbCollide(tileBounds);
			}
			
			if (tile.isSolid(4) && (walls & 16) != 0/* && pz < tz+width*/) {
				tileBounds.setMinMax(tx, ty, tz-.05f, tx+1, ty+1, tz+.05f);
				aabbCollide(tileBounds);
			}
			
			if (tile.isSolid(5) && (walls & 32) != 0/* && pz > (tz + 1f) - width*/) {
				tileBounds.setMinMax(tx, ty, tz+.95f, tx+1, ty+1, tz+1.05f);
				aabbCollide(tileBounds);
			}
		}
		
		if (slopeFactor == 0 && slope != 0 && tile.isSolid(6)) {
			float dx = ((position.x % 1f) + 2) % 1f;
			float dz = ((position.z % 1f) + 2) % 1f;
			
			if ((slope & 1) != 0 && (py <= ty + (1 - dx) || previouslyGrounded)) {
				velocity.y = 0;
				grounded = true;
				position.y = ty + (1f - dx);
				if (stick) position.y++;
			}
			
			if ((slope & 2) != 0 && (py <= ty + dx || previouslyGrounded)) {
				velocity.y = 0;
				grounded = true;
				position.y = ty + dx;
				if (stick) position.y++;
			}
			
			if ((slope & 16) != 0 && (py <= ty + (1 - dz) || previouslyGrounded)) {
				velocity.y = 0;
				grounded = true;
				position.y = ty + (1f - dz);
				if (stick) position.y++;
			}
			
			if ((slope & 32) != 0 && (py <= ty + dz || previouslyGrounded)) {
				velocity.y = 0;
				grounded = true;
				position.y = ty + dz;
				if (stick) position.y++;
			}
		}
	}
	
	private void aabbCollide(AABB other) {
		Manifold manifold = aabb.collide(other);
		
		if (manifold != null) {
			Vector3f axis = manifold.getAxis();
			position.add(Vector3f.mul(axis, manifold.getDepth()));
			aabb.setCenter(position.x, position.y + (height/2), position.z);
			
			if (axis.y == 1f) {
				grounded = true;
			}
			
			if (axis.x != 0) velocity.x = 0;
			if (axis.y != 0) velocity.y = 0;
			if (axis.z != 0) velocity.z = 0;
		}
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
	
	public boolean isFullySubmerged() {
		return fullySubmerged;
	}
	
	public boolean isClimbing() {
		return climbing;
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
		aabb.setCenter(position.x, position.y + (height/2), position.z);
		
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
	
	public AABB getAABB() {
		return aabb;
	}
}
