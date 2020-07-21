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
			
	private static final int[][] tileFaceAdjust = new int[][] {
		new int[] { 0, 0, 1 },	// X != 0
		new int[] { 0, 0, 1 },	// Y != 0
		new int[] { -1, 0, 0 },
		new int[] { -1, 0, 0 },	// Z != 0
		new int[] {0, 0, 0}		// X,Y,Z = 0
	};

	public TilePicker(Terrain terrain) {
		this.terrain = terrain;
		cnd = new ConnectionNodeData();
	}

	// NOTE: snapFlags = wwwwwgsf
	// w = walls, s = slope, f = floor, g = gradual slope
	private static final float RAYCAST_DELTA = 0.05f;

	public Vector3f buildingRaycast(Overworld ow, Vector3f origin, Vector3f dir, float maxDist, byte facing) {
		int facingIndex = Tile.getFacingIndex(facing);
		
		Vector3f point = new Vector3f(origin);
		Vector3f offset = Vector3f.mul(dir, RAYCAST_DELTA);
		
		Vector3f tilePos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		Vector3f normal, result;
		
		for (float i = 0; i <= maxDist; i += RAYCAST_DELTA) {
			int x = (int) (Math.floor(point.x));
			int y = (int) (Math.floor(point.y));
			int z = (int) (Math.floor(point.z));
			if (x != tilePos.x || y != tilePos.y || z != tilePos.z) {
				tilePos.set(x, y, z);

				normal = MathUtil.rayBoxEscapeNormal(point, dir, x, y, z, TILE_SIZE);
				if ((Tile.getFacingByte(normal) & facing) == 0)
					continue;

				result = testTile(tilePos, tileFaceAdjust[4], normal, facingIndex);
				if (result != null)
					return result;

				if (normal.x != 0f) {
					result = testTile(tilePos, tileFaceAdjust[0], normal, facingIndex);
				} else if (normal.y != 0f) {
					result = testTile(tilePos, tileFaceAdjust[1], normal, facingIndex);
					if (result != null)
						return result;
					result = testTile(tilePos, tileFaceAdjust[2], normal, facingIndex);
				} else {
					result = testTile(tilePos, tileFaceAdjust[3], normal, facingIndex);
				}

				if (result != null)
					return result;
			}

			point.add(offset);
		}

		return null;
	}

	private Vector3f testTile(Vector3f tilePos, int[] faceOffset, Vector3f normal, int facingIndex) {
		Tile tile;
		int xOffset = ((normal.x < 0) ? -1 : 0) + faceOffset[0];
		int yOffset = ((normal.y > 0) ?  1 : 0) + faceOffset[1];
		int zOffset = ((normal.z > 0) ?  1 : 0) + faceOffset[2];

		for (int j = 0; j < connections.length; j++) {
			int dx = connections[j][0];
			int dy = connections[j][1];
			int dz = connections[j][2];
			
			int[] placeTileData = cnd.intToNode(cnd.getConnectionData(facingIndex, Material.BRICK));

			tile = getTileAt(tilePos.x + dx + xOffset, tilePos.y + dy + yOffset, tilePos.z + dz + zOffset);

			if (tile != null) {
				int faceIndex = cnd.compare(placeTileData, tile, dx, dy, dz);
				
				if (faceIndex != -1) {
					Console.log(faceOffset[0], faceOffset[1], faceOffset[2]);
					return new Vector3f(tilePos.x + xOffset - faceOffset[0], tilePos.y + yOffset - faceOffset[1], tilePos.z + zOffset - faceOffset[2]);
				}
				
			}
		}
		
		return null;
	}

	public Tile getTileAt(float x, float y, float z) {
		return terrain.getTileAt(x, y, z);
	}
}
