package scene.overworld.inventory;

import org.joml.Vector3f;

import audio.AudioHandler;
import dev.Debug;
import gl.Camera;
import gl.particle.ParticleHandler;
import io.Input;
import map.Chunk;
import map.TerrainIntersection;
import map.prop.Props;
import map.prop.StaticProp;
import map.tile.Tile;
import scene.entity.PlayerEntity;
import scene.overworld.Overworld;
import scene.overworld.inventory.tool.EditorBoundsTool;

public enum ItemAction {
	NONE, PAINT, DIG, CHOP, EAT;
	
	public boolean doAction(Overworld overworld, TerrainIntersection ti, Tile tile, Chunk chunk, int id, int facingIndex, boolean lmb) {
		Camera camera = overworld.getCamera();
		Vector3f selectionPt = overworld.getSelectionPoint();
		Vector3f exactSelectionPt = overworld.getExactSelectionPoint();
	
		PlayerEntity player = overworld.getPlayer();
		
		if (lmb) {
			switch(this) {
			case DIG:
				return dig(ti, chunk, chunk.realX, chunk.realZ, selectionPt);
				
			case CHOP:
				if (Debug.structureMode) {
					EditorBoundsTool.interact(exactSelectionPt, true, false);
					return true;
				}
				return chop(player, camera, ti, exactSelectionPt, exactSelectionPt, chunk, chunk.realX, chunk.realZ);
				
			default:
				return true;
			}
		} else {
			switch(this) {
			case DIG:
				return makeMound(chunk, chunk.realX, chunk.realZ, selectionPt);
			
			case CHOP:
				if (Debug.structureMode) {
					EditorBoundsTool.interact(exactSelectionPt, false, true);
				}
				return true;
				
			case PAINT:
				if (tile != null && tile.getMaterial(facingIndex).isColorable()) {
					paint(Item.get(id).getName(), facingIndex, tile);
					chunk.getBuilding().buildModel();
					return true;
				}
				return false;
				
			case EAT:
				Inventory inv = overworld.getInventory();
				inv.consume(inv.getSelectionPos());
				overworld.getPlayer().heal(1);
				return true;
				
			default:
				return true;
			}
		}
	}

	private boolean chop(PlayerEntity player, Camera camera, TerrainIntersection ti, Vector3f selectionPt,
			Vector3f exactSelectionPt, Chunk chunk, int cx, int cz) {
		if (ti == null) {
			
			return true;
		}
		
		StaticProp propTile = Props.get(ti.getProp());
		if (propTile == null || !propTile.isDestroyableBy(Item.AXE)) {
			AudioHandler.play("swing");
			return false;
		} else {
			AudioHandler.play("chop_bark");
		}
		
		Vector3f splashDir = new Vector3f(camera.getDirectionVector()).negate().normalize();
		ParticleHandler.addSplash(propTile.getMaterial(), exactSelectionPt, splashDir);
		//ParticleHandler.addBurst(Resources.getTexture("materials"), 0, 0, selectionPt);
		
		chunk.damangeProp(ti.getPropX(), ti.getPropZ(), (byte)15);
		
		return true;
	}

	private boolean makeMound(Chunk chunk, int cx, int cz, Vector3f selectionPt) {
		int relX = (int) (selectionPt.x - cx) / Chunk.POLYGON_SIZE;
		int relZ = (int) (selectionPt.z - cz) / Chunk.POLYGON_SIZE;

		if (Input.isDown("sneak")) {
			chunk.setHeight(relX, relZ, (int) selectionPt.y / Chunk.POLYGON_SIZE);
		} else {
			chunk.addHeight(relX, relZ, Chunk.DIG_SIZE);
		}

		chunk.destroyProp(relX, relZ);
		return true;
	}

	private boolean dig(TerrainIntersection ti, Chunk chunk, int cx, int cz, Vector3f selectionPt) {
		StaticProp prop = Props.get(ti.getProp());
		if (prop != null && prop.isDestroyableBy(Item.SPADE))
			return false;

		int relX = (int) (selectionPt.x - cx) / Chunk.POLYGON_SIZE;
		int relZ = (int) (selectionPt.z - cz) / Chunk.POLYGON_SIZE;
		if (!chunk.destroyProp(relX, relZ)) {
			if (Input.isDown("sneak")) {
				chunk.smoothHeight(relX, relZ);
			} else {
				chunk.addHeight(relX, relZ, -Chunk.DIG_SIZE);
			}
		}
		return true;
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
			byte flags = tile.getMaterial(facingIndex).getInitialFlags();
			tile.setFlags(facingIndex, flags);
		}
	}
}
