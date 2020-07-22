package map.tile;

import org.joml.Vector3f;

import gl.Camera;
import map.Material;
import util.MathUtil;

public class Tile {
	public Material[] materials;
	private byte[] flags;
	
	public static final int NUM_MATS = 7;
	
	public static final float TILE_SIZE = 1f;//.5f;
	
	static final byte 	LEFT = 1,
						FRONT = 2,
						BOTTOM = 4,
						SLOPE_LEFT = 8,
						SLOPE_RIGHT = 16,
						SLOPE_TOP = 32,
						SLOPE_BOTTOM = 64,
						SLOPE_WALL_FLAG = (byte) (128 & 0xff);
	
	public Tile(Material material, byte walls, byte flags) {
		this.materials = new Material[NUM_MATS];
		this.flags = new byte[NUM_MATS];
		int val = 1;
		for(int i = 0; i < NUM_MATS; i++) {
			this.materials[i] = (walls & val) != 0 ? material : Material.NONE;
			if ((walls & val) != 0) {
				this.materials[i] = material;
				this.flags[i] = flags;
			} else {
				this.materials[i] = Material.NONE;
				this.flags[i] = (byte)0;
			}
		
			val *= 2;
		}
	}
	
	public Tile(Material[] id, byte[] flags) {
		this.materials = id;
		this.flags = flags;
	}
	
	public byte getWalls() {
		byte walls = 0;
		int val = 1;
		for(int i = 0; i < NUM_MATS; i++) {
			if (materials[i] != Material.NONE)
				walls |= val;
		
			val *= 2;
		}
		return walls;
	}

	public boolean isActive() {
		for(int i = 0; i < NUM_MATS; i++) {
			if (materials[i] != Material.NONE)
				return true;
		}
		
		return false;
	}

	public boolean isActive(int wall) {
		return materials[wall] != Material.NONE;
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
	public static byte getFacingByte(Camera camera, byte wallSetting) {
		if (wallSetting == 1) {
			return BOTTOM;
		}
		
		if (wallSetting == 0) {
			float yaw = camera.getYaw();
			
			switch((int)(Math.floor((yaw-45) / 90))) {
			case 0:
			case 2:
				return FRONT;
			}
			
			return LEFT;
		}
		
		float yaw = camera.getYaw();
		
		switch((int)(Math.floor((yaw-45) / 90))) {
		case 0:
			return SLOPE_TOP;
		case 1:
			return SLOPE_RIGHT;
		case 2:
			return SLOPE_BOTTOM;
		}
		
		return SLOPE_LEFT;
	}

	void append(byte wall, Material mat, byte flags) {
		int val = 1;
		for(int i = 0; i < NUM_MATS; i++) {
			this.materials[i] = (wall & val) != 0 ? mat : this.materials[i];
			this.flags[i] = (wall & val) != 0 ? flags : this.flags[i];
			val *= 2;
		}
	}
	
	void append(Material[] mat, byte[] flags) {
		this.materials = mat;
		this.flags = flags;
	}
	
	public static byte getFacingByte(Vector3f v) {
		int weight = (int) (Math.abs(v.x) + (Math.abs(v.y) * 2) + (Math.abs(v.z) * 4));
		
		switch(weight) {
		case 1:
			return FRONT;
		case 2:
			return BOTTOM;
		case 4:
			return LEFT;
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
		
		if ((sides & BOTTOM) != 0 && looking == BOTTOM) {
			return BOTTOM;
		}
		
		if ((sides & FRONT) != 0 && looking == FRONT) {
			return FRONT;
		}
		
		return 0;
	}

	public Material getMaterial(int facing) {
		return materials[facing];
	}

	public byte[] getFlags() {
		return flags;
	}

	public void setFlags(int i, byte flags) {
		this.flags[i] = flags;
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

	public boolean isSolid(int facingIndex) {
		return this.materials[facingIndex].isSolid();
	}

	public static Vector3f getNormal(byte side) {
		
		if ((side & LEFT) != 0) {
			return new Vector3f(1,0,0);
		}
		
		if ((side & BOTTOM) != 0) {
			return  new Vector3f(0,1,0);
		}
		
		if ((side & FRONT) != 0) {
			return  new Vector3f(0,0,1);
		}
		
		return new Vector3f();
	}

	public static int getFacingIndex(byte wall) {
		switch(wall) {
		case LEFT:
			return 0;
		case FRONT:
			return 1;
		case BOTTOM:
			return 2;
		case SLOPE_LEFT:
			return 3;
		case SLOPE_RIGHT:
			return 4;
		case SLOPE_TOP:
			return 5;
		case SLOPE_BOTTOM:
			return 6;
		}
		
		return 0;
	}
}
