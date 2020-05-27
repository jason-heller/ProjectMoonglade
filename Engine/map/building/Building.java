package map.building;

import static map.Chunk.CHUNK_SIZE;
import static map.Chunk.VERTEX_COUNT;
import static map.building.Tile.BACK;
import static map.building.Tile.BOTTOM;
import static map.building.Tile.FRONT;
import static map.building.Tile.LEFT;
import static map.building.Tile.RIGHT;
import static map.building.Tile.TILE_SIZE;
import static map.building.Tile.TOP;

import org.joml.Vector3f;

import core.res.Model;
import dev.Console;
import map.Chunk;
import util.ModelBuilder;

public class Building {
	private Tile[][][] tiles;
	private Chunk chunk;
	private Model model;
	
	public final static int CHUNK_HEIGHT = 64;
	
	public Building(Chunk chunk) {
		this.chunk = chunk;
		tiles = new Tile[VERTEX_COUNT*2][CHUNK_HEIGHT][VERTEX_COUNT*2];
		/*
		// TEMP
		for (int x = 0; x < VERTEX_COUNT; x++) {
		    for (int y = 0; y < CHUNK_HEIGHT; y++) {
		        for (int z = 0; z < VERTEX_COUNT; z++) {
		        	tiles[x][y][z] = new Tile(chunk, 0, (byte)0);
		        }
		    }
		}*/
	}
	
	private void modifyArr(int x, int y, int z, byte wall, int item) {
		if (tiles[x][y+(CHUNK_HEIGHT/2)][z] == null) {
			tiles[x][y+(CHUNK_HEIGHT/2)][z] = new Tile(chunk, item, wall);
			Console.log("WINGO",x,y+(CHUNK_HEIGHT/2),z);
		} else {
			tiles[x][y+(CHUNK_HEIGHT/2)][z].append(wall, item);
		}
	}
	
	public void add(int x, int y, int z, byte wall, int item) {
		modifyArr(x, y, z, wall, item);
		
		buildModel();
	}
	
	public void add2x2(int x, int y, int z, byte wall, int item) {
		
		x = Math.floorDiv(x, 2) * 2;
		y = Math.floorDiv(y, 2) * 2;
		z = Math.floorDiv(z, 2) * 2;
		
		if ((wall & LEFT) != 0) {
			modifyArr(x, y, z, wall, item);
			modifyArr(x, y, z+1, wall, item);
			modifyArr(x, y+1, z, wall, item);
			modifyArr(x, y+1, z+1, wall, item);
		}
		
		if ((wall & RIGHT) != 0) {
			modifyArr(x+1, y, z, wall, item);
			modifyArr(x+1, y+1, z, wall, item);
			modifyArr(x+1, y, z+1, wall, item);
			modifyArr(x+1, y+1, z+1, wall, item);
		}
		
		if ((wall & FRONT) != 0) {
			modifyArr(x, y, z, wall, item);
			modifyArr(x+1, y, z, wall, item);
			modifyArr(x, y+1, z, wall, item);
			modifyArr(x+1, y+1, z, wall, item);
		}
		
		if ((wall & BACK) != 0) {
			modifyArr(x, y, z+1, wall, item);
			modifyArr(x+1, y, z+1, wall, item);
			modifyArr(x, y+1, z+1, wall, item);
			modifyArr(x+1, y+1, z+1, wall, item);
		}
		
		if ((wall & TOP) != 0) {
			modifyArr(x, y, z, wall, item);
			modifyArr(x+1, y, z, wall, item);
			modifyArr(x, y, z+1, wall, item);
			modifyArr(x+1, y, z+1, wall, item);
		}
		
		if ((wall & BOTTOM) != 0) {
			modifyArr(x, y+1, z, wall, item);
			modifyArr(x+1, y+1, z, wall, item);
			modifyArr(x, y+1, z+1, wall, item);
			modifyArr(x+1, y+1, z+1, wall, item);
		}
		
		buildModel();
	}
	
	public void remove(int x, int y, int z, byte wall, int item) {

		Console.log(x,y+(CHUNK_HEIGHT/2),z,wall,"AAA2222");
		if (tiles[x][y+(CHUNK_HEIGHT/2)][z] != null) {
			tiles[x][y+(CHUNK_HEIGHT/2)][z].remove(wall, item);
			buildModel();
		}
	}

