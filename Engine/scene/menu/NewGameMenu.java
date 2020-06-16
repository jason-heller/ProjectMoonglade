package scene.menu;

import java.io.File;

import core.Application;
import scene.overworld.Overworld;
import ui.menu.GuiButton;
import ui.menu.GuiPanel;
import ui.menu.GuiTextbox;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;

public class NewGameMenu extends GuiPanel {
	private final GuiTextbox worldName;
	private final GuiTextbox worldSeed;
	private final GuiButton create, back;

	public NewGameMenu(GuiPanel parent, int x, int y) {
		super(parent, x, y);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582 / 2, 392);

		worldName = new GuiTextbox(x,y, "World Name", "New World");
		worldSeed = new GuiTextbox(x,y+26, "World Seed", ""+System.currentTimeMillis());
		add(worldName);
		add(worldSeed);
		
		create = new GuiButton(x, y, "Create");
		add(create);
		
		create.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				String append = "";
				File path = new File("saves\\"+worldName.getValue().replaceAll("\\W+", ""));
				while (path.exists()) {
					if (append.equals("")) {
						append = "2";
					} else {
						append = Integer.toString(Integer.parseInt(append) + 1);
					}
					
					path = new File("saves\\"+worldName.getValue().replaceAll("\\W+", "") + append);
				}
				
				Overworld.worldName = worldName.getValue() + append;
				Overworld.worldFileName = Overworld.worldName.replaceAll("\\W+", "");
				Overworld.worldSeed = worldSeed.getValue();
				new File("saves/"+Overworld.worldFileName).mkdir();
				Application.changeScene(Overworld.class);
			}

		});
		
		back = new GuiButton(x, y, "Back");
		add(back);
		
		back.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				setFocus(false);
			}

		});

	}
}
