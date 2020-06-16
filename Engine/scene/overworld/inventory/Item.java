package scene.overworld.inventory;

import dev.Console;
import map.Material;

public enum Item {
	AIR("", 0, 0, Material.NONE),
	SPADE("spade", 1, 1, Material.NONE),
	TROWEL("trowel", 2, 1, Material.NONE),
	AXE("axe", 0, 1, Material.NONE),
	COBBLE("stone", 1, 0, Material.STONE_BRICK),
	BUNDLE_OF_STICKS("sticks", 2, 0, Material.STICKS),
	PLANKS("planks", 3, 0, Material.PLANKS),
	DRYWALL("plaster", 4, 0, Material.DRYWALL),
	BRICKS("brick", 5, 0, Material.BRICK),
	GLASS("glass", 6, 0, Material.WINDOW),
	THATCH("thatch", 7, 0, Material.THATCH),
	FENCE("fence", 8, 0, Material.FENCE);
	
	private final String name;
	private final int tx, ty;
	private final Material material;
	
	Item(String name, int tx, int ty, Material m) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
		this.material = m;
	}
	
	public int getTX() {
		return tx;
	}
	
	public int getTY() {
		return ty;
	}

	public String getName() {
		return name;
	}
	
	public Material getMaterial() {
		return material;
	}
}
