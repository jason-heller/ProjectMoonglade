package map.prop;

import static map.Material.*;

import static gl.terrain.TerrainRender.FLORA_TEX_ATLAS_SIZE;

import java.util.HashMap;
import java.util.Map;

import gl.res.PropModel;
import io.StaticEntityFileLoader;
import map.Material;
import scene.overworld.inventory.Item;

public enum Props {

	PLANE, BUSH, THIN_TREE, DEAD_TREE, ROCK, REED, OAK, AGAVE, CACTUS, PINE, MANGROVE, PALM, VINE, CYPRESS, JOSHUA_TREE,
	CHAPARREL_BUSH, DEAD_GRASS, CHAPARREL_FLOWER, BIG_BUSH, BERRY_BUSH, PURPLE_FLOWERS, GRASS, EVERGREEN, PINE_SNOWY, CYPRESS_BIG, DUCKWEED;

	private static Map<Props, StaticProp> props;
	private static final String path = "terrain/tile/";

	private static final byte WEAK = 1, DEF_STRENGTH = 50, STRONG = 100;

	// TODO: make this an enum
	public static void init() {
		props = new HashMap<Props, StaticProp>();
		// Load resources
		add(PLANE, "plane", STICK, "stick", 1, DEF_STRENGTH, false, true, Item.AIR);
		add(BUSH, "bush", STICK, "stick", 1, DEF_STRENGTH, false, false, Item.AIR);
		add(THIN_TREE, "thin_tree", PLANKS, "planks", 8, DEF_STRENGTH, true, true, Item.AXE);
		add(DEAD_TREE, "dead_tree", PLANKS, "planks", 4, DEF_STRENGTH, true, true, Item.AXE);
		add(ROCK, "rock", STONE_BRICK, "stone", 1, STRONG, true, false, Item.AIR);
		add(REED, "reed", THATCH, "thatch", 6, DEF_STRENGTH, true, false, Item.AIR);
		add(OAK, "oak", PLANKS, "planks", 12, DEF_STRENGTH, false, true, Item.AXE);
		add(AGAVE, "agave", THATCH, "plant_fibers", 3, DEF_STRENGTH, false, false, Item.AIR);
		add(CACTUS, "cactus", THATCH,  "plant_fibers", 10, DEF_STRENGTH, false, true, Item.AXE);
		add(PINE, "pine", PLANKS, "planks", 0, DEF_STRENGTH, false, false, Item.AXE);
		add(PINE_SNOWY, PINE, 0, 4, PLANKS, "planks", 0, DEF_STRENGTH, false, true, Item.AXE, true);
		add(MANGROVE, "mangrove", STICK, "stick", 0, STRONG, false, true, Item.AXE);
		add(PALM, "palm", PALM_PLANKS, "palm_planks", 0, STRONG, false, true, Item.AXE);
		add(VINE, "vine", THATCH, "vine", 8, WEAK, true, false, Item.AIR);
		add(CYPRESS, "cypress", CYPRESS_PLANKS, "cypress_planks", 8, STRONG, false, true, Item.AXE);
		add(CYPRESS_BIG, "cypress2", CYPRESS_PLANKS, "cypress_planks", 8, STRONG, false, true, Item.AXE);
		add(JOSHUA_TREE, "joshuatree", STICK, "plant_fibers", 8, DEF_STRENGTH, false, true, Item.AXE);
		add(BIG_BUSH, "big_bush", STICK, "stick", 1, DEF_STRENGTH, false, false, Item.AIR);
		add(BERRY_BUSH, BUSH, 12, 0, STICK, "stick", 1, DEF_STRENGTH, false, false, Item.AIR, true);

		add(CHAPARREL_BUSH, BUSH, 14, 0, STICK, "stick", 8, DEF_STRENGTH, false, false, Item.AIR, true);
		add(DEAD_GRASS, BUSH, 13, 0, STICK, "stick", 8, DEF_STRENGTH, false, false, Item.AIR, true);
		add(GRASS, BUSH, 11, 1, STICK, "stick", 8, DEF_STRENGTH, false, false, Item.AIR, true);
		add(CHAPARREL_FLOWER, PLANE, 14, 1, STICK, "stick", 8, DEF_STRENGTH, false, false, Item.AIR, true);
		add(PURPLE_FLOWERS, PLANE, 13, 1, STICK, "stick", 8, DEF_STRENGTH, false, false, Item.AIR, true);
		
		add(EVERGREEN, "evergreen", PLANKS, "planks", 12, DEF_STRENGTH, false, true, Item.AXE);
		add(DUCKWEED, PLANE, 7, 12, NONE, "planks", 12, DEF_STRENGTH, false, true, Item.AIR, false);

	}

	private static void add(Props id, String res, Material material, String drop, int dropNum, byte strength,
			boolean alwaysDrop, boolean solid, int tool) {
		PropModel model = StaticEntityFileLoader.readModFile(path + res + ".sef");
		props.put(id, new StaticProp(model, material, Item.getId(drop), dropNum, strength, alwaysDrop, solid, tool, true));
	}

	private static void add(Props id, Props parent, float dtx, float dty, Material material, String drop,
			int dropNum, byte strength, boolean alwaysDrop, boolean solid, int tool, boolean grounded) {
		PropModel model = copyModel(getModel(parent), dtx * FLORA_TEX_ATLAS_SIZE, dty * FLORA_TEX_ATLAS_SIZE);
		props.put(id, new StaticProp(model, material, Item.getId(drop), dropNum, strength, alwaysDrop, solid, tool, grounded));
	}

	private static PropModel copyModel(PropModel model, float dtx, float dty) {
		return model.copyAndShiftTexture(dtx, dty);
	}

	public static PropModel getModel(Props id) {
		return props.get(id).getModel();
	}

	public static StaticProp get(Props id) {
		return props.get(id);
	}
}