	private Model buildModel() {
		
		if (model != null) {
			model.cleanUp();
		}
		
		ModelBuilder builder = new ModelBuilder();
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f();
		float s = TILE_SIZE;
		
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < CHUNK_HEIGHT; y++) {
				for (int z = 0; z < tiles.length; z++) {
					if (tiles[x][y][z] == null) {
						continue;
					}
					
					float dx = (chunk.x * CHUNK_SIZE) + (x * TILE_SIZE);
					float dy = ((y - CHUNK_HEIGHT / 2) * TILE_SIZE);
					float dz = (chunk.z * CHUNK_SIZE) + (z * TILE_SIZE);

					boolean lXNegative = true;
					if (x > 0 && tiles[x - 1][y][z] != null)
						lXNegative = tiles[x - 1][y][z].isActive(LEFT);
					boolean lXPositive = true;
					if (x < VERTEX_COUNT - 1 && tiles[x + 1][y][z] != null)
						lXPositive = tiles[x + 1][y][z].isActive(RIGHT);
					boolean lYNegative = true;
					if (y > 0 && tiles[x][y - 1][z] != null)
						lYNegative = tiles[x][y - 1][z].isActive(TOP);
					boolean lYPositive = true;
					if (y < CHUNK_HEIGHT - 1 && tiles[x][y + 1][z] != null)
						lYPositive = tiles[x][y + 1][z].isActive(BOTTOM);
					boolean lZNegative = true;
					if (z > 0 && tiles[x][y][z - 1] != null)
						lZNegative = tiles[x][y][z - 1].isActive(FRONT);
					boolean lZPositive = true;
					if (z < VERTEX_COUNT - 1 && tiles[x][y][z + 1] != null)
						lZPositive = tiles[x][y][z + 1].isActive(BACK);

					byte walls = tiles[x][y][z].getWalls();

					if ((walls & LEFT) != 0) {
						p1.set(dx, dy + s, dz);
						p2.set(dx, dy + s, dz + s);
						p3.set(dx, dy, dz + s);
						p4.set(dx, dy, dz);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lXNegative)
							builder.addQuad(p4, p3, p2, p1);
					}

					if ((walls & RIGHT) != 0) {
						p1.set(dx + s, dy + s, dz + s);
						p2.set(dx + s, dy + s, dz);
						p3.set(dx + s, dy, dz);
						p4.set(dx + s, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lXPositive)
							builder.addQuad(p4, p3, p2, p1);
					}

					if ((walls & TOP) != 0) {
						p1.set(dx + s, dy + s, dz + s);
						p2.set(dx, dy + s, dz + s);
						p3.set(dx, dy + s, dz);
						p4.set(dx + s, dy + s, dz);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lYNegative)
							builder.addQuad(p4, p3, p2, p1);
					}

					if ((walls & BOTTOM) != 0) {
						p1.set(dx, dy, dz + s);
						p2.set(dx + s, dy, dz + s);
						p3.set(dx + s, dy, dz);
						p4.set(dx, dy, dz);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lYPositive)
							builder.addQuad(p4, p3, p2, p1);
					}

					if ((walls & FRONT) != 0) {
						p1.set(dx + s, dy + s, dz);
						p2.set(dx, dy + s, dz);
						p3.set(dx, dy, dz);
						p4.set(dx + s, dy, dz);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lZNegative)
							builder.addQuad(p4, p3, p2, p1);
					}

					if ((walls & BACK) != 0) {
						p1.set(dx, dy + s, dz + s);
						p2.set(dx + s, dy + s, dz + s);
						p3.set(dx + s, dy, dz + s);
						p4.set(dx, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4);
						
						if (lZPositive)
							builder.addQuad(p4, p3, p2, p1);
					}
				}
			}
		}
		
		model = builder.finish();
		return model;
	}
	/*
	private void readTiles(int x, int y, int z) {
		int len = tiles.length;
		int adjX = (int)Math.floor(x / (float)len);
		int adjY = (int)Math.floor(y / (float)len);
		int adjZ = (int)Math.floor(z / (float)len);
		x = (x + len) % len;
		y = (y + len) % len;
		z = (z + len) % len;

		
		if (adjX == 0 && adjZ == 0) {
			heightmap[x][z] = val;
			if (x == heightmap.length-2) {
				adjacent[1].heightmap[0][z] = heightmap[heightmap.length-2][z];
			}
			if (z == heightmap.length-2) {
				adjacent[3].heightmap[x][0] = heightmap[x][heightmap.length-2];
			}
			if (x == 1) {
				adjacent[0].heightmap[heightmap.length-1][z] = heightmap[1][z];
			}
			if (z == 1) {
				adjacent[2].heightmap[x][heightmap.length-1] = heightmap[x][1];
			}
		} else {
			if (adjX == -1) {
				adjacent[0].heightmap[x][z] = val;
			} else if (adjX == 1) {
				adjacent[1].heightmap[x][z] = val;
			} else if (adjZ == -1) {
				adjacent[2].heightmap[x][z] = val;
			} else {
				adjacent[3].heightmap[x][z] = val;
			}
		}
	}*/

	public void cleanUp() {
		if (model != null) {
			model.cleanUp();
		}
	}

	public Model getModel() {
		return model;
	}

	public Tile getTileAt(float rx, float ry, float rz) {
		int dx = (int) (Math.round(rx/TILE_SIZE) * 1);
		int dy = (int) (Math.round(ry/TILE_SIZE) * 1) + (CHUNK_HEIGHT/2);
		int dz = (int) (Math.round(rz/TILE_SIZE) * 1);
		if (dy >= tiles[0].length || dy < 0)
			return null;
		return tiles[dx][dy][dz];
	}
}
