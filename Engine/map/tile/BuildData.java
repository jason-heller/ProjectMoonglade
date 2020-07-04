package map.tile;

import static map.Chunk.VERTEX_COUNT;
import static map.tile.BuildingTile.BACK;
import static map.tile.BuildingTile.BOTTOM;
import static map.tile.BuildingTile.FRONT;
import static map.tile.BuildingTile.LEFT;
import static map.tile.BuildingTile.RIGHT;
import static map.tile.BuildingTile.TILE_SIZE;
import static map.tile.BuildingTile.TOP;

import org.joml.Vector3f;

import dev.Console;
import gl.res.Model;
import gl.res.TileModel;
import map.Chunk;
import map.Material;
import util.ModelBuilderOld;

public class BuildData {
	private BuildingTile[][][] buildingTiles;
	private Chunk chunk;
	private Model model;
	
	public final static int MIN_BUILD_HEIGHT = -32;
	public final static int MAX_BUILD_HEIGHT = 128;
	public final static int ARR_HEIGHT = 160;

	public BuildData(Chunk chunk) {
		this.chunk = chunk;
		buildingTiles = new BuildingTile[VERTEX_COUNT * 2][ARR_HEIGHT][VERTEX_COUNT * 2];
	}

	// Use this to modify tile data internally
	// See: setTile()
	private void setData(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall && tile.getSlope() == slope) {
					buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = null;
				} else {
					tile.append(wall, slope, material, flags);
				}
			}
		} else if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, slope, flags);
		} else {
			tile.append(wall, slope, material, flags);
		}
	}
	
	// Almost the same as above, but will only make changes if it considers the target tile "free" (meaning nothing is occupying the space or
	// the occupying tile is transparent
	// See: setTile()
	private void setDataIfFree(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall && tile.getSlope() == slope) {
					buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = null;
				} else {
					tile.append(wall, slope, material, flags);
				}
			}
		} else if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, slope, flags);
		} else if ((tile.getWalls() & wall) == 0 || material.isTransparent()) {
			tile.append(wall, slope, material, flags);
		}
	}
	
	
	/** Sets a tile within the build data
	 * @param x local X position
	 * @param y local Y position
	 * @param z local Z position
	 * @param wall flags to represent which walls to set (xxBFBTRL)
	 * @param material array of material enums, corresponding to the wall flags
	 * @param flags certain tiles can have specific flags, if the tile being set is using them, pass them here
	 */
	public void setTile(int x, int y, int z, byte wall, byte slope, Material[] material, byte flags) {
		BuildingTile tile = buildingTiles[x][y-MIN_BUILD_HEIGHT][z];
		if (tile == null) {
			buildingTiles[x][y-MIN_BUILD_HEIGHT][z] = new BuildingTile(chunk, material, wall, slope, flags);
		} else {
			tile.append(wall, slope, material, flags);
		}
	}
	
	/** Sets a tile within the build data
	 * @param x local X position
	 * @param y local Y position
	 * @param z local Z position
	 * @param wall flags to represent which walls to set (xxBFBTRL)
	 * @param material material to use for all walls
	 * @param flags certain tiles can have specific flags, if the tile being set is using them, pass them here
	 */
	public void setTile(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		int _x = (int)(Math.round(x / TILE_SIZE) * TILE_SIZE);
		int _y = (int)(Math.round(y / TILE_SIZE) * TILE_SIZE);
		int _z = (int)(Math.round(z / TILE_SIZE) * TILE_SIZE);
		setData(_x, _y, _z, wall, slope, material, flags);

		switch(wall) {
		case 1: 
			if (_x == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX-1, chunk.arrZ);
				_x = Chunk.VERTEX_COUNT - 2;
				neighbor.getBuilding().setFromNeighbor(_x, _y, _z, (byte)2, (byte)0, material, flags);
			} else {
				setDataIfFree(_x-1,_y,_z, (byte)2, (byte)0, material, flags);
			}
			break;
		case 2: 
			if (_x == Chunk.VERTEX_COUNT-1) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX+1, chunk.arrZ);
				_x = 0;
				neighbor.getBuilding().setFromNeighbor(_x, _y, _z, (byte)1, (byte)0, material, flags);
			} else {
				setDataIfFree(_x+1,_y,_z, (byte)1, (byte)0, material, flags);
			}
			break;
		case 4: 
			setDataIfFree(_x,_y+1,_z, (byte)8, (byte)0, material, flags);
			break;
		case 8: 
			setDataIfFree(_x,_y-1,_z, (byte)4, (byte)0, material, flags);
			break;
		case 16: 
			if (_z == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ-1);
				_z = Chunk.VERTEX_COUNT - 2;
				neighbor.getBuilding().setFromNeighbor(_x, _y, _z, (byte)32, (byte)0, material, flags);
			} else {
				setDataIfFree(_x,_y,_z-1, (byte)32, (byte)0, material, flags);
			}
			break;
		case 32: 
			if (_z == Chunk.VERTEX_COUNT-1) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ+1);
				_z = 0;
				neighbor.getBuilding().setFromNeighbor(_x, _y, _z, (byte)16, (byte)0, material, flags);
			} else {
				setDataIfFree(_x,_y,_z+1, (byte)16, (byte)0, material, flags);
			}
			break;
		}
		
		buildModel();
	}
	
	
	/** Helper function for above, used to call setDataIfFree() form a neighboring chunk
	 * @param x x position in world space
	 * @param y y position in world space
	 * @param z z position in world space
	 * @param wall wall flags
	 * @param material material to use for all walls
	 * @param flags tile specific flags
	 */
	private void setFromNeighbor(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		int _x = (int)(Math.round(x / TILE_SIZE) * TILE_SIZE);
		int _y = (int)(Math.round(y / TILE_SIZE) * TILE_SIZE);
		int _z = (int)(Math.round(z / TILE_SIZE) * TILE_SIZE);
		setDataIfFree(_x, _y, _z, wall, slope, material, flags);
		
		buildModel();
	}
	
	/** rebuilds the tile model
	 * 
	 * @return the tile model. duh
	 */
	public Model buildModel() {
		
		if (model != null) {
			model.cleanUp();
		}
		
		ModelBuilderOld builder = new ModelBuilderOld();
		//Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f();
		//float s = TILE_SIZE;
		
		Vector3f tex = new Vector3f();
		
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
					byte slope = tile.getSlope();
					int slantFactor = (tile.getWalls() >> 6);
					
					byte b = 1;
					for(int f = 0; f < 6; f++ ) {
						if ((walls & b) != 0) {
							TileModel model = tile.getMaterial(f).getTileModel();
							tex = Material.getTexCoordData(tex, tile.materials[f], flags);
							model.pass(dx,dy,dz, builder, tex, b, flags, (byte)-1);
						}
						
						b *= 2;
					}
					
					b = 1;

					if (slantFactor == 0) {	// Gradual
						for(int f = 0; f < 6; f++ ) {
							if ((slope & b) != 0) {
								TileModel model = tile.getMaterial(6).getTileModel();
								tex = Material.getTexCoordData(tex, tile.materials[6], flags);
								model.pass(dx,dy,dz, builder, tex, b, flags, (byte)slantFactor);
							}
							
							b *= 2;
						}
					} else if (slantFactor == 1) { // Steep
						for(int f = 0; f < 4; f++ ) {
							if ((slope & b) != 0) {
								TileModel model = tile.getMaterial(6).getTileModel();
								tex = Material.getTexCoordData(tex, tile.materials[6], flags);
								model.pass(dx,dy,dz, builder, tex, b, flags, (byte)slantFactor);
							}
							
							b *= 2;
						}
					}
					
					/*tex = Material.getTexCoordData(tex, tile.materials[0], flags);
					if ((walls & LEFT) != 0) {
						p1.set(dx, dy + s, dz); p2.set(dx, dy + s, dz + s); p3.set(dx, dy, dz + s); p4.set(dx, dy, dz);
						//if ()
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[1], flags);
					if ((walls & RIGHT) != 0) {
						p1.set(dx + s, dy + s, dz + s); p2.set(dx + s, dy + s, dz); p3.set(dx + s, dy, dz); p4.set(dx + s, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[2], flags);
					if ((walls & TOP) != 0) {
						p1.set(dx + s, dy + s, dz + s); p2.set(dx, dy + s, dz + s); p3.set(dx, dy + s, dz); p4.set(dx + s, dy + s, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[3], flags);
					if ((walls & BOTTOM) != 0) {
						p1.set(dx, dy, dz + s); p2.set(dx + s, dy, dz + s); p3.set(dx + s, dy, dz); p4.set(dx, dy, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[4], flags);
					if ((walls & FRONT) != 0) {
						p1.set(dx + s, dy + s, dz); p2.set(dx, dy + s, dz); p3.set(dx, dy, dz); p4.set(dx + s, dy, dz);
						builder.addQuad(p1, p2, p3, p4, tex);
					}

					tex = Material.getTexCoordData(tex, tile.materials[5], flags);
					if ((walls & BACK) != 0) {
						p1.set(dx, dy + s, dz + s); p2.set(dx + s, dy + s, dz + s); p3.set(dx + s, dy, dz + s); p4.set(dx, dy, dz + s);
						builder.addQuad(p1, p2, p3, p4, tex);
					}*/
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
		int dx = (int) (Math.floor(rx/TILE_SIZE));
		int dy = (int) (Math.floor(ry/TILE_SIZE)) - MIN_BUILD_HEIGHT;
		int dz = (int) (Math.floor(rz/TILE_SIZE));
		if (dy >= buildingTiles[0].length || dy < 0)
			return null;

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
