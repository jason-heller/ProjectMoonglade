package ui.menu;

import java.util.ArrayList;
import java.util.List;

import ui.UI;
import ui.menu.layout.GuiLayout;

public class GuiPanel extends GuiElement {
	private final List<GuiElement> elements = new ArrayList<GuiElement>();
	private GuiPanel parent = null;
	private GuiLayout layout = null;

	public GuiPanel() {
		this(null);
	}

	public GuiPanel(GuiPanel parent) {
		this.parent = parent;
		this.setFocus(false);
	}

	public void add(GuiElement element) {
		if (layout != null) {
			layout.newElement(element);
		}
		elements.add(element);
	}

	public void addSeparator() {
		if (layout != null) {
			layout.addSeparator();
		}
	}

	public void addWithoutLayout(GuiElement element) {
		elements.add(element);
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
		for (final GuiElement element : elements) {
			if (element instanceof GuiPanel) {
				final GuiPanel panel = (GuiPanel) element;
				if (panel.isFocused()) {
					panel.draw();
				}
			} else {
				element.draw();
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
	protected void update() {
		// nothing
	}
}
