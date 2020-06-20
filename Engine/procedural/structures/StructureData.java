package procedural.structures;

import map.Material;
import map.building.BuildingTile;

public class StructureData {
	
	private byte flags;		// uuuuuEBH (E = envtile, B = buildings, H = heights)
	private int width, height, length;
	
	private float[][] terrain;
	private int[][] envTiles;
	private CompTileData[][][] buildingTiles; // ################sssssssswwwwwwww (w = wallflags, s = specialflags
	
	
	public StructureData(int width, int height, int length, byte flags) {
		this.width = width;
		this.height = height;
		this.length = length;
		this.flags = flags;
		
		if ((flags & 0x01) != 0)
			terrain = new float[width][length];
		
		if ((flags & 0x02) != 0)
			buildingTiles = new CompTileData[width][height][length];
			
		if ((flags & 0x04) != 0)
			envTiles = new int[width][length];

	}

	public int getLength() {
		return length;
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public boolean hasTerrain() {
		return ((flags & 0x01) != 0);
	}
	
	public boolean hasBuildingTiles() {
		return ((flags & 0x02) != 0);
	}
	
	public boolean hasEnvTiles() {
		return ((flags & 0x04) != 0);
	}

	public float getTerrain(int i, int j) {
		return terrain[i][j];
	}
	
	public CompTileData getBuildingTile(int i, int j, int k) {
		return buildingTiles[i][j][k];
	}
	
	public int getEnvTile(int i, int j) {
		return envTiles[i][j];
	}

	public void setHeight(int i, int j, float height) {
		this.terrain[i][j] = height;
	}

	public void setEnvTile(int i, int j, int id) {
		this.envTiles[i][j] = id;
	}

	public void setBuildingTile(int i, int j, int k, int[] mats, int tileWalls, int tileFlags) {
		this.buildingTiles[i][j][k] = new CompTileData(mats, tileWalls, tileFlags);
	}
}

class CompTileData {
	/*long l1, l2;
	
	// uuswMMMM MMMMMMMM
	
	public CompTileData(int[] materials, int walls, int flags) {
		l1 |= (0xffff & materials[0]);
		l1 |= (0xffff & materials[1]) << 16;
		l1 |= (0xffff & materials[2]) << 32;
		l1 |= (0xffff & materials[3]) << 48;
		
		l2 |= (0xffff & materials[4]);
		l2 |= (0xffff & materials[5]) << 16;
		l2 |= ((0xff & walls) << 32);
		l2 |= ((0xff & flags) << 40);	
	}
	
	public Material[] getMaterials() {
		final Material[] mats = Material.values();
		final Material[] materials = new Material[6];
		materials[0] = mats[(int) (l1 & 0xffff)];
		materials[1] = mats[(int) (l1 >> 16 & 0xffff)];
		materials[2] = mats[(int) (l1 >> 32 & 0xffff)];
		materials[3] = mats[(int) (l1 >> 48 & 0xffff)];
		materials[4] = mats[(int) (l2 & 0xffff)];
		materials[5] = mats[(int) (l2 >> 16 & 0xffff)];
		
		return materials;
	}
	
	public byte getWalls() {
		return  (byte) ((l2 >> 32) & 0xff);
	}
	
	public byte getFlags() {
		return (byte) ((l2 >> 40) & 0xff);
	}*/
	
	private byte walls, flags;
	private int[] materials;
	
	public CompTileData(int[] materials, int walls, int flags) {
		this.materials = materials;
		this.walls = (byte) walls;
		this.flags = (byte) flags;
	}
	
	public Material[] getMaterials() {
		final Material[] mats = Material.values();
		final Material[] materials = new Material[6];
		materials[0] = mats[this.materials[0]];
		materials[1] = mats[this.materials[1]];
		materials[2] = mats[this.materials[2]];
		materials[3] = mats[this.materials[3]];
		materials[4] = mats[this.materials[4]];
		materials[5] = mats[this.materials[5]];
		
		return materials;
	}
	
	public byte getWalls() {
		return walls;
	}
	
	public byte getFlags() {
		return flags;
	}
}
