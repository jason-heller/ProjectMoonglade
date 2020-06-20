package scene.overworld.inventory.tool;

import org.joml.Vector3f;

import core.Resources;
import gl.Camera;
import gl.particle.ParticleHandler;
import map.Chunk;
import map.Terrain;
import map.TerrainIntersection;
import map.tile.EnvTile;
import scene.entity.PlayerEntity;
import scene.overworld.inventory.Item;

public class Axe {
	public static void interact(Chunk chunkPtr, Terrain terrain, TerrainIntersection terrainIntersection,
			PlayerEntity player, Camera camera, Vector3f selectionPt, Vector3f exactSelectionPt, int cx, int cz, boolean facingTile, boolean withinRange, boolean lmb, boolean rmb) {
		if (facingTile || chunkPtr == null) {
			player.getSource().play(Resources.getSound("swing"));
			return;
		}
		
		if (lmb) {
			EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
			if (envTile == null || !envTile.isDestroyableBy(Item.AXE)) {
				player.getSource().play(Resources.getSound("swing"));
				return;
			} else {
				player.getSource().play(Resources.getSound("chop_bark"));
			}
			
			Vector3f splashDir = new Vector3f(camera.getDirectionVector()).negate().normalize();
			ParticleHandler.addSplash(envTile.getMaterial(), exactSelectionPt, splashDir);
			//ParticleHandler.addBurst(Resources.getTexture("materials"), 0, 0, selectionPt);
			
			int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
			int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
			chunkPtr.damageEnvTile(relX, relZ, (byte)15);
		}
	}
}
