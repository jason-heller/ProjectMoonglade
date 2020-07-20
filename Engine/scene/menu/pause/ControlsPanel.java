package scene.menu.pause;

import java.util.Iterator;

import gl.Camera;
import io.Controls;
import ui.menu.GuiButton;
import ui.menu.GuiElement;
import ui.menu.GuiKeybind;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;
import ui.menu.listener.SliderListener;

public class ControlsPanel extends GuiPanel {

	private final GuiSlider sensitivity;
	private final GuiButton reset;

	public ControlsPanel(GuiPanel parent, int x, int y, int width, int height) {
		super(parent, x, y, width, height);

		setScrollable(true);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 9999);//392

		int i = 0;
		Iterator<String> iter = Controls.controls.keySet().iterator();
		add(new GuiLabel(x, y ,"#SGameplay"));
		for (i = 0; i < 6; i++) {
			addBind(iter.next());
		}
		
		addSeparator();
		add(new GuiLabel(x, y ,"#SMovement"));
		for (i = 0; i < 7; i++) {
			addBind(iter.next());
		}
		
		addSeparator();
		add(new GuiLabel(x, y ,"#SInventory"));
		for (i = 0; i < 9; i++) {
			addBind(iter.next());
		}

		addSeparator();
		sensitivity = new GuiSlider(x, y, "Mouse Sensitivity", .05f, 2f, Camera.mouseSensitivity, .05f);
		sensitivity.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Camera.mouseSensitivity = value;
			}

		});
		add(sensitivity);
		this.reset = new GuiButton(x, y, "Reset Binds");
		reset.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Controls.defaults();
				Controls.save();
				for (final GuiElement element : getElements()) {
					if (element instanceof GuiKeybind) {
						((GuiKeybind) element).updateKey();
					}
				}
			}

		});
		add(reset);
	}

	private void addBind(String bind) {
		add(new GuiKeybind(x, y, bind.replaceAll("_", " "), bind));
	}
}
