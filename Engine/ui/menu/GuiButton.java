package ui.menu;

import io.Input;
import ui.Font;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiButton extends GuiElement {
	private final int lineHeight;
	private boolean selected = false;
	private MenuListener listener = null;

	private final String option;

	public GuiButton(int x, int y, String option) {
		this.x = x;
		this.y = y;
		this.option = option;

		final int longestStrLength = option.length();

		lineHeight = Font.defaultFont.getHeight() + 20;
		height = lineHeight;
		width = Font.defaultFont.getWidth() * (longestStrLength + 1);
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	public void center() {
		x = x - width / 2;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		selected = false;
		if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x + width && Input.getMouseY() > y
				&& Input.getMouseY() < y + lineHeight) {
			selected = true;
			if (Input.isPressed(Input.KEY_LMB) && listener != null) {
				listener.onClick(option, 0);
			}
		}

		if (selected) {
			UI.drawString("#s" + option, x, y, false);
		} else {
			UI.drawString(option, x, y, false);
		}
	}
}
