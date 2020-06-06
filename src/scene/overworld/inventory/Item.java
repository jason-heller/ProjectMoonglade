package scene.overworld.inventory;

import map.building.Material;

public enum Item {
	AIR("", 0, 0, Material.NONE),
	SHOVEL("shovel", 0, 0, Material.NONE),
	COBBLE("stone", 1, 0, Material.STONE_BRICK),
	BUNDLE_OF_STICKS("sticks", 2, 0, Material.STICKS),
	PLANKS("planks", 3, 0, Material.PLANKS),
	DRYWALL("plaster", 4, 0, Material.DRYWALL);
	
	private String name;
	private int tx, ty;
	private Material material;
	
	Item(String name, int tx, int ty, Material material) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
		this.material = material;
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
