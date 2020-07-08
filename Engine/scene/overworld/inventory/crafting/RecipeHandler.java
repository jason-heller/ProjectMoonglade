package scene.overworld.inventory.crafting;

import java.util.HashMap;
import java.util.Map;

import scene.overworld.inventory.Item;
import scene.overworld.inventory.ItemData;

public class RecipeHandler {
	
	private static RecipeHandler handler = new RecipeHandler();
	
	public static RecipeHandler get() {
		return handler;
	}
	
	private Map<Recipe, ItemStack> recipes;
	
	private void initRecipes() {
		/*addRecipe(Item.STICKS, 8, new Item[] {Item.PLANKS}, new int[] {1});
		addRecipe(Item.STICK_BUNDLE, 4, new Item[] {Item.STICKS, Item.VINE}, new int[] {8, 4});
		addRecipe(Item.DOOR, new Item[] {Item.PLANKS}, new int[] {4});
		addRecipe(Item.AXE, new Item[] {Item.STONE, Item.STICKS}, new int[] {1, 1});*/
	}
	
	public RecipeHandler() {
		recipes = new HashMap<Recipe, ItemStack>();
		initRecipes();
	}
	
	public ItemStack checkForRecipe(int item1, int qty1, int item2, int qty2) {
		int[] items = new int[] {item1, item2};
		int[] amounts = new int[] {qty1, qty2};
		
		for(Recipe recipe : recipes.keySet()) {
			int multiples = 0;//recipe.compare(items, amounts);
			if (multiples != 0) {
				ItemStack stack = recipes.get(recipe);
				return new ItemStack(stack.getItem(), multiples * stack.getQuantity());
			}
		}
		
		return null;
	}
	
	private void addRecipe(int item, int amt, int[] items, int[] amounts) {
		recipes.put(new Recipe(items, amounts), new ItemStack(item, amt));
	}
	
	private void addRecipe(int item, int[] items, int[] amounts) {
		recipes.put(new Recipe(items, amounts), new ItemStack(item, 1));
	}
}
