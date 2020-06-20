package scene.overworld.inventory.tool;

import org.joml.Vector3f;

import io.Input;
import map.Chunk;
import map.Terrain;
import map.TerrainIntersection;
import map.tile.EnvTile;
import scene.overworld.inventory.Item;

public class Trowel {
	public static void interact(Chunk chunkPtr, Terrain terrain, TerrainIntersection terrainIntersection,
			Vector3f selectionPt, int cx, int cz, boolean facingTile, boolean withinRange, boolean lmb, boolean rmb) {
		if (facingTile || chunkPtr == null) return;
		
		if (lmb && withinRange) {
			
			EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
			if (envTile != null && envTile.isDestroyableBy(Item.TROWEL)) {
				return;
			}
			
			int relX = Math.round(selectionPt.x - cx);
			int relZ = Math.round(selectionPt.z - cz);
			if (!chunkPtr.breakEnvTile(relX, relZ)) {
				//chunkPtr.raiseHeight(relX, relZ, -Chunk.DIG_SIZE/2f);
			}
		}
		
		if (rmb && withinRange) {
			int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
			int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
			
			/*if (Input.isDown("sneak")) {
				chunkPtr.setHeight(relX, relZ, (int)selectionPt.y/Chunk.POLYGON_SIZE);
			} else {
				chunkPtr.addHeight(relX, relZ, Chunk.DIG_SIZE);
			}*/
			
			chunkPtr.damageEnvTile(relX, relZ, (byte)5);
		}
	}
}
