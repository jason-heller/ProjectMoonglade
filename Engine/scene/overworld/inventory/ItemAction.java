package scene.overworld.inventory;

import dev.Console;
import map.Chunk;
import map.tile.Tile;

public enum ItemAction {
	NONE, PAINT;

	public boolean doAction(int id, int facingIndex, Tile tile, Chunk chunk) {
		switch(this) {
		case PAINT:
			if (tile != null && tile.getMaterial(facingIndex).isColorable()) {
				paint(Item.get(id).getName(), facingIndex, tile);
				chunk.getBuilding().buildModel();
				return true;
			}
			return false;
			
		default:
			return false;
		}
	}

	private void paint(String item, int facingIndex, Tile tile) {
		switch(item) {
		case "red_paint":
			tile.setFlags(facingIndex, (byte) 1);
			break;
		case "orange_paint":
			tile.setFlags(facingIndex, (byte) 2);
			break;
		case "yellow_paint":
			tile.setFlags(facingIndex, (byte) 3);
			break;
		case "green_paint":
			tile.setFlags(facingIndex, (byte) 4);
			break;
		case "cyan_paint":
			tile.setFlags(facingIndex, (byte) 5);
			break;
		case "blue_paint":
			tile.setFlags(facingIndex, (byte) 6);
			break;
		case "indigo_paint":
			tile.setFlags(facingIndex, (byte) 7);
			break;
		case "violet_paint":
			tile.setFlags(facingIndex, (byte) 8);
			break;
		case "dark_grey_paint":
			tile.setFlags(facingIndex, (byte) 9);
			break;
		case "light_grey_paint":
			tile.setFlags(facingIndex, (byte) 10);
			break;
		case "silver_paint":
			tile.setFlags(facingIndex, (byte) 11);
			break;
		case "forest_green_paint":
			tile.setFlags(facingIndex, (byte) 12);
			break;
		default:
			tile.setFlags(facingIndex, (byte) 0);
		}
	}
}
