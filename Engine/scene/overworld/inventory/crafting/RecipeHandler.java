package scene.overworld.inventory.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scene.overworld.inventory.Inventory;

public class RecipeHandler {
	
	private List<Recipe> possibleRecipes;
	
	private Map<Recipe, ItemStack> recipes;
	
	private void initRecipes() {
		addRecipe("stick_bundle", 4, new String[] {"stick", "rope"}, new int[] {8, 4});
		addRecipe("door", new String[] {"planks"}, new int[] {4});
		addRecipe("axe", new String[] {"stone", "stick"}, new int[] {1, 1});
		
		addRecipe("stone_bricks", 2, new String[] {"stone"}, new int[] {2});
		addRecipe("stone_wall", 2, new String[] {"stone"}, new int[] {2});
		
		addRecipe("forge", new String[] {"stone"}, new int[] {4});
		addRecipe("sheet_metal", new String[] {"reclaimed_metal"}, new int[] {1});
	}
	
	public RecipeHandler() {
		recipes = new HashMap<Recipe, ItemStack>();
		possibleRecipes = new ArrayList<Recipe>();
		initRecipes();
	}
	
	public void update(Inventory inv) {
		
		possibleRecipes.clear();
		
		for(Recipe recipe : recipes.keySet()) {
			if (recipe.compare(inv.getItems(), inv.getQuantities())) {
				possibleRecipes.add(recipe);
			}
		}
	}
	
	public ItemStack getRecipeResult(Recipe recipe) {
		return recipes.get(recipe);
	}
	
	private void addRecipe(String item, int amount, String[] items, int[] amounts) {
		recipes.put(new Recipe(items, amounts), new ItemStack(item, amount));
	}
	
	private void addRecipe(String item, String[] items, int[] amounts) {
		addRecipe(item, 1, items, amounts);
	}

	public List<Recipe> getPotentialRecipes() {
		return possibleRecipes;
	}

	public void craft(Inventory inv, Recipe recipe, int amt) {
		//possibleRecipes.remove(recipe);
		int recipeIndex = 0;
		int count = recipe.getAmounts()[recipeIndex];
		int[] items = inv.getItems();
		int[] amounts = inv.getQuantities();
		for(int i = 0; i < items.length; i++) {
			if (isRequiredItem(items[i], recipe)) {
				if (amounts[i] <= count) {
					items[i] = 0;
					count -= amounts[i];
					amounts[i] = 0;
				} else {
					amounts[i] -= count;
					count = 0;
				}
				
				if (count == 0) {
					if (recipeIndex + 1 == recipe.getItems().length) {
						ItemStack itemStack = recipes.get(recipe);
						inv.addItem(itemStack.getItem(), itemStack.getQuantity());
						return;
					}
					recipeIndex++;
					count = recipe.getAmounts()[recipeIndex];
				}
			}
		}
	}

	private boolean isRequiredItem(int item, Recipe recipe) {
		for(int recipeItem : recipe.getItems()) {
			if (item == recipeItem) {
				return true;
			}
		}
		
		return false;
	}
}
