package scene.menu.pause;

import core.Settings;
import ui.menu.GuiLayeredPane;
import ui.menu.GuiPanel;

public class OptionsPanel extends GuiLayeredPane {
	private final GraphicsPanel gfx;
	private final ControlsPanel controls;
	private final SoundPanel sfx;

	public OptionsPanel(GuiPanel parent) {
		super(parent, 200, 240, 890, 400, "Options");

		setMenu("Controls", "Graphics", "Sound", "Back");

		gfx = new GraphicsPanel(this, tabX + 4, y + 4);
		controls = new ControlsPanel(this, tabX + 4, y + 4);
		sfx = new SoundPanel(this, tabX + 4, y + 4);

		setPanels(controls, gfx, sfx);
	}

	@Override
	public void close() {
		super.close();
		Settings.grabData();
	}
}
