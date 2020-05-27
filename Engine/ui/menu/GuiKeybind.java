
package ui.menu;

import io.Controls;
import io.Input;
import ui.Colors;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiKeybind extends GuiElement {
	private String value;
	private final float textWidth = 192;
	private MenuListener listener = null;

	private final String label, bind;

	private int key = -1;
	private boolean edit = false;

	public GuiKeybind(int x, int y, String label, String bind) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.key = Controls.get(bind);
		this.value = Input.getKeyName(key);
		this.bind = bind;

		width = 192;
		height = 24;
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		UI.drawString(label, x, y - 6, false);

		if (edit) {
			UI.drawString("---", x + 16 + width, y, false);
		} else {
			UI.drawString(value.toLowerCase(), x + 16 + width, y, false);
		}

		UI.drawRect(x + width, y, x + (int) textWidth, 24, edit ? Colors.GUI_ACCENT_COLOR : Colors.GUI_BORDER_COLOR);

		if (!tempDisable && hasFocus && Input.getMouseX() > x + width
				&& Input.getMouseX() < x + x + width + textWidth + 32 && Input.getMouseY() > y
				&& Input.getMouseY() < y + height) {
			if (!edit && Input.isPressed(Input.KEY_LMB)) {

				edit = true;
				if (listener != null) {
					listener.onClick(value, 0);
				}

				// if (listener != null && !edit) listener.onClick(value, 0);
				return;
			}
		}

		if (edit) {
			final int input = Input.getAny();
			if (input != -1) {
				key = input;
				edit = false;
				Controls.set(bind, key);
				value = Input.getKeyName(key);
			}
		}

	}

	public void updateKey() {
		key = Controls.get(bind);
		value = Input.getKeyName(key);
	}
}
