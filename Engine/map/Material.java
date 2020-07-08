package map;

import org.joml.Vector3f;

import gl.building.BuildingRender;
import gl.res.TileModel;
import map.tile.Tile;
import map.tile.TileModels;
import scene.overworld.inventory.Item;

/**
 * @author tjaso
 *
 */
public enum Material {
	
	NONE("", 0, 0, "air", false, false), 	// Effectively not a material
	STICK("stick", 6, 0, "stick", TileModels.DEFAULT, false, false, false, false, 0),
	STICK_BUNDLE("stick_bundle", 3, 0, "stick_bundle", TileModels.FILLED, false, false, true, false, 0),
	PLANKS("planks", 0, 1, null, TileModels.FILLED, false, false, true, true, 13),
	DRYWALL("drywall", 1, 0, null, TileModels.DEFAULT, false, false, true, true, 0),
	STONE_BRICK("stone_bricks", 2, 0, TileModels.FILLED),
	STONE_WALL("stone_wall", 8, 0, TileModels.FILLED),
	BRICK("bricks", 4, 0, TileModels.FILLED),
	WINDOW("window", 0, 13, null, true, true),
	THATCH("thatch", 5, 0, null, TileModels.FILLED, false, false, true, false, 0),
	FENCE("fence", 0, 16, null, true, true),
	VINE("vine", 0, 16, "vine", false, true),
	SHINGLES("shingles", 1, 1, null, TileModels.FILLED, false, false, true, true, 14),
	METAL_MESH("metal_mesh", 7, 0, TileModels.FILLED);
	
	private String name;
	private int tx, ty;
	private boolean tiling;
	private boolean transparent = false;
	private boolean colorable = false;
	private String drop;
	private boolean solid = true;
	private byte initialFlags = 0;
	private TileModels tileModel;
	
	Material(String name, int tx, int ty, TileModels tileModel) {
		this(name, tx, ty, null, tileModel, false, false, true, false, 0);
	}
	
	Material(String name, int tx, int ty, String drop, boolean tiling, boolean transparent) {
		this(name, tx, ty, drop, TileModels.DEFAULT, tiling, transparent, true, false, 0);
	}
	
