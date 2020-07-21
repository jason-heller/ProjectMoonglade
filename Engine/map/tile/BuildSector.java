package map.tile;

import static map.tile.Tile.TILE_SIZE;

import org.joml.Vector3f;

import dev.Console;
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
					byte slope = tile.getSlope();
					int slantFactor = (tile.getWalls() >> 6);
					
					byte b = 1;
					for(int f = 0; f < 6; f++ ) {
						if ((walls & b) != 0) {
							byte wallPassFlags = b;//(cornerByte(b, walls));
							//if (wallPassFlags == 0) continue;
							
							Material mat = tile.getMaterial(f);
							TileModel model = mat.getTileModel();
							tex = Material.getTexCoordData(tex, tile.materials[f], flags[f]);
							model.pass(dx,dy,dz, builder, tex, wallPassFlags, flags[f], (byte)-1, tile.materials[f].isColorable());
						}
						
						b *= 2;
					}
					
					b = 1;

					if (slantFactor == 0) {	// Gradual
						for(int f = 0; f < 6; f++ ) {
							if ((slope & b) != 0) {
								
								TileModel model = tile.getMaterial(6).getTileModel();
								tex = Material.getTexCoordData(tex, tile.materials[6], flags[6]);
								model.pass(dx,dy,dz, builder, tex, b, flags[6], (byte)slantFactor, tile.materials[6].isColorable());
							}
							
							b *= 2;
						}
					} else if (slantFactor == 1) { // Steep
						for(int f = 0; f < 4; f++ ) {
							if ((slope & b) != 0) {
								TileModel model = tile.getMaterial(6).getTileModel();
								tex = Material.getTexCoordData(tex, tile.materials[6], flags[6]);
								model.pass(dx,dy,dz, builder, tex, b, flags[6], (byte)slantFactor, tile.materials[6].isColorable());
							}
							
							b *= 2;
						}
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
