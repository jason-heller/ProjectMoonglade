package map.building;

import static map.Chunk.CHUNK_SIZE;
import static map.Chunk.VERTEX_COUNT;
import static map.building.BuildingTile.BACK;
import static map.building.BuildingTile.BOTTOM;
import static map.building.BuildingTile.FRONT;
import static map.building.BuildingTile.LEFT;
import static map.building.BuildingTile.RIGHT;
import static map.building.BuildingTile.TILE_SIZE;
import static map.building.BuildingTile.TOP;

import org.joml.Vector3f;

import dev.Console;
import gl.res.Model;
import map.Chunk;
import map.Material;
import util.ModelBuilder;

public class Building {
	private BuildingTile[][][] buildingTiles;
	private Chunk chunk;
	private Model model;
	
	public final static int MIN_BUILD_HEIGHT = -32;
	public final static int MAX_BUILD_HEIGHT = 128;
	public final static int ARR_HEIGHT = 160;
	
	public Building(Chunk chunk) {
		this.chunk = chunk;
		buildingTiles = new BuildingTile[VERTEX_COUNT*2][ARR_HEIGHT][VERTEX_COUNT*2];
	}
	
	private void modifyArr(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall) {
					buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = null;
				} else {
					tile.append(wall, material, specialFlags);
				}
			}
		} else if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, specialFlags);
		} else {
			tile.append(wall, material, specialFlags);
		}
	}
	
	private void modifyArrOuterwall(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall) {
					buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = null;
				} else {
					tile.append(wall, material, specialFlags);
				}
			}
		} else if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, specialFlags);
		} else if ((tile.getWalls() & wall) == 0 || material.isTransparent()) {
			tile.append(wall, material, specialFlags);
		}
	}
	
	public void addIfEmpty(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		int _x = (int)(Math.round(x / TILE_SIZE) * TILE_SIZE);
		int _y = (int)(Math.round(y / TILE_SIZE) * TILE_SIZE);
		int _z = (int)(Math.round(z / TILE_SIZE) * TILE_SIZE);
		modifyArrOuterwall(_x, _y, _z, wall, material, specialFlags);
		
		buildModel();
	}
	
	public void setTile(int x, int y, int z, byte wall, Material[] material, byte specialFlags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, specialFlags);
		} else {
			tile.append(wall, material, specialFlags);
		}
	}
	
	public void setTile(int x, int y, int z, byte wall, Material material, byte specialFlags) {
		int _x = (int)(Math.round(x / TILE_SIZE) * TILE_SIZE);
		int _y = (int)(Math.round(y / TILE_SIZE) * TILE_SIZE);
		int _z = (int)(Math.round(z / TILE_SIZE) * TILE_SIZE);
		modifyArr(_x, _y, _z, wall, material, specialFlags);
		
		switch(wall) {
		case 1: 
			if (_x == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX-1, chunk.arrZ);
				_x = Chunk.VERTEX_COUNT - 1;
				neighbor.getBuilding().addIfEmpty(_x, _y, _z, (byte)2, material, specialFlags);
			} else {
				modifyArrOuterwall(_x-1,_y,_z, (byte)2, material, specialFlags);
			}
			break;
		case 2: 
			if (_x == Chunk.VERTEX_COUNT-1) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX+1, chunk.arrZ);
				_x = 0;
				neighbor.getBuilding().addIfEmpty(_x, _y, _z, (byte)1, material, specialFlags);
			} else {
				modifyArrOuterwall(_x+1,_y,_z, (byte)1, material, specialFlags);
			}
			break;
		case 4: 
			modifyArrOuterwall(_x,_y+1,_z, (byte)8, material, specialFlags);
			break;
		case 8: 
			modifyArrOuterwall(_x,_y-1,_z, (byte)4, material, specialFlags);
			break;
		case 16: 
			if (_z == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ-1);
				_z = Chunk.VERTEX_COUNT - 1;
				neighbor.getBuilding().addIfEmpty(_x, _y, _z, (byte)32, material, specialFlags);
			} else {
				modifyArrOuterwall(_x,_y,_z-1, (byte)32, material, specialFlags);
			}
			break;
		case 32: 
			if (_z == Chunk.VERTEX_COUNT-1) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ+1);
				_z = 0;
				neighbor.getBuilding().addIfEmpty(_x, _y, _z, (byte)16, material, specialFlags);
			} else {
				modifyArrOuterwall(_x,_y,_z+1, (byte)16, material, specialFlags);
			}
			break;
		}
		
		buildModel();
	}

	public Model buildModel() {
		
		if (model != null) {
			model.cleanUp();
		}
		
		ModelBuilder builder = new ModelBuilder();
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f();
		float s = TILE_SIZE;
		
		for (int x = 0; x < buildingTiles.length; x++) {
			for (int y = 0; y < ARR_HEIGHT; y++) {
				for (int z = 0; z < buildingTiles.length; z++) {
					BuildingTile tile = buildingTiles[x][y][z];
					
					if (tile == null) {
						continue;
					}
					
					float dx = chunk.realX + (x * TILE_SIZE);
					float dy = ((y + MIN_BUILD_HEIGHT) * TILE_SIZE);
					float dz = chunk.realZ + (z * TILE_SIZE);

					byte walls = tile.getWalls();
					byte flags = tile.getFlags();
					Vector3f tex = new Vector3f();
					tex = Material.getTexCoordData(tex, tile.materials[0], flags);
					if ((walls & LEFT) != 0) {
						p1.set(dx, dy + s, dz);
						p2.set(dx, dy + s, dz + s);
						p3.set(dx, dy, dz + s);
						p4.set(dx, dy, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[1], flags);
					if ((walls & RIGHT) != 0) {
						p1.set(dx + s, dy + s, dz + s);
						p2.set(dx + s, dy + s, dz);
						p3.set(dx + s, dy, dz);
						p4.set(dx + s, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[2], flags);
					if ((walls & TOP) != 0) {
						p1.set(dx + s, dy + s, dz + s);
						p2.set(dx, dy + s, dz + s);
						p3.set(dx, dy + s, dz);
						p4.set(dx + s, dy + s, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[3], flags);
					if ((walls & BOTTOM) != 0) {
						p1.set(dx, dy, dz + s);
						p2.set(dx + s, dy, dz + s);
						p3.set(dx + s, dy, dz);
						p4.set(dx, dy, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[4], flags);
					if ((walls & FRONT) != 0) {
						p1.set(dx + s, dy + s, dz);
						p2.set(dx, dy + s, dz);
						p3.set(dx, dy, dz);
						p4.set(dx + s, dy, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[5], flags);
					if ((walls & BACK) != 0) {
						p1.set(dx, dy + s, dz + s);
						p2.set(dx + s, dy + s, dz + s);
						p3.set(dx + s, dy, dz + s);
						p4.set(dx, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4, tex);
					}
				}
			}
		}
		
		model = builder.finish();
		return model;
	}
	
	public void cleanUp() {
		if (model != null) {
			model.cleanUp();
		}
		
		model = null;
		this.buildingTiles = null;
		this.chunk = null;
	}

	public Model getModel() {
		return model;
	}

	public BuildingTile getTileAt(float rx, float ry, float rz) {
		int dx = (int) (Math.round(rx/TILE_SIZE));
		int dy = (int) (Math.round(ry/TILE_SIZE)) - MIN_BUILD_HEIGHT;
		int dz = (int) (Math.round(rz/TILE_SIZE));
		if (dy >= buildingTiles[0].length || dy < 0)
			return null;
		//EntityControl.addEntity(new TestEntity(dx+(chunk.x*Chunk.CHUNK_SIZE),dy - (CHUNK_HEIGHT/4),dz+(chunk.z*Chunk.CHUNK_SIZE)));
		return buildingTiles[dx][dy][dz];
	}

	public BuildingTile get(int i, int j, int k) {
		return buildingTiles[i][j - MIN_BUILD_HEIGHT][k];
	}

	public BuildingTile[][][] getTilemap() {
		return buildingTiles;
	}

	public void setTilemap(BuildingTile[][][] tiles) {
		this.buildingTiles = tiles;
	}
}
