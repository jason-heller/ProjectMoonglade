package scene.overworld.inventory;

import org.joml.Vector3f;

import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.res.Texture;
import io.Input;
import scene.overworld.inventory.crafting.ItemStack;
import scene.overworld.inventory.crafting.RecipeHandler;
import ui.Colors;
import ui.Image;
import ui.UI;

public class Inventory {
	private Item[] items;
	private int[] quantities;
	private final int INV_WIDTH = 9;

	private int selectionPos = 0;

	private final static float itemAtlasScale = 32;
	private static float itemAtlasSize;

	// private Texture itemTexture;
	private final int itemTexSize = 32;
	private final int padding = 2;
	public static float scale = 1.4f;

	private Viewmodel viewmodel;

	private final int LEFT_ALIGN = 24;
	private final int BOTTOM_ALIGN = 24;
	
	private Item heldItem = Item.AIR;
	private int heldQuantity = 0;

	private boolean open = false;

	public Inventory() {
		items = new Item[45];
		quantities = new int[45];
		for (int i = 0; i < items.length; i++) {
			items[i] = Item.AIR;
		}

		Resources.addModel("axe", "item/axe.mod");
		Resources.addModel("spade", "item/spade.mod");
		Resources.addTexture("tools", "item/tools.png");

		if (Debug.structureMode) {
			addItem(Item.SPADE, 1);
			addItem(Item.AXE, 1);
			addItem(Item.PLANKS, 1);
			addItem(Item.STONE, 1);
			addItem(Item.GLASS, 1);
			addItem(Item.BRICKS, 1);
			addItem(Item.DRYWALL, 1);
			addItem(Item.THATCH, 1);
			addItem(Item.STICKS, 1);
		}

		Texture texture = Resources.addTexture("items", "item/items.png");
		itemAtlasSize = 1f / (texture.size / itemAtlasScale);
		// int type, boolean nearest, boolean mipmap, float bias,
		// boolean clampEdges, boolean isTransparent, int numRows

		viewmodel = new Viewmodel();
	}

	public void update() {

		if (Input.isPressed("use_backpack")) {
			open = !open;

			if (!open) {
				Input.requestMouseGrab();
			} else {
				Input.requestMouseRelease();
			}
		}

		// Hotbar
		final int imgSize = (int) (itemTexSize * scale);
		int size = (int) ((itemTexSize + padding) * scale);
		int slotSize = (padding * 2) + size;

		int dx, dy;
		int numRows = (open) ? 5 : 1;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < numRows; j++) {
				dx = LEFT_ALIGN + (i * slotSize);
				dy = (720 - BOTTOM_ALIGN) - ((j + 1) * slotSize);
				int id = i + (j * 9);

				UI.drawRect(dx, dy, size, size, Colors.BLACK).setOpacity(.7f);

				if (items[id] != Item.AIR) {
					Image img = UI.drawImage("items", dx + padding, dy + padding, imgSize, imgSize);
					img.setUvOffset(items[id].getTX() * itemAtlasSize, items[id].getTY() * itemAtlasSize,
							(items[id].getTX() + 1) * itemAtlasSize, (items[id].getTY() + 1) * itemAtlasSize);
				}

				final int amt = quantities[id];
				if (amt != 0) {
					UI.drawString(Integer.toString(amt), dx + 2, dy + 2, .2f, false);
				}
			}
		}

		UI.drawHollowRect(LEFT_ALIGN + (selectionPos * slotSize) - 2, (720 - BOTTOM_ALIGN) - (size + 4), size + 2,
				size + 2, 2, Colors.WHITE);
		
		int mx = Input.getMouseX();
		int my = Input.getMouseY();
		
		if (open) {
			final int INV_TOP = (720 - BOTTOM_ALIGN) - slotSize * 5;
			int id = -1;
			
			if (mx >= LEFT_ALIGN && mx < LEFT_ALIGN + (9 * slotSize)
			&&  my >= (720 - BOTTOM_ALIGN) - (5 * slotSize) && my < (720 - BOTTOM_ALIGN)) {
				
				
				id = ((mx - LEFT_ALIGN) / slotSize);
				id += (4 - ((my - INV_TOP) / slotSize)) * 9;
			}
			
			if (id != -1 && Input.isPressed(Input.KEY_LMB)) {
				Item tempItem = items[id];
				int tempQuantity = quantities[id];
				
				items[id] = heldItem;
				quantities[id] = heldQuantity;
				
				heldItem = tempItem;
				heldQuantity = tempQuantity;
			} else if (heldItem != Item.AIR) {
				// DROP SHIT
			}
			
			if (heldItem != Item.AIR) {
				// Toss graphic
				if (id == -1) {
					UI.drawString("->", mx, my - 24);
				} else {
					// Crafting
					ItemStack stack = RecipeHandler.get().checkForRecipe(heldItem, heldQuantity, items[id], quantities[id]);
					if (stack != null) {
						
						Item item = stack.getItem();
						UI.drawString("->", mx, my - 24);
						Image img = UI.drawImage("items", mx+24, my-24, (int)(imgSize/1.5), (int)(imgSize/1.5));
						img.setUvOffset(item.getTX() * itemAtlasSize, item.getTY() * itemAtlasSize,
								(item.getTX() + 1) * itemAtlasSize, (item.getTY() + 1) * itemAtlasSize);
						
						if (Input.isPressed(Input.KEY_RMB)) {
							items[id] = stack.getItem();
							quantities[id] = stack.getQuantity();
							
							heldItem = Item.AIR;
							heldQuantity = 0;
						}
					}
				}
				
				Image img = UI.drawImage("items", mx, my, imgSize, imgSize);
				img.setUvOffset(heldItem.getTX() * itemAtlasSize, heldItem.getTY() * itemAtlasSize,
						(heldItem.getTX() + 1) * itemAtlasSize, (heldItem.getTY() + 1) * itemAtlasSize);
			}
		}

		for (int i = 1; i <= 9; i++) {
			if (Input.isPressed("item slot " + i)) {
				selectionPos = i - 1;
				viewmodel.set(getSelected());
			}
		}

		int dWheel = Input.getMouseDWheel();
		if (dWheel > 0) {
			selectionPos--;
			if (selectionPos == -1) {
				selectionPos = 8;
			}
			viewmodel.set(getSelected());
		}
		if (dWheel < 0) {
			selectionPos++;
			if (selectionPos == 9) {
				selectionPos = 0;
			}
			viewmodel.set(getSelected());
		}
		viewmodel.update();
	}

	public Item getSelected() {
		return items[selectionPos];
	}

	public void addItem(Item drop, int numDrops) {
		if (numDrops == 0)
			return;
		;
		for (int i = 0; i < items.length; i++) {
			if (items[i] == drop || items[i] == Item.AIR) {
				items[i] = drop;
				quantities[i] += numDrops;
				return;
			}
		}
	}
	
	public boolean isOpen() {
		return open;
	}

	public Item[] getItems() {
		return items;
	}

	public int[] getQuantities() {
		return quantities;
	}

	public void render(Camera camera, Vector3f lightDirection) {
		viewmodel.render(camera, lightDirection);
	}

	public int getSelectionPos() {
		return selectionPos;
	}

	public void consume(int index) {
		if (!Debug.structureMode) {

			quantities[index]--;

			if (quantities[index] == 0) {
				items[index] = Item.AIR;
			}
		}
	}
}
