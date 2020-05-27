package scene.overworld.inventory;

public enum Item {
	AIR("", 0, 0),
	SHOVEL("Shovel", 0, 0),
	COBBLE("Stone", 1, 0);
	
	private String name;
	private int tx, ty;
	
	Item(String name, int tx, int ty) {
		this.tx = tx;
		this.ty = ty;
		this.name = name;
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
}
