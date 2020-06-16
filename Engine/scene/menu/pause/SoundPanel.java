package scene.menu.pause;

import audio.AudioHandler;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.SliderListener;

public class SoundPanel extends GuiPanel {
	private final GuiSlider volume;

	public SoundPanel(GuiPanel parent, int x, int y) {
		super(parent, x, y);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);

		volume = new GuiSlider(x, y, "Volume", 0f, 1f, AudioHandler.volume, .01f);
		volume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				AudioHandler.volume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				AudioHandler.volume = value;
				AudioHandler.changeMasterVolume();
			}

		});
		add(volume);
	}
}
