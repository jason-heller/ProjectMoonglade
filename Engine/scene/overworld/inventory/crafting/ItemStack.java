package scene.overworld.inventory.crafting;

import map.Material;
import scene.overworld.inventory.Item;

public class ItemStack {
	private int item;
	private int quantity;
	
	public ItemStack(int item, int quantity) {
		this.item = item;
		this.quantity = quantity;
	}

	public int getItem() {
		return item;
	}
	
	public int getQuantity() {
		return quantity;
	}
}
