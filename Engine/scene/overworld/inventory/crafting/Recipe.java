package scene.overworld.inventory.crafting;

public class Recipe {
	
	private int[] items;
	private int[] amounts;

	public Recipe(int[] items, int[] amounts) {
		this.items = new int[items.length];
		for(int i = 0; i < items.length; i++) {
			this.items[i] = items[i];
		}
		
		this.amounts = amounts;
	}

	public int compare(int[] items, int[] amounts) {
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
