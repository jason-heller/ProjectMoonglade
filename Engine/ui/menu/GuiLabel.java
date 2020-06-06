package ui.menu;

import ui.Font;
import ui.UI;

public class GuiLabel extends GuiElement {

	private final String text;

	public GuiLabel(int x, int y, String option) {
		this.x = x;
		this.y = y;
		this.text = option;

		final int longestStrLength = option.length();

		int lineHeight = Font.defaultFont.getHeight() + 20;
		height = lineHeight;
		width = Font.defaultFont.getWidth() * (longestStrLength + 1);
	}

	public void center() {
		x = x - width / 2;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		UI.drawString(text, x, y, false);
	}
}