	Material(String name, int tx, int ty, String drop, TileModels tileModel, boolean tiling, boolean transparent, boolean solid, boolean colorable, int initialFlags) {
		this.name = name;
		this.tx = tx;
		this.ty = ty;
		
		this.tiling = tiling;
		this.transparent = transparent;
		this.solid = solid;
		this.colorable = colorable;
		this.tileModel = tileModel;
		this.initialFlags = (byte)initialFlags;
		
		if (drop != null) {
			this.drop = name;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getTX() {
		return tx;
	}
	
	public int getTY() {
		return ty;
	}
	
	public int getDrop() {
		return Item.getId(drop);
	}
	
	public byte getInitialFlags() {
		return initialFlags;
	}

	public static Vector3f getTexCoordData(Vector3f v, Material id, byte flags) {
		if (id.isTiling()) {
			final float dx = flags % 16;
			final float dy = flags / 16;
			
			v.x = (id.tx + dx) * BuildingRender.materialAtlasSize;
			v.y = (id.ty + dy) * BuildingRender.materialAtlasSize;
			v.z = BuildingRender.materialAtlasSize;
		} else {
			v.x = id.tx * BuildingRender.materialAtlasSize;
			v.y = id.ty * BuildingRender.materialAtlasSize;
			v.z = BuildingRender.materialAtlasSize;
		}
		return v;
	}

	public boolean isTiling() {
		return tiling;
	}
	
	public boolean isSolid() {
		return solid;
	}

	public TileModel getTileModel() {
		return tileModel.getModel();
	}
	
	/** This will probably be confusing, maps a byteflag variable to a 16x16 (+) offset in the
	 * material texture offset. This is to be used for tiling materials. Note that this expects a 
	 * specific arrangement of tiles in the texture atlas.
	 * @param specialFlags, byte flags, 1 = left, 2 = right, 4 = top, 8 = bottom, rest unused
	 * @return
	 */
	private static byte tileFlagsToUvOffset(byte specialFlags) {
		switch(specialFlags) {
		case 1: return 2;
		case 2: return 1;
		case 4: return 32;
		case 8: return 16;
		case 5: return 37;
		case 6: return 35;
		case 9: return 5;
		case 10: return 3;
		case 13: return 21;
		case 14: return 19;
		case 15: return 20;
		case 7: return 36;
		case 3: return 33;
		case 12: return 34;
		case 11: return 4;
		
		default:
			return 0;
		}
	}
	
	private static byte flipUvOffset(byte uvOffset) {
		switch(uvOffset) {
		case 1: return 2;
		case 2: return 1;
		case 3: return 5;
		case 5: return 3;
		case 19: return 21;
		case 21: return 19;
		case 35: return 37;
		case 37: return 35;
		
		default:
			return uvOffset;
		}
	}

	public static byte setTilingFlags(Tile originator, Terrain terrain, float rx, float ry, float rz, float dx, float dz, Material mat, int facingIndex, int iterations) {
		byte specialFlags = 0;
		
		Tile l, r, t, b;
		l = terrain.getTileAt(rx - dx, ry, rz - dz);
		r = terrain.getTileAt(rx + dx, ry, rz + dz);
		t = terrain.getTileAt(rx,  ry + Tile.TILE_SIZE,  rz);
		b = terrain.getTileAt(rx, ry - Tile.TILE_SIZE,  rz);
		
		specialFlags += (l == null) ? 0 : (l.getMaterial(facingIndex) == mat) ? 1 : 0;
		specialFlags += (r == null) ? 0 : (r.getMaterial(facingIndex) == mat) ? 2 : 0;
		specialFlags += (t == null) ? 0 : (t.getMaterial(facingIndex) == mat) ? 4 : 0;
		specialFlags += (b == null) ? 0 : (b.getMaterial(facingIndex) == mat) ? 8 : 0;
		
		if (specialFlags != 0) {
			
			specialFlags = Material.tileFlagsToUvOffset(specialFlags);
			originator.setFlags(facingIndex, specialFlags);
			
			mimicOnFlipside(terrain, originator, rx, ry, rz, facingIndex);
			
			if (iterations == 0) {
				if (l != null)
					setTilingFlags(l, terrain, rx - dx, ry, rz - dz, dx, dz, mat, facingIndex, iterations + 1);
				if (r != null)
					setTilingFlags(r, terrain, rx + dx, ry, rz + dz, dx, dz, mat, facingIndex, iterations + 1);
				if (t != null)
					setTilingFlags(t, terrain, rx,  ry + Tile.TILE_SIZE,  rz, dx, dz, mat, facingIndex, iterations + 1);
				if (b != null)
					setTilingFlags(b, terrain, rx,  ry - Tile.TILE_SIZE,  rz, dx, dz, mat, facingIndex, iterations + 1);
			}
		} else {
			originator.setFlags(facingIndex, (byte)0);
			mimicOnFlipside(terrain, originator, rx, ry, rz, facingIndex);
		}
		
		return specialFlags;
	}
	
	public static void removeTilingFlags(Tile originator, Terrain terrain, float rx, float ry, float rz, float dx, float dz, int facingIndex, int iterations) {
		Tile l, r, t, b;
		l = terrain.getTileAt(rx - dx, ry, rz - dz);
		r = terrain.getTileAt(rx + dx, ry, rz + dz);
		t = terrain.getTileAt(rx,  ry + Tile.TILE_SIZE,  rz);
		b = terrain.getTileAt(rx, ry - Tile.TILE_SIZE,  rz);
		
		for(int i = 0; i < 6; i++) {
			originator.materials[i] = Material.NONE;
		}
		mimicOnFlipside(terrain, originator, rx, ry, rz, facingIndex);
		
		if (iterations == 0) {
			if (l != null)
				setTilingFlags(l, terrain, rx - dx, ry, rz - dz, dx, dz, l.getMaterial(facingIndex), facingIndex, iterations + 1);
			if (r != null)
				setTilingFlags(r, terrain, rx + dx, ry, rz + dz, dx, dz, r.getMaterial(facingIndex), facingIndex, iterations + 1);
			if (t != null)
				setTilingFlags(t, terrain, rx,  ry + Tile.TILE_SIZE, rz, dx, dz, t.getMaterial(facingIndex), facingIndex, iterations + 1);
			if (b != null)
				setTilingFlags(b, terrain, rx,  ry - Tile.TILE_SIZE, rz, dx, dz, b.getMaterial(facingIndex), facingIndex, iterations + 1);
		}
	}
	
	private static void mimicOnFlipside(Terrain terrain, Tile tile, float x, float y, float z, int facingIndex) {
		byte specialFlags = flipUvOffset(tile.getFlags()[facingIndex]);
		Tile flipTile = null;
		//TODO: This igores the flip tile possibly having more sides to it
		switch(tile.getWalls()) {
		case 1:
			if ((flipTile = terrain.getTileAt(x-Tile.TILE_SIZE, y, z)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		case 2:
			if ((flipTile = terrain.getTileAt(x+Tile.TILE_SIZE, y, z)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		case 4:
			if ((flipTile = terrain.getTileAt(x, y+Tile.TILE_SIZE, z)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		case 8:
			if ((flipTile = terrain.getTileAt(x, y-Tile.TILE_SIZE, z)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		case 16:
			if ((flipTile = terrain.getTileAt(x, y, z-Tile.TILE_SIZE)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		case 32:
			if ((flipTile = terrain.getTileAt(x, y, z+Tile.TILE_SIZE)) != null)
				flipTile.setFlags(facingIndex, specialFlags);
			break;
		}
	}

	public boolean isTransparent() {
		return transparent;
	}

	public boolean isColorable() {
		return colorable;
	}

	public String getDropName() {
		return drop;
	}

	public void setDrop(String drop) {
		this.drop = drop;
	}
}
 