package map.tile;

import org.joml.Vector3f;

import dev.Console;
import gl.Camera;
import map.Chunk;
import map.Material;
import scene.overworld.inventory.Item;
import util.MathUtil;

public class BuildingTile {
	private Chunk chunk;
	private byte walls, slope;
	private byte flags;		// For tiling...tiles, this represents the graphic offset, can have other uses
	public Material[] materials;
	
	public static final int NUM_MATS = 7;
	
	public static final float TILE_SIZE = 1f;//.5f;
	
	static final byte 	LEFT = 1,
						RIGHT = 2,
						TOP = 4,
						BOTTOM = 8,
						FRONT = 16,
						BACK = 32;
	
	public BuildingTile(Chunk chunk, Material materials, byte walls, byte slope, byte flags) {
		this.chunk = chunk;
		this.materials = new Material[NUM_MATS];
		this.walls = walls;
		this.slope = slope;
		this.flags = flags;
		
		int val = 1;
		for(int i = 0; i < NUM_MATS-1; i++) {
			this.materials[i] = (walls & val) != 0 ? materials : Material.NONE;
		
			val *= 2;
		}
		
		if (slope != 0) {
			this.materials[NUM_MATS-1] = materials;
		} else {
			this.materials[NUM_MATS-1] = Material.NONE;
		}
	}
	
	public BuildingTile(Chunk chunk, Material[] id, byte walls, byte slope, byte flags) {
		this.chunk = chunk;
		this.materials = id;
		this.walls = walls;	
		this.slope = slope;
		this.flags = flags;
	}
	
	public byte getWalls() {
		return walls;
	}

	public boolean isActive() {
		return (walls != 0);
	}

	public boolean isActive(byte wall) {
		return (walls & wall) != wall;
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
		
		float yaw = camera.getYaw();
		
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

	void append(byte wall, byte slope, Material mat, byte flags) {
		if (mat == Material.NONE) {
			walls = (byte) (walls - (walls & wall));
			this.slope = (byte) (this.slope - (this.slope & slope));
		} else {
			walls |= wall;
			this.slope |= slope;
			
			if (slope != 0) {
				this.materials[NUM_MATS-1] = mat;
			}
		}
		
		this.flags = flags;
		int val = 1;
		for(int i = 0; i < NUM_MATS-1; i++) {
			this.materials[i] = (wall & val) != 0 ? mat : this.materials[i];
			
			val *= 2;
		}
	}
	
	void append(byte wall, byte slope, Material[] mat, byte flags) {
		this.walls = wall;
		this.materials = mat;
		this.flags = flags;
		this.slope = slope;
	}
	
	public static byte getFacingByte(Vector3f v) {
		int weight = Math.round(v.x) + (10 + (Math.round(v.y) * 2)) + (100 + (Math.round(v.z) * 4));
		
		switch(weight) {
		case 109:
			return LEFT;
		case 111:
			return RIGHT;
		case 112:
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
		Vector3f normal = MathUtil.rayBoxEscapeNormal(origin, dir, tx, ty, tz, .5f);
		return checkRay(normal, sides);
	}
	
	public static byte checkRay(Vector3f normal, byte sides) {
		byte looking = getFacingByte(normal);
		
		if ((sides & LEFT) != 0 && looking == LEFT) {
			return LEFT;
		}
		
		if ((sides & RIGHT) != 0 && looking == RIGHT) {
			return RIGHT;
		}
		
		if ((sides & TOP) != 0 && looking == TOP) {
			return TOP;
		}
		
		if ((sides & BOTTOM) != 0 && looking == BOTTOM) {
			return BOTTOM;
		}
		
		if ((sides & FRONT) != 0 && looking == FRONT) {
			return FRONT;
		}
		
		if ((sides & BACK) != 0 && looking == BACK) {
			return BACK;
		}
		
		return 0;
	}

	public Material getMaterial(int facing) {
		return materials[facing];
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public static byte getOpposingBytes(byte facingBytes) {
		byte output = 0;
		
		boolean bitIsSet;
		for(int i = 1; i < 128; i *= 4) {
			bitIsSet = ((facingBytes & i) != 0);
			if (bitIsSet)
				output |= i*2;
		}
		
		for(int i = 2; i < 128; i *= 4) {
			bitIsSet = ((facingBytes & i) != 0);
			if (bitIsSet)
				output |= i/2;
		}
		
		return output;
	}

	public Item getDrop() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSolid(int facingIndex) {
		return this.materials[facingIndex].isSolid();
	}

	public byte getSlope() {
		return slope;
	}

	public static Vector3f getNormal(byte side) {
		
		if ((side & LEFT) != 0) {
			return new Vector3f(1,0,0);
		}
		
		if ((side & RIGHT) != 0) {
			return  new Vector3f(-1,0,0);
		}
		
		if ((side & TOP) != 0) {
			return  new Vector3f(0,-1,0);
		}
		
		if ((side & BOTTOM) != 0) {
			return  new Vector3f(0,1,0);
		}
		
		if ((side & FRONT) != 0) {
			return  new Vector3f(0,0,1);
		}
		
		if ((side & BACK) != 0) {
			return  new Vector3f(0,0,-1);
		}
		
		return new Vector3f();
	}
}
