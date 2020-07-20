package scene.overworld.inventory.crafting;

import java.util.List;

import io.Input;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import ui.UI;
import util.Colors;

public class CraftingUI {
	private RecipeHandler handler;
	
	private static final int TOP = 441, BOTTOM = 720-26, LEFT = 490, RIGHT = 490+256;
	
	public CraftingUI(RecipeHandler handler) {
		this.handler = handler;
	}
	
	public void update(Inventory inventory) {
		if (inventory.isOpen()) {
			List<Recipe> recipes = handler.getPotentialRecipes();
			int len = recipes.size();
			
			int selected = -1;
			
			UI.drawString("Crafting", LEFT, TOP-25);
			UI.drawRect(LEFT, TOP, RIGHT-LEFT, BOTTOM-TOP, Colors.BLACK).setOpacity(.6f);
			
			for(int i = 0; i < len; i++) {
				Recipe recipe = recipes.get(i);
				
				int my = Input.getMouseY();
				int mx = Input.getMouseX();
				int y = BOTTOM - (i*40) - 40;
				if (my >= y && my < y + 40 && mx > LEFT && mx < RIGHT) {
					selected = i;
				}
				
				if (selected == i) {
					UI.drawRect(LEFT, y, 256, 40, Colors.SILVER).setOpacity(.25f);
				}
				
				for(int j = 0; j < recipe.getItems().length; j++) {
					int item = recipe.getItems()[j];
					int amt = recipe.getAmounts()[j];
					
					Inventory.drawItem(Item.get(item), LEFT + 60 + (j*40), y, .65f);
					UI.drawString("x"+amt, LEFT + 60 + 24 + (j*40), y + 16, .26f, false);
				}
				
				if (selected == i && Input.isPressed("attack")) {
					handler.craft(inventory, recipe, 1);
				}
				
				ItemStack itemStack = handler.getRecipeResult(recipe);
				Inventory.drawItem(Item.get(itemStack.getItem()), LEFT, BOTTOM - (i*40) - 42, 1f);
				UI.drawString("x"+itemStack.getQuantity(), LEFT + 24, BOTTOM - (i*40) - 40 + 20, .26f, false);
				
				
			}
		}
	}
}
