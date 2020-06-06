package scene.overworld;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import io.Controls;
import io.Input;
import scene.menu.pause.OptionsPanel;
import ui.Colors;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.listener.MenuListener;

public class OverworldUI {

	private final GuiMenu mainMenu;
	private final OptionsPanel options;
	
	private final Overworld scene;
	
	private final int CROSSHAIR_SIZE = 8;
	private final int CROSSHAIR_THICKNESS = 1;
	private final Vector3f CROSSHAIR_COLOR = new Vector3f(1, 1, 1);
	
	private boolean paused;
	
	public OverworldUI(Overworld scene) {
		this.scene = scene;
		mainMenu = new GuiMenu(50, 300, "resume", "options", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);

		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				
				switch (index) {
				case 0:
					options.setFocus(false);
					paused = false;
					break;
				case 1:
					options.setFocus(!options.isFocused());
					break;
				case 2:
					scene.returnToMenu = true;
					options.setFocus(false);
					break;
				}
			}

		});
	}
	
	public void cleanUp() {
	}
	
	public void update() {
		if (scene.isLoading()) {
			UI.drawString("Loading", 720, 360);
			return;
		}

		UI.drawRect(640 - CROSSHAIR_THICKNESS, 360 - CROSSHAIR_SIZE, 2 * CROSSHAIR_THICKNESS, CROSSHAIR_SIZE,
				CROSSHAIR_COLOR);
		UI.drawRect(640 - CROSSHAIR_THICKNESS, 360, 2 * CROSSHAIR_THICKNESS, CROSSHAIR_SIZE, CROSSHAIR_COLOR);
		UI.drawRect(640 - (CROSSHAIR_SIZE + 1), 360 - CROSSHAIR_THICKNESS, CROSSHAIR_SIZE, 2 * CROSSHAIR_THICKNESS,
				CROSSHAIR_COLOR);
		UI.drawRect(640, 360 - CROSSHAIR_THICKNESS, CROSSHAIR_SIZE, 2 * CROSSHAIR_THICKNESS, CROSSHAIR_COLOR);

		if (Input.isPressed(Controls.get("pause"))) {
			if (!paused) {
				Mouse.setGrabbed(false);
				paused = true;
			} else {
				if (options.isFocused()) {
					options.setFocus(false);
				} else {
					paused = false;
					//if (!Console.isVisible()) {
						Mouse.setGrabbed(true);
					//}
				}
			}
		}
		
		if (paused) {
			UI.drawRect(0, 0, 1280, 720, Colors.BLACK).setOpacity(.5f);
			if (options.isFocused()) {
				options.update();
				options.draw();
			} else {
				mainMenu.draw();
			}
		}
	
	}
}
