package scene.overworld;

import core.Resources;

public class OverworldResourceManager {
	public static void init() {
		Resources.addSound("walk_grass", "walk_grass.ogg", 3, false);
		Resources.addSound("walk_snow", "walk_snow.ogg", 2, true);
		Resources.addSound("walk_mud", "walk_mud.ogg", 3, false);
		Resources.addSound("walk_dirt", "walk_dirt.ogg", 2, true);
		Resources.addSound("walk_wood", "step_wood.ogg", true);
		Resources.addSound("walk_metal", "step_metal.ogg", true);
		Resources.addSound("walk_asphalt", "step_asphalt.ogg", true);
		Resources.addSound("tree_fall", "tree_fall.ogg", true);
		Resources.addSound("chop_bark", "chop.ogg", 2, false);
		Resources.addSound("swing", "swing.ogg", 2, false);
		Resources.addSound("collect", "collect03.wav", true);
		Resources.addSound("water", "ambient/water_ambient.ogg", true);
		Resources.addSound("hit", "hit.ogg", true);
		
		Resources.addSound("player_hurt", "player/ow.ogg", 3, false);
		Resources.addSound("player_die", "player/die.ogg", true);
	}
	
	public static void cleanUp() {
		Resources.removeSound("walk_grass");
		Resources.removeSound("walk_snow");
		Resources.removeSound("walk_mud");
		Resources.removeSound("walk_dirt");
		Resources.removeSound("walk_wood");
		Resources.removeSound("walk_metal");
		Resources.removeSound("walk_asphalt");
		Resources.removeSound("tree_fall");
		Resources.removeSound("chop_bark");
		Resources.removeSound("swing");
		Resources.removeSound("collect");
		Resources.removeSound("water");
		
		Resources.removeSound("player_hurt");
		Resources.removeSound("player_die");
		Resources.removeSound("hit");
	}
}
