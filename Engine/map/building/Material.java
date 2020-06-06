package map.building;

import org.joml.Vector3f;

import dev.Console;

public enum Material {
	NONE("", 0, 0), 	// Effectively not a material
	STICKS("sticks", 3, 0),
	PLANKS("planks", 0, 0),
	DRYWALL("drywall", 1, 0),
	STONE_BRICK("stone bricks", 2, 0);
	
	private String name;
	private int tx, ty;
	
	Material(String name, int tx, int ty) {
		this.name = name;
		this.tx = tx;
		this.ty = ty;
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

	public static Vector3f getTexCoordData(Vector3f v, Material id) {
		v.x = id.tx*BuildingRender.materialAtlasSize;
		v.y = id.ty*BuildingRender.materialAtlasSize;
		v.z = BuildingRender.materialAtlasSize;
		return v;
	}
}
