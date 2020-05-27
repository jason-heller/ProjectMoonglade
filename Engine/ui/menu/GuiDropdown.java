package ui.menu;

import io.Input;
import ui.Colors;
import ui.Font;
import ui.Image;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiDropdown extends GuiElement {
	private final int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;
	private boolean isOpen = false;
	private final String label;

	private final Image backdrop, labelBackdrop;

	private final String[] options;

	public GuiDropdown(int x, int y, String label, String... options) {
		this.options = options;
		this.label = label;

		int longestStrLength = label.length();
		for (final String option : options) {
			longestStrLength = Math.max(longestStrLength, option.length());
		}

		lineHeight = Font.defaultFont.getHeight() + 12;
		height = 24;
		width = Font.defaultFont.getWidth() * (longestStrLength + 1);
		width += 8;

		backdrop = new Image("none", x, y + 24).setColor(Colors.GUI_BORDER_COLOR);
		backdrop.w = width;
		backdrop.h = options.length * 24;
		backdrop.setDepth(-2);

		labelBackdrop = new Image("none", x, y).setColor(Colors.GUI_BORDER_COLOR);
		labelBackdrop.w = width;
		labelBackdrop.h = 24;
	}

	public void addListener(MenuListener listener) {
		this.listener = listener;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		backdrop.x = x;
		backdrop.y = y + 24;
		labelBackdrop.x = x;
		labelBackdrop.y = y;
	}

	@Override
	public void update() {
		UI.drawImage(labelBackdrop);

		if (isOpen) {
			selectedOption = -1;
			int index = 0;
			UI.drawImage(backdrop);
			for (final String option : options) {

				if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x + width
						&& Input.getMouseY() > y + (1 + index) * lineHeight
						&& Input.getMouseY() < y + (1 + index) * lineHeight + lineHeight) {
					selectedOption = index;
					if (Input.isPressed(Input.KEY_LMB) && listener != null) {
						listener.onClick(option, index);
					}
				}

				if (index == selectedOption) {
					UI.drawString("#s" + option, x + 4, y + (1 + index) * lineHeight, false).setDepth(-3);
				} else {
					UI.drawString(option, x + 4, y + (1 + index) * lineHeight, false).setDepth(-3);
				}
				index++;
			}
		}
		if (!tempDisable && hasFocus && Input.isPressed(Input.KEY_LMB) && Input.getMouseX() > x
				&& Input.getMouseX() < x + width && Input.getMouseY() > y && Input.getMouseY() < y + lineHeight) {
			isOpen = !isOpen;
		}

		UI.drawString(label, x + 4, y, false).setDepth(-3);
	}
}
