package map.tile;

import java.util.HashMap;
import java.util.Map;

import map.Material;

public class ConnectionNodeData {
	private Map<TileModels, int[]> nodes;
	
	// Left, right, top, bottom, front, back, slopeL, slopeR, slopeF, slopeB
	
	//    6--------7
	//   /|       /|
	//  / |      / |
	// 4--------5  |
	// |  |     |  |
	// |  2-----|--3
	// | /      | /
	// |/       |/
	// 0--------1
	// -1 = no node
	
	/*private final int LEFT = nodesToInt(1, 5, 9, 6);
	private final int BOTTOM = nodesToInt(0, 1, 2, 3);
	private final int FRONT = nodesToInt(0, 5, 8, 4);*/
	
	private final int node0 = (int) pair3(0,0,0);
	private final int node1 = (int) pair3(1,0,0);
	private final int node2 = (int) pair3(0,0,1);
	private final int node3 = (int) pair3(1,0,1);
	
	private final int node4 = (int) pair3(0,1,0);
	private final int node5 = (int) pair3(1,1,0);
	private final int node6 = (int) pair3(0,1,1);
	private final int node7 = (int) pair3(1,1,1);
	
	private final int LEFT = nodesToInt(node0, node1, node4, node5);
	private final int BOTTOM = nodesToInt(node4, node5, node6, node7);
	private final int FRONT = nodesToInt(node1, node5, node3, node7);

	
	private final int SLOPE_LEFT = nodesToInt(node0, node2, node5, node7);
	private final int SLOPE_RIGHT = nodesToInt(node1, node3, node4, node6);
	private final int SLOPE_TOP = nodesToInt(node0, node1, node6, node7);
	private final int SLOPE_BOTTOM = nodesToInt(node2, node3, node4, node5);
	//private final int SLOPE_RIGHT = nodesToInt(11, 1, -1, -1);
	//private final int SLOPE_FRONT = nodesToInt(9, 3, 10, 0);
	//private final int SLOPE_BACK = nodesToInt(10, 0, -1, -1);
	
	private final int[] DEFAULT_NODES = new int[] {
		LEFT, FRONT, BOTTOM, SLOPE_LEFT, SLOPE_RIGHT, SLOPE_TOP, SLOPE_BOTTOM
	};
	
	public ConnectionNodeData() {
		nodes = new HashMap<TileModels, int[]>();
		
		nodes.put(TileModels.DEFAULT, DEFAULT_NODES);
		nodes.put(TileModels.FILLED, DEFAULT_NODES);
	}

	public int getConnectionData(int faceIndex, Material material) {
		int id = faceIndex;
		
		TileModels tileModel = material.getTileModelType();
		
		return nodes.get(tileModel)[id];
	}
	
	public int compare(int[] nodes1, Tile tile, int dx, int dy, int dz) {
		int[] nodes2;
		for(int i = 0; i < Tile.NUM_MATS; i++) {
			Material mat = tile.getMaterial(i);
			
			if (mat != Material.NONE) {
				nodes2 = intToNode(getConnectionData(i, mat));
				
				for(int node1 : nodes1) {
					
					for(int node2 : nodes2) {
						
						if (augment(node1,dx,dy,dz) == node2 && node1 != 255) {
							return i;
						}
					}
				}
			}
		}
		return -1;
	}

	private long augment(int node1, int dx, int dy, int dz) {
		int nx = (node1 & 3);
		int ny = (node1 & 12) >> 2;
		int nz = (node1 & 48) >> 4;
		return pair3(dx+nx, dy+ny, dz+nz);
	}

	private long pair3(int x, int y, int z) {
		x = (x < 0) ? 3: x;
		y = (y < 0) ? 3: y;
		z = (z < 0) ? 3: z;
		return (x & 3) | ((y << 2) & 12) | ((z << 4) & 48);
	}

	private int nodesToInt(int n1, int n2, int n3, int n4) {
		return n1 + (n2 << 8) + (n3 << 16) + (n4 << 24);
	}

	public int[] intToNode(int n) {
		//long pos = NoiseUtil.szudzik(z, NoiseUtil.szudzik(x, y));
		return new int[] { (n & 0xff), (n >> 8 & 0xff), (n >> 16 & 0xff), (n >> 24 & 0xff) };
	}
}
