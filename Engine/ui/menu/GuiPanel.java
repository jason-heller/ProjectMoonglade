package ui.menu;

import java.util.ArrayList;
import java.util.List;

import io.Input;
import ui.UI;
import ui.menu.layout.GuiLayout;
import util.Colors;

public class GuiPanel extends GuiElement {
	private final List<GuiElement> elements = new ArrayList<GuiElement>();
	private GuiPanel parent = null;
	private GuiLayout layout = null;

	private int yScroll = Integer.MAX_VALUE, maxScroll = 0;
	private int scrollbarHeight = 48, scrollbarWidth = 12, scrollbarY = 0;
	
	private boolean grabbed = false;

	public GuiPanel() {
		this(null, 0, 0, 0, 0);
	}

	public GuiPanel(GuiPanel parent, int x, int y, int width, int height) {
		this.parent = parent;
		this.setFocus(false);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void add(GuiElement element) {
		if (layout != null) {
			layout.newElement(element);
		}
		elements.add(element);

		if (yScroll != Integer.MAX_VALUE) {
			maxScroll += element.height;
		}
	}

	public void addSeparator() {
		if (layout != null) {
			layout.addSeparator();
		}

	}

	public void addWithoutLayout(GuiElement element) {
		elements.add(element);
		if (yScroll != Integer.MAX_VALUE) {
			maxScroll += element.height;
		}
	}

	public void close() {
		if (parent != null) {
			parent.setFocus(true);
		}
		this.setFocus(false);
	}

	public void collapse() {
		boolean lastPanel = true;

		for (final GuiElement element : elements) {
			if (element instanceof GuiPanel) {
				((GuiPanel) element).collapse();
				lastPanel = false;
			}
		}

		if (lastPanel) {
			this.close();
		}
	}

	@Override
	public void draw() {
		if (maxScroll != 0) {
			int scrollbarX = x + width;
			
			int mx = Input.getMouseX(), my = Input.getMouseY();
			if (Input.isPressed(Input.KEY_LMB) && mx >= scrollbarX - scrollbarWidth && mx < scrollbarX && my > y - 4
					&& my < y-4 + height) {
				grabbed = true;
			}
			
			if (grabbed && !Input.isDown(Input.KEY_LMB)) {
				grabbed = false;
			}
			
			if (grabbed) {
				yScroll = -(int)(((my - (y-4)) / (float)height) * maxScroll);
				yScroll = Math.max(Math.min(yScroll, 0), -maxScroll);
			}
			
			float scrollFactor = -(yScroll / (float)maxScroll);
			scrollbarY = (int) (scrollFactor * (height - scrollbarHeight));
			
			UI.drawRect(scrollbarX - scrollbarWidth, y - 4, scrollbarWidth, height, Colors.DK_GREY);
			UI.drawRect(scrollbarX - scrollbarWidth, y - 4 + scrollbarY, scrollbarWidth, scrollbarHeight, Colors.SILVER).setDepth(-1);
			
			
		}
		
		for (final GuiElement element : elements) {
			if (element instanceof GuiPanel) {
				final GuiPanel panel = (GuiPanel) element;
				if (panel.isFocused()) {
					panel.yScroll = yScroll;
					panel.draw();
				}
			} else {
				if (yScroll != Integer.MAX_VALUE) {
					int relPosY = element.y + yScroll;
					if (relPosY >= y && relPosY + element.height < y + height) {
						element.draw(0, yScroll);
					}
				} else {
					element.draw(0, 0);
				}

			}
		}

		UI.setOpacity(1f);
	}

	public List<GuiElement> getElements() {
		return elements;
	}

	public GuiLayout getLayout() {
		return layout;
	}

	public void open() {
		if (parent != null) {
			parent.setFocus(false);
		}
		this.setFocus(true);
	}

	@Override
	public void setFocus(boolean focus) {
		hasFocus = focus;
		for (final GuiElement element : elements) {
			if (!(element instanceof GuiPanel)) {
				element.setFocus(focus);
			}
		}
	}

	public void setLayout(GuiLayout layout, int x, int y, int w, int h) {
		this.layout = layout;
		layout.init(x, y, w, h);
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		if (yScroll != Integer.MAX_VALUE) {
			int dWheel = Input.getMouseDWheel();
			if (dWheel > 0) {
				yScroll = Math.max(Math.min(yScroll + 34, 0), -maxScroll);
			} else if (dWheel < 0) {
				yScroll = Math.max(Math.min(yScroll - 34, 0), -maxScroll);
			}
		}
	}

	public void setScrollable(boolean scroll) {
		this.yScroll = (scroll) ? 0 : Integer.MAX_VALUE;
	}
}
