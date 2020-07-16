package scene.overworld.inventory;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.building.BuildingRender;
import gl.res.Texture;
import io.Input;
import scene.entity.EntityHandler;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.crafting.ItemStack;
import scene.overworld.inventory.crafting.RecipeHandler;
import ui.Image;
import ui.UI;
import util.Colors;

public class Inventory {
	private int[] items;
	private int[] quantities;
	private final int INV_WIDTH = 9;

	private int selectionPos = 0;

	private final static float itemAtlasScale = 32;
	public static float itemAtlasSize;

	// private Texture itemTexture;
	private static final int itemTexSize = 32;
	private static final int padding = 2;
	public static float scale = 1.4f;

	private Viewmodel viewmodel;

	private final int LEFT_ALIGN = 24;
	private final int BOTTOM_ALIGN = 24;
	
	private int heldItem = 0;
	private int heldQuantity = 0;

	private boolean open = false;

	public Inventory() {
		items = new int[45];
		quantities = new int[45];
		for (int i = 0; i < items.length; i++) {
			items[i] = 0;
		}

		Resources.addModel("axe", "item/axe.mod");
		Resources.addModel("spade", "item/spade.mod");
		Resources.addTexture("tools", "item/tools.png");

		if (Debug.structureMode) {
			addItem(Item.AXE, 1);
			//addItem(Item.PLANK, 1);
		}

		Texture texture = Resources.addTexture("items", "item/items.png");
		itemAtlasSize = 1f / (texture.size / itemAtlasScale);
		EntityHandler.getItemRender().setTexture(texture);
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
		
		if (getSelected() != viewmodel.getItem()) {
			viewmodel.setItem(getSelected());
		}

		// Hotbar
		
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

				if (items[id] != 0) {
					ItemData data = Item.get(items[id]);
					drawItem(data, dx, dy, 1f);
					
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
				int tempItem = items[id];
				int tempQuantity = quantities[id];
				
				items[id] = heldItem;
				quantities[id] = heldQuantity;
				
				heldItem = tempItem;
				heldQuantity = tempQuantity;
			} else if (heldItem != 0 && Input.isPressed(Input.KEY_LMB)) {
				EntityHandler.addEntity(new ItemEntity(Application.scene.getCamera().getPosition(), heldItem, heldQuantity));
				heldItem = 0;
				heldQuantity = 0;
			}
			
			if (heldItem != 0) {
				// Toss graphic
				if (id == -1) {
					UI.drawString("->", mx, my - 24);
				} else {
					// Crafting
					/*ItemStack stack = RecipeHandler.get().checkForRecipe(heldItem, heldQuantity, items[id], quantities[id]);
					if (stack != null) {
						
						int item = stack.getItem();
						UI.drawString("->", mx, my - 24);
						Image img = UI.drawImage("items", mx+24, my-24, (int)(imgSize/1.5), (int)(imgSize/1.5));
						
						ItemData data = Item.get(item);
						img.setUvOffset(data.getTX() * itemAtlasSize, data.getTY() * itemAtlasSize,
								(data.getTX() + 1) * itemAtlasSize, (data.getTY() + 1) * itemAtlasSize);
						
						if (Input.isPressed(Input.KEY_RMB)) {
							items[id] = stack.getItem();
							quantities[id] = stack.getQuantity();
							
							heldItem = 0;
							heldQuantity = 0;
						}
					}*/
				}
				
				ItemData data = Item.get(heldItem);
				drawItem(data, mx, my, 1f);
			}
		}

		for (int i = 1; i <= 9; i++) {
			if (Input.isPressed("item slot " + i)) {
				selectionPos = i - 1;
			}
		}

		int dWheel = Input.getMouseDWheel();
		if (dWheel > 0) {
			selectionPos--;
			if (selectionPos == -1) {
				selectionPos = 8;
			}
		}
		if (dWheel < 0) {
			selectionPos++;
			if (selectionPos == 9) {
				selectionPos = 0;
			}
		}
		viewmodel.update();
	}

	public static void drawItem(ItemData data, int dx, int dy, float s) {
		final int imgSize = (int) (itemTexSize * scale * s);
		
		if (data.isUsingMaterialTexture()) {
			Image img = UI.drawImage("materials", dx + padding + 8, dy + padding + 8, imgSize - 16, imgSize - 16);
			img.setUvOffset(data.getTX() * BuildingRender.materialAtlasSize, data.getTY() * BuildingRender.materialAtlasSize,
					(data.getTX() + 1) * BuildingRender.materialAtlasSize, (data.getTY() + 1) * BuildingRender.materialAtlasSize);
		} else {
			Image img = UI.drawImage("items", dx + padding, dy + padding, imgSize, imgSize);
			img.setUvOffset(data.getTX() * itemAtlasSize, data.getTY() * itemAtlasSize,
					(data.getTX() + 1) * itemAtlasSize, (data.getTY() + 1) * itemAtlasSize);
		}
	}

	public int getSelected() {
		return items[selectionPos];
	}

	public void addItem(ItemData drop, int numDrops) {
		addItem(drop.id, numDrops);
	}
	
	public void addItem(int id, int numDrops) {
		if (numDrops == 0)
			return;
		;
		for (int i = 0; i < items.length; i++) {
			if (items[i] == id || items[i] == 0) {
				items[i] = id;
				quantities[i] += numDrops;
				return;
			}
		}
	}
	
	public boolean isOpen() {
		return open;
	}

	public int[] getItems() {
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
				items[index] = 0;
			}
		}
	}

	public boolean isEmpty() {
		for(int item : items) {
			if (item != Item.AIR) return false;
		}
		
		return true;
	}
}
