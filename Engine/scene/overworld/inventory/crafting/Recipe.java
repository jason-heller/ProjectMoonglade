package scene.overworld.inventory.crafting;

import scene.overworld.inventory.Item;

public class Recipe {
	
	private int[] items;
	private int[] amounts;

	public Recipe(String[] items, int[] amounts) {
		this.items = new int[items.length];
		for(int i = 0; i < items.length; i++) {
			this.items[i] = Item.getId(items[i]);
		}
		
		this.amounts = amounts;
	}

	// TODO: Optimize
	public boolean compare(int[] items, int[] amounts) {
		boolean[] conditions = new boolean[this.items.length];
		
		for(int i = 0; i < items.length; i++) {
			
			for(int j = 0; j < this.items.length; j++) {
				if (items[i] == this.items[j] && amounts[i] >= this.amounts[j]) {
					conditions[j] = true;
				}
			}
		}
				
		for(boolean condition : conditions) {
			if (!condition) return false;
		}
		
		return true;
	}

	public int[] getItems() {
		return items;
	}
	
	public int[] getAmounts() {
		return amounts;
	}
}
