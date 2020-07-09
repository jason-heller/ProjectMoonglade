package scene.overworld;

import org.lwjgl.input.Keyboard;

import io.Input;
import ui.UI;
import ui.menu.GuiTextbox;
import util.Colors;

public class StructureEditUI {
	private Overworld scene;
	private boolean exportOpen = false;
	
	private GuiTextbox name;
	
	public StructureEditUI(Overworld scene) {
		this.scene = scene;
	
	}
	
	public void cleanUp() {
	}
	
	public void update() {
		if (Input.isDown(Keyboard.KEY_LCONTROL)) {
			if (Input.isPressed(Keyboard.KEY_E)) {
				exportOpen = true;
				Input.requestMouseRelease();
			}
		}
		
		if (exportOpen) {
			UI.drawRect(1280/2 - 320, 720/2 - 160, 640, 320, Colors.GREY);
			
			
		}
	}
}
