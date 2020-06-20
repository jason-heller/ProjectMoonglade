package scene.overworld.inventory.crafting;

import scene.overworld.inventory.Item;

public class ItemStack {
	private Item item;
	private int quantity;
	
	public ItemStack(Item item, int quantity) {
		this.item = item;
		this.quantity = quantity;
	}

	public Item getItem() {
		return item;
	}
	
	public int getQuantity() {
		return quantity;
	}
}
