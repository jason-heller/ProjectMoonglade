package map.prop;

import java.util.ArrayList;
import java.util.List;

import gl.res.PropModel;
import io.StaticEntityFileLoader;
import map.Material;
import scene.overworld.inventory.Item;

import static map.Material.*;

public class PropData {
	
	private List<StaticProp> props;
	private static final String path = "terrain/tile/";
	
	private static final byte WEAK = 1, DEF_STRENGTH = 50, STRONG = 100;
	
	//TODO: make this an enum
	public PropData() {
		props = new ArrayList<StaticProp>();
		// Load resources
		props.add(null);
		add("bush", STICK, Item.STICKS, 1, DEF_STRENGTH, false, Item.AIR);
		add("thin_tree", PLANKS, Item.PLANKS, 8, DEF_STRENGTH, true, Item.AXE);
		add("dead_tree", PLANKS, Item.PLANKS, 4, DEF_STRENGTH, true, Item.AXE);
		add("rock", STONE_BRICK, Item.STONE, 1, STRONG, true, Item.AIR);
		add("reed", THATCH, Item.THATCH, 12, DEF_STRENGTH, false, Item.SPADE);
		add("oak", PLANKS, Item.PLANKS, 12, DEF_STRENGTH, false, Item.AXE);
		
		add("agave", THATCH, Item.AIR, 0, DEF_STRENGTH, false, Item.AIR);
		add("cactus", THATCH, Item.AIR, 0, DEF_STRENGTH, false, Item.SPADE);
		
		add("pine", PLANKS, Item.PLANKS, 0, DEF_STRENGTH, false, Item.AXE);
		add("mangrove", STICK, Item.STICKS, 0, STRONG, false, Item.AXE);
		add("palm", STICK, Item.THATCH, 0, STRONG, false, Item.AXE);
		
		add("vine", THATCH, Item.VINE, 8, WEAK, true, Item.AIR);
	}
	
	private void add(String res, Material material, Item drop, int dropNum, byte strength, boolean alwaysDrop, Item tool) {
		PropModel model = StaticEntityFileLoader.readModFile(path + res + ".sef");
		props.add(new StaticProp(model, material, drop, dropNum, strength, alwaysDrop, tool));
	}


	public PropModel getModel(int id) {
		return props.get(id).getModel();
	}

	public StaticProp get(int id) {
		return props.get(id);
	}
}
