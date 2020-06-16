package scene.menu;

import core.Resources;
import core.res.Texture;
import dev.Console;
import scene.Scene;
import scene.menu.pause.OptionsPanel;
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
	
	private final NewGameMenu newGame;
	private final LoadGameMenu loadGame;

	private final Texture mainMenuBg;
	
	private final Scene scene;
	
	public MainMenuUI(Scene scene) {
		this.scene = scene;
		mainMenuBg = Resources.addTexture("main_menu_bg", "gui/menu"+(1+(int)(Math.random()*2.0))+".png");

		mainMenu = new GuiMenu(50, 300, "new game", "load game", "options", "quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);
		
		newGame = new NewGameMenu(null, 300, 300);
		loadGame = new LoadGameMenu(null, 300, 300);

		title = new Text("Title", 50, 125, .75f, false);
		background = new Image(mainMenuBg, 0, 0, (int) UI.width, (int) UI.height);

		UI.addComponent(background);
		UI.addComponent(title);
		
		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				
				switch (index) {
				case 0:
					newGame.setFocus(true);
					loadGame.setFocus(false);
					options.setFocus(false);
					break;
				case 1:
					loadGame.scan();
					loadGame.setFocus(true);
					newGame.setFocus(false);
					options.setFocus(false);
					break;
				case 2:
					options.setFocus(!options.isFocused());
					loadGame.setFocus(false);
					newGame.setFocus(false);
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

		if (newGame.isFocused()) {
			newGame.update();
			newGame.draw();
		}
		
		else if (loadGame.isFocused()) {
			loadGame.update();
			loadGame.draw();
		}
		
		if (options.isFocused()) {
			options.update();
			options.draw();
		} else {
			mainMenu.draw();
		}

		scene.getCamera().updateViewMatrix();
	
	}
}
