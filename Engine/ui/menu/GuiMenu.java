package ui.menu;

import io.Input;
import ui.Font;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiMenu extends GuiElement {
	private final int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;

	private final String[] options;
	private boolean bordered;

	public GuiMenu(int x, int y, String... options) {
		this.x = x;
		this.y = y;
		this.options = options;

		int longestStrLength = 0;
		for (final String option : options) {
			longestStrLength = Math.max(longestStrLength, option.length());
		}

		lineHeight = Font.defaultFont.getHeight() + 20;
		height = lineHeight * options.length;
		width = Font.defaultFont.getWidth() * (longestStrLength + 1);
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public void setBordered(boolean bordered) {
		this.bordered = bordered;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		selectedOption = -1;
		int index = 0;
		for (final String option : options) {

			if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x + width
					&& Input.getMouseY() > y + index * lineHeight
					&& Input.getMouseY() < y + index * lineHeight + lineHeight) {
				selectedOption = index;
				if (Input.isPressed(Input.KEY_LMB) && listener != null) {
					listener.onClick(option, index);
					UI.getSource().play("click");
				}
			}

			if (bordered) {
				UI.drawString("#0" + option, x, y + index * lineHeight - 2);
				UI.drawString("#0" + option, x, y + index * lineHeight + 2);
				UI.drawString("#0" + option, x - 2, y + index * lineHeight);
				UI.drawString("#0" + option, x + 2, y + index * lineHeight);
			}
			if (index == selectedOption) {
				UI.drawString("#s" + option, x, y + index * lineHeight, false);
			} else {
				UI.drawString(option, x, y + index * lineHeight, false);
			}
			index++;
		}
	}
}
