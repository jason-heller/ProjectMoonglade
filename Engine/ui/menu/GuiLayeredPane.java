package ui.menu;

import ui.Colors;
import ui.Image;
import ui.Text;
import ui.UI;
import ui.menu.listener.MenuListener;

public class GuiLayeredPane extends GuiPanel {
	private final Image pane;
	private final Image tabs;
	private final Image border, accent;
	private final Text label;
	
	private float bgOpacity = 0.5f;

	private GuiPanel[] panels;
	private GuiPanel currentPane = null;

	protected int tabX;
	protected final int tabWidth = 192;
	protected final int tabHeight = 32;

	protected GuiMenu menu;

	public GuiLayeredPane(GuiPanel parent, int x, int y, int width, int height, String label) {
		super(parent, x, y);
		this.x = x;
		this.y = y;
		tabX = x;
		this.width = width;
		this.height = height;

		pane = new Image("none", tabX, y).setColor(Colors.GUI_BACKGROUND_COLOR);
		pane.w = width - tabWidth;
		pane.h = height;
		pane.setUvOffset(0, 0, width / pane.getTexture().size, height / pane.getTexture().size);

		
		border = new Image("none", 0, 0).setColor(Colors.GUI_BACKGROUND_COLOR);
		border.w = 1280;
		border.h = 720;
		border.setOpacity(bgOpacity);

		accent = new Image("none", x, y - 2).setColor(Colors.GUI_ACCENT_COLOR);
		accent.w = width - tabWidth;
		accent.h = 2;

		tabs = new Image("none", x, y).setColor(Colors.GUI_BACKGROUND_COLOR);
		tabs.w = tabWidth;

		this.label = new Text(label, x + 2, y - 64, .4f, false);
		this.label.setDepth(3);
	}

	@Override
	public void draw() {
		if (currentPane == null) {
			return;
		}
		UI.setOpacity(1f);

		UI.drawImage(border);
		UI.drawImage(accent);
		UI.drawImage(pane);
		UI.drawImage(tabs);
		UI.drawString(label);
		super.draw();

		currentPane.draw();
	}

	protected void setMenu(String... options) {
		menu = new GuiMenu(x + 4, y + 4 - 32, options);
		menu.setAlignment(GuiComponent.HORIZONTAL);
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
	
	public GuiPanel getPane() {
		return currentPane;
	}

	public void setPanels(GuiPanel... panels) {
		this.panels = panels;
		currentPane = panels[0];
	}
}
