package ui.menu.layout;

import ui.menu.GuiElement;

public class GuiFlowLayout implements GuiLayout {

	public static final int VERTICAL = 0, HORIZONTAL = 1;
	private int x, y, w, h, xo, yo;
	private int dx, dy;

	private final int dir;
	private int padding = 10;

	public GuiFlowLayout(int dir) {
		this.dir = dir;
	}

	@Override
	public void addSeparator() {
		if (dir == VERTICAL) {
			dy += 24;
			if (dy - y > h) {
				dy = y;
				dx += w;
			}
		} else {
			dx += 256;
			if (dx - x > w) {
				dx = x;
				dy += h;
			}
		}
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void init(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.dx = x;
		this.dy = y;
		this.xo = x;
		this.yo = y;
	}

	@Override
	public void reset() {
		this.x = xo;
		this.y = yo;
		this.dx = xo;
		this.dy = yo;
	}
	
	@Override
	public void newElement(GuiElement element) {
		element.setPosition(dx, dy);

		if (dir == VERTICAL) {
			dy += element.height + padding;
			if (dy - y > h) {
				dy = y;
				dx += w;
			}
		} else {
			dx += element.width + padding;
			if (dx - x > w) {
				dx = x;
				dy += h;
			}
		}

	}

	public void setPadding(int p) {
		padding = p;
	}

}
