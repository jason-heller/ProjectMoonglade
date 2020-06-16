package scene.menu.pause;

import dev.Console;
import ui.menu.GuiMenu;
import ui.menu.GuiPanel;
import ui.menu.listener.MenuListener;

public class PausePanel extends GuiPanel {
	private final GuiMenu pauseMenu;
	private final OptionsPanel options;

	public PausePanel(PauseGui gui) {
		super(null, 0, 0);
		pauseMenu = new GuiMenu(100, 300, "Resume", "Options", "Debug", "Back To Menu");
		// pauseMenu.setBordered(true);

		options = new OptionsPanel(this);
		add(options);

		pauseMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (option.equals("Resume")) {
					gui.unpause();
				} else if (option.equals("Debug")) {
					Console.send("debug");
					gui.unpause();
				} else if (option.equals("Options")) {
					options.open();
				} else if (option.equals("Back To Menu")) {
					//Application.changeScene(MainMenuScene.class);
				}
			}

		});

		add(pauseMenu);
	}
}
