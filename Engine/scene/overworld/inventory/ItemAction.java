package scene.overworld.inventory;

import dev.Console;
import map.Chunk;
import map.tile.BuildingTile;

public enum ItemAction {
	NONE, PAINT;

	public boolean doAction(Item item, int facingIndex, BuildingTile tile, Chunk chunk) {
		switch(this) {
		case PAINT:
			if (tile != null && tile.getMaterial(facingIndex).isColorable()) {
				paint(item, facingIndex, tile);
				chunk.getBuilding().buildModel();
				return true;
			}
			return false;
			
		default:
			return false;
		}
	}

	private void paint(Item item, int facingIndex, BuildingTile tile) {
		switch(item) {
		case RED_PAINT:
			tile.setFlags((byte) 1);
			break;
		case ORANGE_PAINT:
			tile.setFlags((byte) 2);
			break;
		case YELLOW_PAINT:
			tile.setFlags((byte) 3);
			break;
		case GREEN_PAINT:
			tile.setFlags((byte) 4);
			break;
		case CYAN_PAINT:
			tile.setFlags((byte) 5);
			break;
		case BLUE_PAINT:
			tile.setFlags((byte) 6);
			break;
		case INDIGO_PAINT:
			tile.setFlags((byte) 7);
			break;
		case VIOLET_PAINT:
			tile.setFlags((byte) 8);
			break;
		case DARK_GREY_PAINT:
			tile.setFlags((byte) 9);
			break;
		case LIGHT_GREY_PAINT:
			tile.setFlags((byte) 10);
			break;
		case SILVER_PAINT:
			tile.setFlags((byte) 11);
			break;
		case FOREST_GREEN_PAINT:
			tile.setFlags((byte) 12);
			break;
		default:
			tile.setFlags((byte) 0);
		}
	}
}
