package scene.entity.utility;

import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Application;
import geom.AABB;
import geom.Manifold;
import gl.Window;
import map.Chunk;
import map.Material;
import map.Terrain;
import map.prop.Props;
import map.prop.StaticProp;
import map.prop.StaticPropProperties;
import map.tile.Tile;
import scene.Scene;
import scene.entity.Entity;
import scene.overworld.Overworld;

public abstract class PhysicsEntity extends Entity {
	
	private static final float DEFAULT_FRICTION = 3f;

	private static final float ALLOWABLE_STEP = .1f;
	
	protected boolean grounded = false;
	public boolean previouslyGrounded = false;
	private boolean sliding = false;
	protected boolean submerged = false;

	private boolean fullySubmerged = false;
	private boolean climbing = false;
	
	private static float gravity = 17f;
	private static float maxGravity = -40f;
	
	public float maxSpeed = 25f, maxAirSpeed = 5f, maxWaterSpeed = 1f;
	public float friction = DEFAULT_FRICTION;
	public float airFriction = 0f;

	public boolean visible = true;
	
	private Material stoodOnMaterial = Material.NONE;

	public PhysicsEntity(String model, String diffuse) {
		super(model, diffuse);
		aabb = new AABB(new Vector3f(), new Vector3f());
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
		stoodOnMaterial = Material.NONE;
		Chunk chunk = terrain.getChunkAt(position.x, position.z);
		
		if (chunk == null) return;
		
		final float relx = position.x - chunk.realX;
		final float relz = position.z - chunk.realZ;
		int tx = ((int) Math.floor(relx / Chunk.POLYGON_SIZE));
		int tz = ((int) Math.floor(relz / Chunk.POLYGON_SIZE));
				
		//if (tx < 0 || tz < 0 || tx >= Chunk.VERTEX_COUNT || tz >= Chunk.VERTEX_COUNT) return;
		
		submerged = false;
		fullySubmerged = false;
		final float waterHeight = chunk.getWaterHeight(tx, tz);
		if (waterHeight != Float.MIN_VALUE && waterHeight > position.y + .2f) {
			submerged = true;
			
			if (waterHeight > position.y + aabb.getBounds().y*2) {
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
		
		tx = ((tx-1)/2);
		tz = ((tz-1)/2);
		
		final float w = aabb.getBounds().x-.01f;
		
		Chunk tr = terrain.getChunkAt(position.x+w, position.z-w),
				bl = terrain.getChunkAt(position.x-w, position.z-w),
				tl = terrain.getChunkAt(position.x-w, position.z+w),
				br = terrain.getChunkAt(position.x+w, position.z+w);
		
		if (tr == null || tl == null || br == null || bl == null) {
			return;
		}
		
		final float minHeight = Math.max(
				tr.getPolygon(position.x+w, position.z-w).barryCentric(position.x+w, position.z-w), Math.max(
				bl.getPolygon(position.x-w, position.z-w).barryCentric(position.x-w, position.z-w), Math.max(
				tl.getPolygon(position.x-w, position.z+w).barryCentric(position.x-w, position.z+w), 
				br.getPolygon(position.x+w, position.z+w).barryCentric(position.x+w, position.z+w))));
		
		
		if (position.y <= minHeight || position.y <= minHeight + ALLOWABLE_STEP && previouslyGrounded) {
			position.y = minHeight;
			grounded = true;
			velocity.y = 0;
		} else {
			grounded = false;
		}
		
		collideWithProps(terrain);
		
		collideWithBuildings(terrain);
		
	}
	
	private void collideWithProps(Terrain terrain) {
		final float w = aabb.getBounds().x-.01f;
		testProp(terrain, position.x+w, position.z-w);
		testProp(terrain, position.x-w, position.z-w);
		testProp(terrain, position.x-w, position.z+w);
		testProp(terrain, position.x+w, position.z+w);
		//testProp(terrain, position.x, position.z);
	}
	
	private void testProp(Terrain terrain, float px, float pz) {
		Chunk chunk = terrain.getChunkAt(px, pz);
		
		if (chunk == null) return;
		
		int tileX = (int)(px - chunk.realX);
		int tileZ = (int)(pz - chunk.realZ);
		
		Props prop = chunk.getProps().getProp(tileX, tileZ);
		if (prop != null) {
			StaticProp staticProp = Props.get(prop);
			
			if (staticProp.isSolid()) {
			
				final StaticPropProperties tileProps = chunk.getProps().getEntityProperties(tileX, tileZ);
				
				final float x = chunk.realX + (tileX + .5f);
				final float z = chunk.realZ + (tileZ + .5f);
				
				final StaticProp tile = Props.get(prop);
				Vector3f bounds = tile.getBounds();
	
				float tileY = staticProp.isGrounded() ? chunk.heightLookup(tileX, tileZ) : chunk.getWaterHeight(tileX, tileZ);
				
				final float y = tileY + (tileProps.scale * bounds.y);
	
				final AABB aabb = new AABB(x, y, z, bounds.x * tileProps.scale, bounds.y * tileProps.scale, bounds.z * tileProps.scale);
				this.aabbCollide(tile.getMaterial(), aabb);
			}
		}
	}

	private void collideWithBuildings(Terrain t) {
		aabb.setCenter(position.x, position.y + aabb.getBounds().y, position.z);

		Tile tile;
		
		for(float i = position.x - 1f; i <= position.x + 1f; i += 1f) {
			for(float j = position.z - 1f; j <= position.z + 1f; j += 1f) {
				for(float k = position.y + 2; k > position.y-1; k -= 1f) {
					Chunk chunkPtr = t.getChunkAt(i, j);
					if (chunkPtr == null) continue;
					
					float tx = (float)Math.floor(i);
					float tz = (float)Math.floor(j);
					float ty = (float)Math.floor(k);
					
					tile = chunkPtr.getBuilding().getTileAt(tx - chunkPtr.realX, ty, tz - chunkPtr.realZ);
					
					if (tile != null) {
						testTile(tile, position.x, position.y, position.z,
								tx, ty, tz, (k == position.y));
					}
				}
			}
		}
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
		AABB tileBounds = new AABB(0,0,0,0,0,0);

		if (walls != 0) {
			if (tile.isSolid(2) && (walls & 4) != 0) {
				tileBounds.setMinMax(tx, ty-.05f, tz, tx+1, ty+.05f, tz+1);
				aabbCollide(tile.getMaterial(2), tileBounds);
			}
			
			if (tile.isSolid(0) && (walls & 1) != 0) {
				tileBounds.setMinMax(tx, ty, tz-.05f, tx+1, ty+1, tz+.05f);
				aabbCollide(tile.getMaterial(0), tileBounds);
			}
			
			if (tile.isSolid(1) && (walls & 2) != 0) {
				tileBounds.setMinMax(tx+.95f, ty, tz, tx+1.05f, ty+1, tz+1);
				aabbCollide(tile.getMaterial(1), tileBounds);
			}
		}
		
		if ((walls & 120) != 0) {
			float dx = position.x - tx;//((position.x % 1f) + 2) % 1f;
			float dz = position.z - tz;//((position.z % 1f) + 2) % 1f;
			
			if (dx < 0 || dz < 0 || dx > 1|| dz > 1) {
				float f = aabb.getBounds().x;
				tileBounds.setMinMax(tx+f, ty+f, tz+f, tx+(1-f), ty+(1-f), tz+(1-f));
				aabbCollide(tile.getMaterial(3), tileBounds);
				return;
			}
			
			float yNew = 0;

			if (tile.isSolid(3) && (walls & 64) != 0 && (position.y <= ty + (1 - dx) || previouslyGrounded)) {
				yNew = ty + (1f - dx);
			}
			
			if (tile.isSolid(4) && (walls & 32) != 0 && (position.y <= ty + dx || previouslyGrounded)) {
				yNew = ty + dx;
			}
			
			if (tile.isSolid(5) && (walls & 8) != 0 && (position.y <= ty + (1 - dz) || previouslyGrounded)) {
				yNew = ty + (1f - dz);
			}
			
			if (tile.isSolid(6) && (walls & 16) != 0 && (position.y <= ty + dz || previouslyGrounded)) {
				yNew = ty + dz;
			}
			
			if (Math.abs(position.y - yNew) < .5) {
				velocity.y = 0;
				grounded = true;
				position.y = yNew;
			} else {
				float f = aabb.getBounds().x;
				tileBounds.setMinMax(tx+f, ty+f, tz+f, tx+(1-f), ty+(1-f), tz+(1-f));
				aabbCollide(tile.getMaterial(6), tileBounds);
			}
		}
	}
	
	private void aabbCollide(Material material, AABB other) {
		Manifold manifold = aabb.collide(other);
		
		if (manifold != null) {
			Vector3f axis = manifold.getAxis();
			float aabbTop = (other.getY() + other.getBounds().y);
			float dy = aabbTop - position.y;
			if (grounded && dy < ALLOWABLE_STEP && dy > 0) {
				position.y = aabbTop;
				aabb.setCenter(position.x, position.y + aabb.getBounds().y, position.z);
				return;
			}
			
			position.add(Vector3f.mul(axis, manifold.getDepth()));
			aabb.setCenter(position.x, position.y + aabb.getBounds().y, position.z);
			
			if (axis.y == 1f) {
				grounded = true;
				stoodOnMaterial = material;
			}
			
			if (axis.x != 0) velocity.x = 0;
			if (axis.y != 0) velocity.y = 0;
			if (axis.z != 0) velocity.z = 0;
		}
	}
	
	private void testWall(Chunk chunk, int rx1, int rz1, int rx2, int rz2, int n) {
		if (rx1 < 0 || rz1 < 0 || rx2 >= Chunk.VERTEX_COUNT || rz2 >= Chunk.VERTEX_COUNT) return;
		
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
			if (position.x - aabb.getBounds().x < x1) {
				position.x = x1 + aabb.getBounds().x;
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
			if (position.x + aabb.getBounds().x > x1) {
				position.x = x1 - aabb.getBounds().x;
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
			if (position.z - aabb.getBounds().x < z1) {
				position.z = z1 + aabb.getBounds().x;
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
			if (position.z + aabb.getBounds().x > z1) {
				position.z = z1 - aabb.getBounds().x;
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

			//previouslyGrounded = false;
		} else {
			velocity.y = height;
			grounded = false;
			sliding = false;
			//previouslyGrounded = false;
		}
	}

	@Override
	public void update(Scene scene) {
		aabb.setCenter(position.x, position.y + aabb.getBounds().y, position.z);
		
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
	public void hurt(int  damage, Entity attacker, float invulnerabiltiyTime) {
		if (invulnerabilityTimer == 0 && isHurtable()) {
			hp -=  damage;
			invulnerabilityTimer = invulnerabiltiyTime;
			if (attacker != null) {
				this.jump(6f);
				this.accelerate(Vector3f.sub(attacker.position, position).negate(), 150f);
			}
		}
		
		if (hp <= 0) {
			die();
		}
		
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	public AABB getAABB() {
		return aabb;
	}
	
	public Material getStoodOnMaterial() {
		return stoodOnMaterial;
	}
}
