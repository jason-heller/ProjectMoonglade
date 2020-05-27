package map.building;

import static map.Chunk.POLYGON_SIZE;
import static map.building.Tile.BOTTOM;

import org.joml.Vector3f;

import dev.Console;
import gl.Camera;
import map.Chunk;

public class Tile {
	private Chunk chunk;
	private byte walls;
	public int[] id;
	private int subid;
	
	public static final float TILE_SIZE = POLYGON_SIZE/2f;
	
	static final byte 	LEFT = 1,
						RIGHT = 2,
						TOP = 4,
						BOTTOM = 8,
						FRONT = 16,
						BACK = 32;
	
	public Tile(Chunk chunk, int id, byte walls) {
		this.chunk = chunk;
		this.id = new int[8];
		this.walls = walls;
		
		int val = 1;
		for(int i = 0; i < 8; i++) {
			this.id[i] = (walls & val) != 0 ? id : 0;
		
			val *= 2;
		}
	}
	
	public byte getWalls() {
		return walls;
	}

	public boolean isActive() {
		return (walls != 0);
	}

	public boolean isActive(byte wall) {
		return (walls & wall) != 0;
	}

	/**
	 * Gets the byte value associated with the current looking direciton. Note this
	 * is the opposite of which way the palyer faces since all walls are inward
	 * facing per tile
	 * 
	 * @param camera the current camera
	 * @param floors whether or not we are checking for floors/ceilings or for walls
	 *               (true = floors/ceilings)
	 * @return the byte value for the wall/floor facing the player
	 */
	public static byte getFacingByte(Camera camera, boolean floors) {
		if (floors) {
			return camera.getPitch() > 0 ? BOTTOM : TOP;
		}
		
		float yaw = camera.getYaw();Console.log(yaw, Math.round((yaw-45) / 90));
		
		switch((int)(Math.floor((yaw-45) / 90))) {
		case 0:
			return RIGHT;
		case 1:
			return BACK;
		case 2:
			return LEFT;
		}
		
		return FRONT;
	}

	public void append(byte wall, int id) {
		walls |= wall;
		int val = 1;
		for(int i = 0; i < 8; i++) {
			this.id[i] = (wall & val) != 0 ? id : 0;
		
			val *= 2;
		}
	}

	public void remove(byte wall, int id) {
		int val = 1;
		byte newWall = 0;
		
		for(int i = 0; i < 8; i++) {
			if ((wall & val) != 0) {
				this.id[i] = 0;
			}
			else if ((walls & val) != 0) {
				newWall += val;
			}
			
			walls = newWall;
			val *= 2;
		}
	}
	
	public static byte getFacingByte(Vector3f v) {
		int weight = Math.round(v.x) + (10 + Math.round(v.y) * 2) + (100 + Math.round(v.z) * 4);
		
		switch(weight) {
		case 109:
			return LEFT;
		case 111:
			return RIGHT;
		case 121:
			return TOP;
		case 108:
			return BOTTOM;
		case 106:
			return FRONT;
		case 114:
			return BACK;
		}
		
		return 0;
	}

	public static byte checkRay(Vector3f origin, Vector3f dir, float tx, float ty, float tz, byte sides) {
		int dx = Math.round(dir.x);
		int dy = Math.round(dir.y);
		int dz = Math.round(dir.z);
		
		if ((sides & LEFT) != 0 && dx == -1 && dy == 0 && dz == 0) {
			return LEFT;
		}
		
		if ((sides & RIGHT) != 0 && dx == 1 && dy == 0 && dz == 0) {
			return RIGHT;
		}
		
		if ((sides & TOP) != 0 && dx == 0 && dy == 1 && dz == 0) {
			return TOP;
		}
		
		if ((sides & BOTTOM) != 0 && dx == 0 && dy == -1 && dz == 0) {
			return BOTTOM;
		}
		
		if ((sides & FRONT) != 0 && dx == 0 && dy == 0 && dz == -1) {
			return FRONT;
		}
		
		if ((sides & BACK) != 0 && dx == 0 && dy == 0 && dz == 1) {
			return BACK;
		}
		
		return 0;
		
		/*Vector3f planeOrigin = new Vector3f(tx, ty, tz);
		Vector3f planeNormal = new Vector3f();
		Vector3f hitPoint = null;
		final float halfTileSize = (TILE_SIZE/2);
		Vector3f tileCenter = new Vector3f(tx+halfTileSize, ty+halfTileSize, tz+halfTileSize);
		final float rangeSqr = (halfTileSize * halfTileSize) + 1;
		float shortestSqr = Float.MAX_VALUE;
		
		byte output = 0;
		
		if ((sides & LEFT) != 0) {
			planeNormal.set(-1, 0, 0);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			Console.log("LTEST", hitPoint, tileCenter, "    ", distSqr, rangeSqr);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}
		
		if ((sides & TOP) != 0) {
			planeNormal.set(0, 0, -1);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}
		
		if ((sides & FRONT) != 0) {
			planeNormal.set(0, -1, 0);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}

		planeOrigin.set(tx + TILE_SIZE, ty + TILE_SIZE, tz + TILE_SIZE);
		
		if ((sides & RIGHT) != 0) {
			planeNormal.set(1, 0, 0);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}
		
		if ((sides & BOTTOM) != 0) {
			planeNormal.set(0, 0, 1);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}
		
		if ((sides & BACK) != 0) {
			planeNormal.set(0, 1, 0);
			hitPoint = rayIntersection(origin, dir, planeOrigin, planeNormal);
			float distSqr = Vector3f.distanceSquared(hitPoint, tileCenter);
			if (distSqr < rangeSqr) {
				distSqr = Vector3f.distanceSquared(hitPoint, origin);
				if (distSqr < shortestSqr) {
					shortestSqr = distSqr;
					output = LEFT;
				}
			}
		}
		Console.log("OUT:",output);
		return output;*/
	}
	
	/*private static Vector3f rayIntersection(Vector3f rayOrigin, Vector3f rayDirection, Vector3f planeOrigin, Vector3f normal) {
		float dist = -(normal.x * planeOrigin.x + normal.y * planeOrigin.y + normal.z * planeOrigin.z);
		
		if (normal.dot(rayDirection) == 0.0f) {
			return null;
		}

		final float t = (dist - normal.dot(rayOrigin)) / normal.dot(rayDirection);
		return Vector3f.add(rayOrigin, Vector3f.mul(rayDirection, t));
	}*/
}
