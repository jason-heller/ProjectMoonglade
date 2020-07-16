package map.tile;

import java.util.HashMap;
import java.util.Map;

import dev.Console;
import map.Material;

public class ConnectionNodeData {
	private Map<TileModels, int[]> nodes;
	
	// Left, right, top, bottom, front, back, slopeL, slopeR, slopeF, slopeB
	
	//   +---10---+
	//   9|       /|
	//  / 6     11 |
	// +---8----+  7
	// |  |     |  |
	// |  +--2--|--+
	// 5 1      4 3
	// |/       |/
	// +---0----+
	// -1 = no node
	
	private final int LEFT = nodesToInt(1, 5, 9, 6);
	private final int RIGHT = nodesToInt(3, 4, 11, 7);
	private final int BOTTOM = nodesToInt(8, 9, 10, 11);
	private final int TOP = nodesToInt(0, 1, 2, 3);
	private final int FRONT = nodesToInt(0, 5, 8, 4);
	private final int BACK = nodesToInt(2, 6, 10, 7);
	
	private final int SLOPE = nodesToInt(9, 3, 10, 0);
	//private final int SLOPE_RIGHT = nodesToInt(11, 1, -1, -1);
	//private final int SLOPE_FRONT = nodesToInt(9, 3, 10, 0);
	//private final int SLOPE_BACK = nodesToInt(10, 0, -1, -1);
	
	private final int[] DEFAULT_NODES = new int[] {
		LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK, SLOPE
	};
	
	public ConnectionNodeData() {
		nodes = new HashMap<TileModels, int[]>();
		
		nodes.put(TileModels.DEFAULT, DEFAULT_NODES);
		nodes.put(TileModels.FILLED, DEFAULT_NODES);
	}

	public int getConnectionData(int faceIndex, Material material, boolean isSlope) {
		int id = faceIndex;
		
		if (isSlope) {
			id = 6;
		}
		
		TileModels tileModel = material.getTileModelType();
		
		return nodes.get(tileModel)[id];
	}
	
	public int compare(int[] nodes1, Tile tile, int dx, int dy, int dz) {
		int[] nodes2;
		for(int i = 0; i < Tile.NUM_MATS; i++) {
			Material mat = tile.getMaterial(i);
			
			if (mat != Material.NONE) {
				nodes2 = intToNode(getConnectionData(i, mat, (i == Tile.NUM_MATS-1)));
				
				for(int node1 : nodes1) {
					int n1 = augment(node1, dx, dy, dz);
					for(int node2 : nodes2) {
						int n2 = augment(node2, dx, dy, dz);
						if (n1 == n2 && n1 != -1) {
							return i;
						}
					}
				}
			}
		}
		return -1;
	}
	
	private int augment(int n, int dx, int dy, int dz) {
		if (dx == 0 && dy == 0 && dz == 0) return n;

		switch(n) {
		case 0: if (dz == 1 || dy == -1) return n; break;
		case 1: if (dx == 1 || dy == -1) return n; break;
		case 2: if (dz == -1 || dy == -1) return n; break;
		case 3: if (dx == -1 || dy == -1) return n; break;
		
		case 4: if (dz == -1 || dx == -1) return n; break;
		case 5: if (dz == -1 || dx == 1) return n; break;
		case 6: if (dz == 1 || dx == 1) return n; break;
		case 7: if (dz == 1 || dx == -1) return n; break;
		
		case 8: if (dz == 1 || dy == 1) return n; break;
		case 9: if (dx == 1 || dy == 1) return n; break;
		case 10: if (dz == -1 || dy == 1) return n; break;
		case 11: if (dx == -1 || dy == 1) return n; break;
		}
		
		return -1;
	}

	private int nodesToInt(int n1, int n2, int n3, int n4) {
		return n1 + (n2 << 8) + (n3 << 16) + (n4 << 24);
	}

	public int[] intToNode(int n) {
		//long pos = NoiseUtil.szudzik(z, NoiseUtil.szudzik(x, y));
		return new int[] { (n & 0xff), (n >> 8 & 0xff), (n >> 16 & 0xff), (n >> 24 & 0xff) };
	}
}
