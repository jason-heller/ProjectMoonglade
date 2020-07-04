package scene.overworld.inventory;

import map.Chunk;
import map.Material;
import map.tile.BuildingTile;

public enum Item {
	AIR("", 0, 0, Material.NONE),
	SPADE("spade", 1, 1, Material.NONE),
	TROWEL("trowel", 2, 1, Material.NONE),
	AXE("axe", 0, 1, Material.NONE),
	STONE("stone", 1, 0, Material.STONE_BRICK),
	STICKS("sticks", 9, 0, Material.STICK),
	STICK_BUNDLE("stick_bundle", 2, 0, Material.STICK_BUNDLE),
	PLANKS("planks", 3, 0, Material.PLANKS),
	DRYWALL("drywall", 4, 0, Material.DRYWALL),
	BRICKS("brick", 5, 0, Material.BRICK),
	GLASS("glass", 6, 0, Material.WINDOW),
	THATCH("thatch", 7, 0, Material.THATCH),
	FENCE("fence", 8, 0, Material.FENCE),
	VINE("vine", 8, 0, Material.VINE),
	SHINGLES("shingles", 0, 0, Material.SHINGLES),
	
	RED_PAINT("red_paint", 1, 1, ItemAction.PAINT), 
	ORANGE_PAINT("orange_paint", 2, 1, ItemAction.PAINT), 
	YELLOW_PAINT("yellow_paint", 3, 1, ItemAction.PAINT), 
	GREEN_PAINT("green_paint", 4, 1, ItemAction.PAINT), 
	CYAN_PAINT("cyan_paint", 5, 1, ItemAction.PAINT), 
	BLUE_PAINT("blue_paint", 6, 1, ItemAction.PAINT), 
	INDIGO_PAINT("indigo_paint", 7, 1, ItemAction.PAINT), 
	VIOLET_PAINT("violet_paint", 8, 1, ItemAction.PAINT), 
	FOREST_GREEN_PAINT("forest_green_paint", 12, 1, ItemAction.PAINT), 
	DARK_GREY_PAINT("dark_grey_paint", 9, 1, ItemAction.PAINT), 
	LIGHT_GREY_PAINT("light_grey_paint", 10, 1, ItemAction.PAINT), 
	SILVER_PAINT("silver_paint", 11, 1, ItemAction.PAINT), 
	
	DOOR("door", 9, 0, 3);	
	
	private final String name;
	private final int tx, ty;
	private final Material material;
	private int entity = -1;
	private ItemAction action = ItemAction.NONE;
	
	Item(String name, int tx, int ty, Material m) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
		this.material = m;
	}
	
	Item(String name, int tx, int ty, int entity) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
		this.entity = entity;
		this.material = Material.NONE;
	}
	
	Item(String name, int tx, int ty, ItemAction action) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
		this.action = action;
		this.material = Material.NONE;
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
	
	public int getEntityId() {
		return entity;
	}

	public boolean doAction(BuildingTile tile, int facingIndex, Chunk chunk) {
		return action.doAction(this, facingIndex, tile, chunk);
	}
}
