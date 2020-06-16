package map.tile;

import java.util.ArrayList;
import java.util.List;

import core.res.TileableModel;
import io.TileableModelFileLoader;
import map.Material;
import scene.overworld.inventory.Item;

import static map.Material.*;

public class TileData {
	
	private List<EnvTile> tiles;
	private static final String path = "terrain/tile/";
	
	private static final byte WEAK = 1, DEF_STRENGTH = 50, STRONG = 100;
	
	//TODO: make this an enum
	public TileData() {
		tiles = new ArrayList<EnvTile>();
		// Load resources
		tiles.add(null);
		add("bush", STICKS, Item.BUNDLE_OF_STICKS, 1, DEF_STRENGTH, false, Item.AIR);
		add("thin_tree", STICKS, Item.PLANKS, 8, DEF_STRENGTH, true, Item.AXE);
		add("dead_tree", STICKS, Item.PLANKS, 4, DEF_STRENGTH, true, Item.AXE);
		add("rock", STONE_BRICK, Item.COBBLE, 1, STRONG, true, Item.AIR);
		add("reed", THATCH, Item.THATCH, 12, DEF_STRENGTH, false, Item.SPADE);
		add("oak", THATCH, Item.PLANKS, 12, DEF_STRENGTH, false, Item.AXE);
		
		add("agave", THATCH, Item.AIR, 0, DEF_STRENGTH, false, Item.AIR);
		add("cactus", THATCH, Item.AIR, 0, DEF_STRENGTH, false, Item.SPADE);
		
		add("pine", STICKS, Item.PLANKS, 0, DEF_STRENGTH, false, Item.AXE);
		add("mangrove", STICKS, Item.BUNDLE_OF_STICKS, 0, STRONG, false, Item.AXE);
	}
	
	private void add(String res, Material material, Item drop, int dropNum, byte strength, boolean alwaysDrop, Item tool) {
		TileableModel model = TileableModelFileLoader.readModFile(path + res + ".til");
		tiles.add(new EnvTile(model, material, drop, dropNum, strength, alwaysDrop, tool));
	}


	public TileableModel getModel(int id) {
		return tiles.get(id).getModel();
	}

	public EnvTile get(int id) {
		return tiles.get(id);
	}
}
