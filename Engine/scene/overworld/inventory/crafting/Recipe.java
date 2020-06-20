package scene.overworld.inventory.crafting;

import scene.overworld.inventory.Item;

public class Recipe {
	
	private Item[] items;
	private int[] amounts;

	public Recipe(Item[] items, int[] amounts) {
		this.items = items;
		this.amounts = amounts;
	}

	public int compare(Item[] items, int[] amounts) {
		if (this.items.length != items.length) 
			return 0;
				
		int multiples = 0 ;
		
		for(int i = 0; i < items.length; i++) {
			
			int m = 0;
			for(int j = 0; j < this.items.length; j++) {
				if (items[i] == this.items[j] && amounts[i] >= this.amounts[j]) {
					m = (int)(amounts[i] / this.amounts[j]);
					multiples = (multiples == 0) ? m : Math.min(multiples, m);
					break;
				}
			}
			
			if (m == 0) {
				return 0;
			}
		}
				
		return multiples;
	}
}
