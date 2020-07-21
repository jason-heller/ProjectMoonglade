package scene.overworld;

import core.Resources;
import gl.Window;
import io.Input;
import scene.PlayableScene;
import ui.UI;
import util.Colors;

public class TileShapePicker {
	private boolean open = false;
	private float anim = 0f;
	private final int size = 80, radius = 128;
	private int selected = -1;
	
	private PlayableScene scene;
	
	//private Texture slot;
	private String[] labels = new String[] {"Walls", "Steep Slope", "Regular Slope", "Floors"};
	
	public TileShapePicker(PlayableScene scene) {
		this.scene = scene;
		Resources.addTexture("slot", "gui/slot.png");
	}
	
	public void update() {
		selected = -1;
		
		if (Input.isPressed(Input.KEY_MMB)) {
			open = !open;
			if (open) {
				Input.requestMouseRelease();
			} else {
				Input.requestMouseGrab();
			}
		}
		
		if (open) {
			anim = Math.min(anim + (5f * Window.deltaTime), 1f);
		} else {
			anim = Math.max(anim - (5f * Window.deltaTime), 0f);
		}
		
		if (anim > 0f) {
			UI.setOpacity(anim/2f);
			UI.drawCircle(640, 360, (int)(anim * radius), 1, 24, Colors.WHITE);
			UI.setOpacity(anim);
			
			int index = 0;
			float prt = (float) (2f * Math.PI) / 4;
			for (float i = 0; i < 2 * Math.PI; i += prt) {
				int dx = (int) (Math.cos(i) * radius);
				int dy = (int) (Math.sin(i) * radius);
				int x = 640+(int)(anim * dx);
				int y = 360+(int)(anim * dy);
				
				dx = Input.getMouseX() - x;
				dy = Input.getMouseY() - y;
				UI.setOpacity(.75f);
				if (dx*dx + dy*dy < size*size) {
					selected = index;
					UI.setOpacity(1f);
					UI.drawImage("slot", x, y, size, size).setCentered(true);
				}
				
				UI.drawImage("slot", x, y, size, size).setCentered(true);
				UI.drawString(labels[index], x, y+24);
				
				index++;
			}
			
			UI.setOpacity(1f);
		}
		
		if (Input.isPressed(Input.KEY_LMB) && open) {
			switch(selected) {
			case 0:
				scene.setTileShape(0);
				break;
				
			case 1:
				scene.setTileShape(2);
				break;
				
			case 2:
				
				break;
			case 3:
				scene.setTileShape(1);
				break;
			}
			
			open = false;
			Input.requestMouseGrab();
		}
	}
	
	public void cleanUp() {
		Resources.getTexture("slot").delete();
	}
}
