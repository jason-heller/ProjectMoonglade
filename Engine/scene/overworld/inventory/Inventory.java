package scene.overworld.inventory;

import org.joml.Vector3f;

import core.Resources;
import core.res.Texture;
import gl.Camera;
import io.Input;
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
	
	//private Texture itemTexture;
	private final int itemTexSize = 32;
	private final int padding = 2;
	public static float scale = 1.4f;
	
	private Viewmodel viewmodel;
	
	private final int LEFT_ALIGN = 24;
	private final int BOTTOM_ALIGN = 24;
	
	public Inventory() {
		items = new Item[36];
		quantities = new int[36];
		for(int i = 0; i < items.length; i++) {
			items[i] = Item.AIR;
		}
		
		Resources.addModel("axe", "item/axe.mod");
		Resources.addModel("spade", "item/spade.mod");
		Resources.addTexture("tools", "item/tools.png");
		
		Texture texture = Resources.addTexture("items", "item/items.png");
		itemAtlasSize = 1f / (texture.size / itemAtlasScale);
		//int type, boolean nearest, boolean mipmap, float bias,
		//boolean clampEdges, boolean isTransparent, int numRows
		
		viewmodel = new Viewmodel();
	}
	
	public void update() {
		
		// Hotbar
		int size = (int)((itemTexSize + padding) * scale);
		//int dx = (int) (640f - (size*4.5f)) - (size/2);
		//int dy = 720 - (size*2);
		int dx = LEFT_ALIGN;
		int dy = (720 - BOTTOM_ALIGN) - size;
		UI.drawString(getSelected().getName(), dx, dy-18, .2f, false);
		
		for(int i = 0; i < 9; i++) {
			UI.drawRect(dx, dy, size, size, Colors.BLACK).setOpacity(.7f);
			
			if (i == selectionPos) {
				UI.drawHollowRect(dx, dy, size+2, size+2, 2, Colors.WHITE);
			}
	
			if (items[i] != Item.AIR ) {
				final int imgSize = (int)(itemTexSize*scale);
				Image img = UI.drawImage("items", dx+padding, dy+padding, imgSize, imgSize);
				img.setUvOffset(items[i].getTX()*itemAtlasSize, items[i].getTY()*itemAtlasSize,
						(items[i].getTX()+1)*itemAtlasSize, (items[i].getTY()+1)*itemAtlasSize);
			}

			final int amt = quantities[i];
			if (amt != 0) {
				UI.drawString(Integer.toString(amt), dx+2, dy+2, .2f, false);
			}

			dx += size + padding*2;
		}
		
		for(int i = 1; i <= 9; i++) {
			if (Input.isPressed("item slot "+i)) {
				selectionPos = i-1;
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
		for(int i = 0; i < items.length; i++) {
			if (items[i] == drop || items[i] == Item.AIR) {
				items[i] = drop;
				quantities[i] += numDrops;
				return;
			}
		}
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
		quantities[index]--;
		
		if(quantities[index] == 0) {
			items[index] = Item.AIR;
		}
	}
}
