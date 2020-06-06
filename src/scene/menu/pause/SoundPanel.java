package scene.menu.pause;

import audio.AudioHandler;
import core.Globals;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.SliderListener;

public class SoundPanel extends GuiPanel {
	private final GuiSlider volume;

	public SoundPanel(GuiPanel parent, int x, int y) {
		super(parent, x, y);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);

		volume = new GuiSlider(x, y, "Volume", 0f, 1f, Globals.volume, .01f);
		volume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				Globals.volume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				Globals.volume = value;
				AudioHandler.changeMasterVolume();
			}

		});
		add(volume);
	}
}
