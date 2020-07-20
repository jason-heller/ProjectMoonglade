package scene.menu;

import java.io.File;
import java.io.FilenameFilter;

import core.Application;
import dev.Console;
import scene.overworld.Overworld;
import ui.menu.GuiButton;
import ui.menu.GuiLabel;
import ui.menu.GuiMenu;
import ui.menu.GuiPanel;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;

public class LoadGameMenu extends GuiPanel {
	private GuiButton load;
	private GuiButton delete;
	private GuiButton back;
	
	private GuiMenu worlds;
	
	private String selected = null;

	public LoadGameMenu(GuiPanel parent, int x, int y) {
		super(parent, x, y, 0, 0);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582 / 2, 392);
	}

	public void scan() {
		this.getElements().clear();
		this.getLayout().reset();
		
		File file = new File("saves/");
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		load = new GuiButton(x, y, "load");
		delete = new GuiButton(x, y, "#rdelete");
		
		load.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (selected != null) {
					Overworld.worldName = selected;
					Overworld.worldFileName = selected.replaceAll("\\W+", "");
					Application.changeScene(Overworld.class);
				}
			}

		});
		
		worlds = new GuiMenu(x, y, directories);
		worlds.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (option.contains("<") || option.contains("#S")) {
					selected = null;
				} else {
					selected = option;
				}
				for(int i = 0; i < worlds.getOptions().length; i++) {
					if (worlds.getOptions()[i].equals(selected)) {
						worlds.setOption(i, ">"+selected+"<");
					} else {
						worlds.setOption(i, worlds.getOptions()[i].replaceFirst("<", "").replaceFirst(">", ""));
					}
				}
			}
			
		});
		
		if (directories.length == 0) {
			add(new GuiLabel(x,y, "No Saves Found :("));
		} else {
			add(worlds);
		}
		
		delete.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (selected != null) {
					File path = new File("saves\\"+selected.replaceAll("\\W+", ""));
					
					File folder = path;
					File[] listOfFiles = folder.listFiles();

					for (int i = 0; i < listOfFiles.length; i++) {
						//if (listOfFiles[i].isFile()) {
							listOfFiles[i].delete();
						//} else if (listOfFiles[i].isDirectory()) {
						// recursion
						//}
					}
					
					Console.log("Tried to delete"+path.toString()+", success="+path.delete());
					for(int i = 0; i < worlds.getOptions().length; i++) {
						if (worlds.getOptions()[i].replaceFirst("<", "").replaceFirst(">", "").equals(selected)) {
							worlds.setOption(i, "#S"+selected);
							break;
						}
					}
					
					selected = null;
				}
			}

		});
		
		back = new GuiButton(x, y, "back");
		addSeparator();
		add(load);
		addSeparator();
		add(delete);
		add(back);
		
		back.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				selected = null;
				setFocus(false);
			}

		});
	}
	
	@Override
	public void update() {
		super.update();
		/*if (selected == null) {
			load.setFocus(false);
		} else {
			load.setFocus(true);
		}*/
	}
}
