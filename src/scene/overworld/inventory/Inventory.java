package scene.overworld.inventory;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import core.res.Texture;
import dev.Console;
import io.Input;
import ui.Image;
import ui.Image;
import ui.UI;

public class Inventory {
	private Item[] items;
	private int[] amount;
	private final int INV_WIDTH = 9;
	
	private int selected = 0;
	
	private Texture itemTexture;
	private int itemTexSize = 32;
	private int padding = 2;
	
	public Inventory() {
		items = new Item[36];
		amount = new int[36];
		for(int i = 0; i < items.length; i++) {
			items[i] = Item.AIR;
		}
		
		items[0] = Item.SHOVEL;
		
		itemTexture = Resources.addTexture("items", "item/items.png", GL11.GL_TEXTURE_2D, false, false, 0f,
				false, true, 16);
		//int type, boolean nearest, boolean mipmap, float bias,
		//boolean clampEdges, boolean isTransparent, int numRows
	}
	
	public void update() {
		
		// Hotbar
		int size = (itemTexSize+padding);
		int dx = (int) (640f - (size*4.5f)) - (size/2);
		int dy = 720 - (size*2);
		UI.drawString(getSelected().getName(), dx, dy-12, .15f, false);
		
		for(int i = 0; i < 9; i++) {
			if (i == selected) {
				UI.drawRect(dx-1, dy-2, size+2, size+2, Vector3f.Z_AXIS);
			}
			else {
				UI.drawRect(dx, dy, size, size, Vector3f.ZERO);
			}
			
			if (items[i] != Item.AIR ) {
				Image img = UI.drawImage("items", dx, dy, itemTexSize, itemTexSize);
				img.setUvOffset(items[i].getTX()*.125f, items[i].getTY()*.125f, .0625f, .0625f);
			}
			dx += size + padding*2;
		}
		
		for(int i = 1; i <= 9; i++) {
			if (Input.isPressed("item slot "+i)) {
				selected = i-1;
			}
		}
		
		int dWheel = Input.getMouseDWheel();
		if (dWheel > 0) {
			selected--;
			if (selected == -1) {
				selected = 8;
			}
		}
		if (dWheel < 0) {
			selected++;
			if (selected == 9) {
				selected = 0;
			}
		}
	}
	
	public Item getSelected() {
		return items[selected];
	}

	public void addItem(Item drop, int numDrops) {
		if (numDrops == 0) 
			return;
		;
		for(int i = 0; i < items.length; i++) {
			if (items[i] == drop || items[i] == Item.AIR) {
				items[i] = drop;
				amount[i] += numDrops;
				return;
			}
		}
	}
}
