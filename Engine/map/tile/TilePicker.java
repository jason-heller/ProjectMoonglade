package map.tile;

import static map.tile.Tile.TILE_SIZE;

import org.joml.Vector3f;

import dev.Console;
import map.Material;
import map.Terrain;
import scene.overworld.Overworld;
import util.MathUtil;

public class TilePicker {

	private ConnectionNodeData cnd;
	private Terrain terrain;

	private static final int[][] connections = new int[][] { new int[] { 0, 0, 0 }, new int[] { -1, 0, 0 },
			new int[] { 1, 0, 0 }, new int[] { 0, 0, -1 }, new int[] { 0, 0, 1 }, new int[] { 0, -1, 0 },
			new int[] { 0, 1, 0 } };

	public TilePicker(Terrain terrain) {
		this.terrain = terrain;
		cnd = new ConnectionNodeData();
	}

	// NOTE: snapFlags = wwwwwgsf
	// w = walls, s = slope, f = floor, g = gradual slope
	private static final float RAYCAST_DELTA = 0.05f;

	public Vector3f buildingRaycast(Overworld ow, Vector3f origin, Vector3f dir, float maxDist, byte facing, boolean sloped) {
		Tile tile;

		int facingIndex = Tile.getFacingIndex(facing);
		
		Vector3f point = new Vector3f(origin);
		Vector3f offset = Vector3f.mul(dir, RAYCAST_DELTA);

		int tx = Integer.MIN_VALUE;
		int ty = Integer.MIN_VALUE;
		int tz = Integer.MIN_VALUE;

		for (float i = 0; i <= maxDist; i += RAYCAST_DELTA) {
			int x = (int) (Math.floor(point.x));
			int y = (int) (Math.floor(point.y));
			int z = (int) (Math.floor(point.z));
			if (x != tx || y != ty || z != tz) {
				tx = x;
				ty = y;
				tz = z;
				
				Vector3f normal = MathUtil.rayBoxEscapeNormal(point, dir, x, y, z, TILE_SIZE);
				if ((Tile.getFacingByte(normal) & facing) == 0) continue;
				
				for (int j = 0; j < connections.length; j++) {
					int dx = connections[j][0];
					int dy = connections[j][1];
					int dz = connections[j][2];
					
					if (sloped) {
						if (dy != 0) continue;
						
						if ((facing & 1) != 0 && dx != 0)
							dy -= dx;
						if ((facing & 2) != 0 && dx != 0)
							dy += dx;
						if ((facing & 16) != 0 && dz != 0)
							dy -= dz;
						if ((facing & 32) != 0 && dz != 0)
							dy += dz;
					}
					
					int[] placeTileData = cnd.intToNode(cnd.getConnectionData(facingIndex, Material.BRICK, sloped));

					tile = getTileAt(tx + dx, ty + dy, tz + dz);

					if (tile != null) {
						int faceIndex = cnd.compare(placeTileData, tile, dx, dy, dz);
						
						if (faceIndex != -1 && isLegalTilePlacement(Tile.getFacingIndex(facing), faceIndex, dx, dy, dz)) {
							return new Vector3f(tx, ty, tz);
						}
						
					}
				}
			}

			point.add(offset);
		}

		return null;
	}

	private boolean isLegalTilePlacement(int placeFacing, int tileFacing, int dx, int dy, int dz) {
		if (placeFacing == tileFacing) {
			switch(placeFacing) {
			case 0:
			case 1:
				if (dx != 0) return false;
				break;
			case 2:
			case 3:
				if (dy != 0) return false;
				break;
			case 4:
			case 5:
				if (dz != 0) return false;
				break;
			}
		}
		
		return true;
	}

	public Tile getTileAt(float x, float y, float z) {
		return terrain.getTileAt(x, y, z);
	}
}
