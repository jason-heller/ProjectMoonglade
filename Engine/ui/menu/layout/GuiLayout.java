package ui.menu.layout;

import ui.menu.GuiElement;

public interface GuiLayout {

	public void addSeparator();

	public int getHeight();

	public int getWidth();

	public int getX();

	public int getY();

	public void init(int x, int y, int w, int h);

	public void newElement(GuiElement element);
}
