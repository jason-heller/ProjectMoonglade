package map.tile;

import java.util.ArrayList;
import java.util.List;

import core.res.TileableModel;
import io.TileableModelFileLoader;
import scene.overworld.inventory.Item;

public class TileData {
	
	private List<EnvTile> tiles;
	private static final String path = "terrain/tile/";
	
	public TileData() {
		tiles = new ArrayList<EnvTile>();
		// Load resources
		tiles.add(null);
		add("bush", Item.BUNDLE_OF_STICKS, 1, false);
		add("thin_tree", Item.PLANKS, 8, true);
		add("dead_tree", Item.PLANKS, 4, true);
		add("rock", Item.COBBLE, 1, true);
	}
	
	private void add(String res, Item drop, int dropNum, boolean alwaysDrop) {
		TileableModel model = TileableModelFileLoader.readModFile(path + res + ".til");
		tiles.add(new EnvTile(model, drop, dropNum, alwaysDrop));
	}

	public TileableModel getModel(int id) {
		return tiles.get(id).getModel();
	}

	public EnvTile get(int id) {
		return tiles.get(id);
	}
}
