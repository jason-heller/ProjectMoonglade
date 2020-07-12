package map.tile;

import static map.tile.Tile.TILE_SIZE;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import dev.Console;
import gl.res.Model;
import map.Chunk;
import map.Material;
import util.ModelBuilder;

public class BuildData {
	private Map<Integer, BuildSector> sectors;
	private Chunk chunk;
	private Model model;
	
	public final static int MIN_BUILD_HEIGHT = -999;
	public final static int MAX_BUILD_HEIGHT = 999;
	//public final static int ARR_HEIGHT = 160;

	public BuildData(Chunk chunk) {
		this.chunk = chunk;
		sectors = new HashMap<Integer, BuildSector>();
	}

	
	
	// Use this to modify tile data internally
	// See: setTile()
	private void setData(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		//int rx = x - chunk.realX;
		//int rz = z - chunk.realZ;
		
		int tx = (int)Math.floor(x/8f);
		int ty = (int)Math.floor(y/8f);
		int tz = (int)Math.floor(z/8f);
		BuildSector sector = getSector(tx, ty, tz);
		if (sector == null) {
			// Add new sector
			sector = new BuildSector(tx, ty, tz);
			sectors.put(getSectorId(tx, ty, tz), sector);
		}
		
		tx = x - tx*8;
		ty = y - ty*8;
		tz = z - tz*8;
		
		Tile tile = sector.get(tx, ty, tz);
		
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall && tile.getSlope() == slope) {
					sector.removeTile(tx, ty, tz);
				} else {
					tile.append(wall, slope, material, flags);
				}
			}
		} else if (tile == null) {
			tile = new Tile(material, wall, slope, flags);
			sector.addTile(tile, tx, ty, tz);
		} else {
			tile.append(wall, slope, material, flags);
		}
	}
	
	// Almost the same as above, but will only make changes if it considers the target tile "free" (meaning nothing is occupying the space or
	// the occupying tile is transparent
	// See: setTile()
	private void setDataIfFree(int x, int y, int z, byte wall, byte slope, Material material, byte flags) {
		//int rx = x - chunk.realX;
		//int rz = z - chunk.realZ;
		
		int tx = (int)Math.floor(x/8f);
		int ty = (int)Math.floor(y/8f);
		int tz = (int)Math.floor(z/8f);
		
		BuildSector sector = getSector(tx, ty, tz);
		if (sector == null) {
			// Add new sector
			sector = new BuildSector(tx, ty, tz);
			sectors.put(getSectorId(tx, ty, tz), sector);
		}
		
		tx = x - tx*8;
		ty = y - ty*8;
		tz = z - tz*8;
		
		Tile tile = sector.get(tx, ty, tz);
		
		if (material == Material.NONE) {
			if (tile != null) {
				if (tile.getWalls() == wall && tile.getSlope() == slope) {
					sector.removeTile(tx, ty, tz);
				} else {
					tile.append(wall, slope, material, flags);
				}
			}
		} else if (tile == null) {
			tile = new Tile(material, wall, slope, flags);
			sector.addTile(tile, tx, ty, tz);
		} else if ((tile.getWalls() & wall) == 0 || material.isTransparent() || tile.getMaterial(Tile.getFacingIndex(wall)).isTransparent()) {
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
		setData(x, y, z, wall, slope, material, flags);
		
		switch(wall) {
		case 1: 
			if (x == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX-1, chunk.arrZ);
				x = Chunk.VERTEX_COUNT - 2;
				neighbor.getBuilding().setFromNeighbor(x, y, z, (byte)2, (byte)0, material, flags);
			} else {
				setDataIfFree(x-1,y,z, (byte)2, (byte)0, material, flags);
				
			}
			break;
		case 2: 
			if (x == Chunk.VERTEX_COUNT-2) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX+1, chunk.arrZ);
				x = 0;
				neighbor.getBuilding().setFromNeighbor(x, y, z, (byte)1, (byte)0, material, flags);
			} else {
				setDataIfFree(x+1,y,z, (byte)1, (byte)0, material, flags);
			}
			break;
		case 4: 
			setDataIfFree(x,y+1,z, (byte)8, (byte)0, material, flags);
			break;
		case 8: 
			setDataIfFree(x,y-1,z, (byte)4, (byte)0, material, flags);
			break;
		case 16: 
			if (z == 0) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ-1);
				z = Chunk.VERTEX_COUNT - 2;
				neighbor.getBuilding().setFromNeighbor(x, y, z, (byte)32, (byte)0, material, flags);
			} else {
				setDataIfFree(x,y,z-1, (byte)32, (byte)0, material, flags);
			}
			break;
		case 32: 
			if (z == Chunk.VERTEX_COUNT-2) {
				Chunk neighbor = chunk.getTerrain().get(chunk.arrX, chunk.arrZ+1);
				z = 0;
				neighbor.getBuilding().setFromNeighbor(x, y, z, (byte)16, (byte)0, material, flags);
			} else {
				setDataIfFree(x,y,z+1, (byte)16, (byte)0, material, flags);
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
		setDataIfFree(x, y, z, wall, slope, material, flags);
		
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
		
		ModelBuilder builder = new ModelBuilder(true, true, false);
		builder.addAttrib(3, 3);
		
		Vector3f tex = new Vector3f();
		
		for(BuildSector sector : sectors.values()) {
			sector.draw(builder, chunk.realX, chunk.realZ, tex);
		}
		
		model = builder.finish();
		return model;
	}
	
	public void cleanUp() {
		if (model != null) {
			model.cleanUp();
		}
		
		model = null;
		//this.sectors = null;
		this.chunk = null;
	}

	public Model getModel() {
		return model;
	}

	public Tile getTileAt(float rx, float ry, float rz) {
		int dx = (int) (Math.floor(rx/TILE_SIZE));
		int dy = (int) (Math.floor(ry/TILE_SIZE));
		int dz = (int) (Math.floor(rz/TILE_SIZE));
		return get(dx, dy, dz);
	}

	public Tile get(int x, int y, int z) {
		int dx = x/8;
		int dy = (int)Math.floor(y/8f);
		int dz = z/8;
		BuildSector sector = getSector(dx, dy, dz);
		if (sector == null) return null;
		dx *= 8;
		dy *= 8;
		dz *= 8;
		
		return sector.get(x-dx, y-dy, z-dz);//buildingTiles[i][j - MIN_BUILD_HEIGHT][k];
	}

	public BuildSector getSector(int x, int y, int z) {
		return sectors.get(getSectorId(x, y, z));
	}

	private int getSectorId(int x, int y, int z) {
		return x + y*4 + z*2;
	}

	public Collection<BuildSector> getSectors() {
		return sectors.values();
	}

	public BuildSector addSector(int x, int y, int z) {
		BuildSector sector = new BuildSector(x, y, z);
		sectors.put(getSectorId(x, y, z), sector);
		return sector;
	}
}
