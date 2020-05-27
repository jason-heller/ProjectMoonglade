package ui.menu;

import ui.Colors;
import ui.Image;
import ui.Text;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiLayeredPane extends GuiPanel {
	private final Image pane;
	private final Image tabs;
	private final Image backdrop, border, accent;
	private final Text label;

	private GuiPanel[] panels;
	private GuiPanel currentPane = null;

	protected int tabX;
	protected final int tabWidth = 128;
	protected final int tabHeight = 24;

	protected GuiMenu menu;

	public GuiLayeredPane(GuiPanel parent, int x, int y, int width, int height, String label) {
		super(parent);
		this.x = x;
		this.y = y;
		tabX = x + tabWidth;
		this.width = width;
		this.height = height;

		pane = new Image("none", tabX, y).setColor(Colors.GUI_BACKGROUND_COLOR);
		pane.w = width - tabWidth;
		pane.h = height;
		pane.setUvOffset(0, 0, width / pane.getTexture().size, height / pane.getTexture().size);

		backdrop = new Image("none", x, y).setColor(Colors.GUI_BORDER_COLOR);
		backdrop.w = width;
		backdrop.h = height;

		border = new Image("none", x, y - 20).setColor(Colors.GUI_BORDER_COLOR);
		border.w = width;
		border.h = 20;

		accent = new Image("none", x, y - 2).setColor(Colors.GUI_ACCENT_COLOR);
		accent.w = width;
		accent.h = 2;

		tabs = new Image("none", x, y).setColor(Colors.GUI_BACKGROUND_COLOR);
		tabs.w = tabWidth;

		this.label = new Text(label, x + 2, y - 22, .25f, false);
		this.label.setDepth(3);
	}

	@Override
	public void draw() {
		if (currentPane == null) {
			return;
		}
		UI.setOpacity(1f);

		UI.drawImage(border);
		UI.drawImage(backdrop);
		UI.drawImage(accent);
		UI.drawImage(pane);
		UI.drawImage(tabs);
		UI.drawString(label);
		super.draw();

		currentPane.draw();
	}

	protected void setMenu(String... options) {
		menu = new GuiMenu(x + 4, y + 4, options);
		tabs.h = menu.getLineHeight() * options.length;
		tabs.setUvOffset(0, 0, width, options.length);

		menu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (!option.equals("Back")) {
					setPane(index);
				} else {
					close();
				}
			}

		});
		add(menu);
	}

	public void setPane(int index) {
		currentPane = panels[index];
	}

	public void setPanels(GuiPanel... panels) {
		this.panels = panels;
		currentPane = panels[0];
	}
}
