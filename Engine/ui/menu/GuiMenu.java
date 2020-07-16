package ui.menu;

import audio.AudioHandler;
import io.Input;
import ui.Font;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiMenu extends GuiElement {
	private final int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;
	private boolean centered = false;
	
	private int alignment = GuiComponent.VERTICAL;

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
		width = Font.defaultFont.getWidth() * (longestStrLength + 2);
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
		int dx = 0;
		int dy = 0;
		for (final String option : options) {
			if (alignment == GuiComponent.VERTICAL) {
				dy = index * lineHeight;
			} else {
				dx = index * width;
			}

			if (!tempDisable && hasFocus && Input.getMouseX() > x + dx && Input.getMouseX() < x + dx + width
					&& Input.getMouseY() > y + dy
					&& Input.getMouseY() < y + dy + lineHeight) {
				selectedOption = index;
				if (Input.isPressed(Input.KEY_LMB) && listener != null) {
					listener.onClick(option, index);
					AudioHandler.play("click");
				}
			}

			if (bordered) {
				UI.drawString("#0" + option, x + dx, y + dy - 2);
				UI.drawString("#0" + option, x + dx, y + dy + 2);
				UI.drawString("#0" + option, x + dx - 2, y + dy);
				UI.drawString("#0" + option, x + dx + 2, y + dy);
			}
			
			if (index == selectedOption) {
				UI.drawString("#s" + option, x + dx, y + dy, centered);
			} else {
				UI.drawString(option, x + dx, y + dy, centered);
			}
			index++;
		}
	}
	
	public void setOption(int index, String option) {
		options[index] = option;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	public String[] getOptions() {
		return options;
	}
}
