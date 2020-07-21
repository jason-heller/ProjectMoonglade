package map.tile;

import static map.tile.Tile.TILE_SIZE;

import org.joml.Vector3f;

import gl.res.TileModel;
import map.Material;
import util.ModelBuilder;

public class BuildSector {
	private Tile[][][] tiles;
	private int x, y, z;
	
	public static final int SIZE = 8;
	
	public BuildSector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		tiles = new Tile[SIZE][SIZE][SIZE];
	}

	public void removeTile(int tx, int ty, int tz) {
		tiles[tx][ty][tz] = null;
	}

	public void addTile(Tile tile, int tx, int ty, int tz) {
		tiles[tx][ty][tz] = tile;
	}

	public Tile get(int tx, int ty, int tz) {
		return tiles[tx][ty][tz];
	}

	public void draw(ModelBuilder builder, int chunkX, int chunkZ, Vector3f tex) {
		for(int x = 0; x < 8; x++ ) {
			for(int y = 0; y < 8; y++ ) {
				for(int z = 0; z < 8; z++) {
					Tile tile = tiles[x][y][z];
					
					if (tile == null) {
						continue;
					}
					
					float dx = chunkX + (x * TILE_SIZE) + (this.x * SIZE);
					float dy = (y * TILE_SIZE) + (this.y * SIZE);
					float dz = chunkZ + (z * TILE_SIZE) + (this.z * SIZE);

					byte walls = tile.getWalls();
					byte[] flags = tile.getFlags();
					
					byte b = 1;
					for(int f = 0; f < Tile.NUM_MATS; f++ ) {
						if ((walls & b) != 0) {
							byte wallPassFlags = b;
							Material mat = tile.getMaterial(f);
							TileModel model = mat.getTileModel();
							tex = Material.getTexCoordData(tex, tile.materials[f], flags[f]);
							model.pass(dx,dy,dz, builder, tex, wallPassFlags, flags[f], tile.materials[f].isColorable());
						}
						
						b *= 2;
					}
				}
			}
		}
	}

	/*private byte cornerByte(byte b, byte walls) {
		switch(b) {
		case 1: return (byte) (b | (16 & walls));
		case 2: 
			if ((walls & 16) != 0) 
				return 0;
			return (byte) (b | (32 & walls));
		case 16: 
			if ((walls & 1) != 0) 
				return 0;
			return (byte) (b | (2 & walls));
		case 32: return (byte) (b | (1 & walls));
		}
		
		return 0;
	}*/

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
}
