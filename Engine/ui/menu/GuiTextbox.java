package ui.menu;

import dev.Console;
import io.Input;
import ui.Font;
import ui.Image;
import ui.UI;
import ui.menu.listener.MenuListener;
import util.Colors;

public class GuiTextbox extends GuiElement {
	private String value;
	private MenuListener listener = null;

	private final String label;
	private boolean edit = false;
	private boolean modified = false;

	private final Image backdrop;
	private final int TEXTBOX_XSHIFT = 180;

	public GuiTextbox(int x, int y, String label, String defaultInput) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.value = defaultInput;

		width = 192 + TEXTBOX_XSHIFT;
		height = 16;

		backdrop = new Image("none", x + TEXTBOX_XSHIFT, y).setColor(Colors.GUI_BORDER_COLOR);
		backdrop.w = width - TEXTBOX_XSHIFT;
		backdrop.h = 24;
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		UI.drawString(label, x, y - 3, false);

		if (!tempDisable && hasFocus && Input.getMouseX() > x + TEXTBOX_XSHIFT && Input.getMouseX() < x + width
				&& Input.getMouseY() > y && Input.getMouseY() < y + height) {
			if (Input.isPressed(Input.KEY_LMB)) {

				edit = !edit;

				if (listener != null && !edit) {
					listener.onClick(value, 0);
				}
			}
		} else if (Input.isPressed(Input.KEY_LMB)) {
			edit = false;
		}

		if (edit) {
			final char[] keysIn = Input.getTypedKey();

			for (final char in : keysIn) {
				if (!modified) {
					modified = true;
					value = "";
				}
				
				if (in != '`') {
					if (in == '\b') {
						if (value.length() > 0) {
							value = value.substring(0, value.length() - 1);
						}
					} else {

						if ((value + in).length() * (Font.defaultFont.getWidth() + 1) < width - TEXTBOX_XSHIFT) {
							value += in;
						}
					}
				}
			}
		}

		UI.drawImage(backdrop);
		UI.drawString(edit ? value + (System.currentTimeMillis() % 750 > 375 ? "|" : "") : "#s" + value,
				x + TEXTBOX_XSHIFT, y, false);
	}
}
