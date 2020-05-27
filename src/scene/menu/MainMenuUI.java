package scene.menu;

import core.Application;
import core.Globals;
import core.Resources;
import core.res.Texture;
import dev.Console;
import scene.Scene;
import scene.menu.pause.OptionsPanel;
import scene.overworld.Overworld;
import ui.Image;
import ui.Text;
import ui.UI;
import ui.menu.GuiMenu;
import ui.menu.listener.MenuListener;

public class MainMenuUI {

	private final Text title;
	private final Image background;
	private final GuiMenu mainMenu;
	private final OptionsPanel options;

	private final Texture mainMenuBg;
	
	private final Scene scene;
	
	public MainMenuUI(Scene scene) {
		this.scene = scene;
		mainMenuBg = Resources.addTexture("main_menu_bg", "gui/main_menu.png");

		mainMenu = new GuiMenu(50, 300, "new game", "continue", "options", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);

		title = new Text("Leatherboot", 50, 125, .75f, false);
		background = new Image(mainMenuBg, 0, 0, (int) Globals.guiWidth, (int) Globals.guiHeight);

		UI.addComponent(background);
		UI.addComponent(title);

		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				switch (index) {
				case 0:
					Application.changeScene(Overworld.class);
					options.setFocus(false);
					break;
				case 1:
					break;
				case 2:
					options.setFocus(!options.isFocused());
					break;
				case 3:
					Console.send("quit");
					break;
				}
			}

		});
	}
	
	public void cleanUp() {
		mainMenuBg.delete();
	}
	
	public void update() {
		if (scene.isLoading()) {
			UI.drawString("Loading", 720, 360);
			return;
		}

		mainMenu.draw();
		if (options.isFocused()) {
			options.draw();
		}

		scene.getCamera().updateViewMatrix();
	
	}
}
