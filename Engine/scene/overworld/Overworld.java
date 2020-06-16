package scene.overworld;

import static map.building.BuildingTile.TILE_SIZE;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleHandler;
import gl.skybox.Skybox;
import io.Input;
import io.SaveDataIO;
import map.Chunk;
import map.Enviroment;
import map.Material;
import map.Terrain;
import map.TerrainIntersection;
import map.building.Building;
import map.building.BuildingTile;
import map.tile.EnvTile;
import scene.MainMenu;
import scene.Scene;
import scene.entity.EntityControl;
import scene.entity.ItemEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;

public class Overworld implements Scene {
	
	private static final int PLAYER_REACH = Chunk.POLYGON_SIZE * 6;
	public static String worldName;
	public static String worldSeed;
	public static String worldFileName;

	private Skybox skybox;
	
	private Enviroment enviroment;
	private Camera camera;
	private Inventory inventory;
	
	private PlayerEntity player;
		
	private final OverworldUI ui;
	
	private Vector3f selectionPt, exactSelectionPt;
	private byte cameraFacing;

	protected boolean returnToMenu;
	
	public Overworld() {
		Enviroment.time = 0;
		EntityControl.init();
		
		camera = new Camera();
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.setPosition(new Vector3f((Chunk.CHUNK_SIZE)/2f, 0, (Chunk.CHUNK_SIZE)/2f));
		camera.grabMouse();
		
		inventory = new Inventory();
		
		SaveDataIO.readSaveData(this);
		
		skybox = new Skybox();
		enviroment = new Enviroment(this);
		
		ui = new OverworldUI(this);
		
		player = new PlayerEntity(this);
		camera.focusOn(player);
		player.position.set(camera.getPosition());
		EntityControl.addEntity(player);
		
		load();
	}

	@Override
	public void load() {
	}
	
	@Override
	public void update() {
		camera.move();
		player.update(this);
	}

	@Override
	public void tick() {
		if (returnToMenu) {
			Application.changeScene(MainMenu.class);
			return;
		}
		
		ui.update();
		EntityControl.update(enviroment.getTerrain());
		
		if (ui.isPaused()) {
			return;
		}
		
		enviroment.tick(this);
		inventory.update();
		
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		cameraFacing = BuildingTile.getFacingByte(camera, Input.isDown("sneak"));
		selectionPt = null;
		exactSelectionPt = null;
		boolean facingTile = false;
		
		TerrainIntersection terrainIntersection = enviroment.getTerrain().terrainRaycast(camera.getPosition(),
				camera.getDirectionVector(), PLAYER_REACH);
		if (terrainIntersection != null) {
			selectionPt = terrainIntersection.getPoint();
		}
			
		if (inventory.getSelected().getMaterial() != Material.NONE) {
			Vector3f pt = enviroment.getTerrain().buildingRaycast(this, camera.getPosition(), camera.getDirectionVector(), PLAYER_REACH, cameraFacing, Input.isDown("sneak"));

			
			if (pt != null && (selectionPt == null || Vector3f.distanceSquared(camera.getPosition(), pt) <
			Vector3f.distanceSquared(camera.getPosition(), selectionPt))) {
				selectionPt = pt;
				facingTile = true;
			}
		}
		
		if (selectionPt != null) {
			exactSelectionPt = new Vector3f(selectionPt);
			selectionPt.set((float)Math.floor(selectionPt.x),
					(float)Math.floor(selectionPt.y ),
					(float)Math.floor(selectionPt.z ));
			
			selectionPt.y = Math.min(Math.max(selectionPt.y, Building.MIN_BUILD_HEIGHT+1), Building.MAX_BUILD_HEIGHT-1);
			
			float dx = camera.getPosition().x - selectionPt.x;
			float dy = camera.getPosition().y - selectionPt.y;
			float dz = camera.getPosition().z - selectionPt.z;
			
			boolean withinRange = (dx*dx + dy*dy + dz*dz <= PLAYER_REACH * PLAYER_REACH);
			
			final boolean lmb = Input.isPressed(Input.KEY_LMB), rmb = Input.isPressed(Input.KEY_RMB);
			
			final Terrain terrain = enviroment.getTerrain();
			Chunk chunkPtr = terrain.getChunkAt(selectionPt.x, selectionPt.z);
			
			final int cx = chunkPtr.realX;
			final int cz = chunkPtr.realZ;
			int _x = (int) ((selectionPt.x - cx) / TILE_SIZE);
			int _y = (int) (selectionPt.y / TILE_SIZE);
			int _z = (int) ((selectionPt.z - cz) / TILE_SIZE);
			
			final int facingIndex = cameraFacing == 1 ? 0 : (int)Math.sqrt(cameraFacing);
			
			BuildingTile tile;
			
			switch(inventory.getSelected()) {
			case SPADE:
				if (facingTile || chunkPtr == null) break;
				
				if (lmb && withinRange) {
					
					EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
					if (envTile != null && envTile.isDestroyableBy(Item.SPADE)) {
						break;
					}
					
					int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
					int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
					if (!chunkPtr.breakEnvTile(relX, relZ)) {
						if (Input.isDown("sneak")) {
							chunkPtr.smoothHeight(relX, relZ);
						} else {
							chunkPtr.addHeight(relX, relZ, -Chunk.DIG_SIZE);
						}
					}
				}
				
				if (rmb && withinRange) {
					int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
					int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
					
					if (Input.isDown("sneak")) {
						chunkPtr.setHeight(relX, relZ, (int)selectionPt.y/Chunk.POLYGON_SIZE);
					} else {
						chunkPtr.addHeight(relX, relZ, Chunk.DIG_SIZE);
					}
					
					chunkPtr.breakEnvTile(relX, relZ);
				}
				break;
			case TROWEL:
				if (facingTile || chunkPtr == null) break;
				
				if (lmb && withinRange) {
					
					EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
					if (envTile != null && envTile.isDestroyableBy(Item.TROWEL)) {
						break;
					}
					
					int relX = Math.round(selectionPt.x - cx);
					int relZ = Math.round(selectionPt.z - cz);
					if (!chunkPtr.breakEnvTile(relX, relZ)) {
						//chunkPtr.raiseHeight(relX, relZ, -Chunk.DIG_SIZE/2f);
					}
				}
				
				if (rmb && withinRange) {
					int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
					int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
					
					/*if (Input.isDown("sneak")) {
						chunkPtr.setHeight(relX, relZ, (int)selectionPt.y/Chunk.POLYGON_SIZE);
					} else {
						chunkPtr.addHeight(relX, relZ, Chunk.DIG_SIZE);
					}*/
					
					chunkPtr.damageEnvTile(relX, relZ, (byte)5);
				}
				break;
			case AXE:
				if (facingTile || chunkPtr == null) {
					player.getSource().play(Resources.getSound("swing"));
					break;
				}
				
				if (lmb) {
					EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
					if (envTile == null || !envTile.isDestroyableBy(Item.AXE)) {
						player.getSource().play(Resources.getSound("swing"));
						break;
					} else {
						player.getSource().play(Resources.getSound("chop_bark"));
					}
					
					Vector3f splashDir = new Vector3f(camera.getDirectionVector()).negate().normalize();
					ParticleHandler.addSplash(envTile.getMaterial(), exactSelectionPt, splashDir);
					//ParticleHandler.addBurst(Resources.getTexture("materials"), 0, 0, selectionPt);
					
					int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
					int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
					chunkPtr.damageEnvTile(relX, relZ, (byte)15);
				}
				break;
			default:
				tile = chunkPtr.getBuilding().get(_x, _y, _z);
				if (lmb && tile != null) {
					if (tile.getMaterial(facingIndex).isTiling()) {
						final float rx = (_x * TILE_SIZE) + cx;
						final float ry = (_y * TILE_SIZE);
						final float rz = (_z * TILE_SIZE) + cz;
						dx = ((cameraFacing & 3) == 0) ? TILE_SIZE : 0;
						dz = TILE_SIZE - dx;
						if ((cameraFacing & 1) != 0) dz *= -1;
						if ((cameraFacing & 32) != 0) dx *= -1;
						Material.removeTilingFlags(tile, enviroment.getTerrain(), rx, ry, rz, dx, dz, facingIndex, 0);
					}
					byte facing = BuildingTile.getFacingByte(camera, Input.isDown("sneak"));
					chunkPtr.setTile(_x, _y, _z, facing,
							Material.NONE, (byte) 0);
					EntityControl.addEntity(new ItemEntity(exactSelectionPt, tile.getMaterial(facing).getDrop(), 1));
					chunkPtr.rebuildWalls();
				}
				
				if (!facingTile && chunkPtr != null && lmb) {
					EnvTile envTile = terrain.getTileById(terrainIntersection.getTile());
					
					if (envTile != null) {
						int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
						int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
						
						Item tool = envTile.getTool();
						if (tool == Item.AIR) {
							ParticleHandler.addBurst("materials", 0, 0, exactSelectionPt);
							chunkPtr.breakEnvTile(relX, relZ);
						}
					}
				}
				
				final Item selected = inventory.getSelected();
				if (rmb && selected != Item.AIR && selected.getMaterial() != Material.NONE) {

					if (chunkPtr != null) {
						tile = chunkPtr.getBuilding().get(_x, _y, _z);
						
						if (_x < 0) {
							chunkPtr = terrain.get(chunkPtr.arrX-1, chunkPtr.arrZ);
							_x += Chunk.VERTEX_COUNT*2;
						} else if (_x > Chunk.VERTEX_COUNT*2) {
							chunkPtr = terrain.get(chunkPtr.arrX+1, chunkPtr.arrZ);
							_x -= Chunk.VERTEX_COUNT*2;
						}
						
						if (_z < 0) {
							chunkPtr = terrain.get(chunkPtr.arrX, chunkPtr.arrZ-1);
							_z += Chunk.VERTEX_COUNT*2;
						} else if (_z > Chunk.VERTEX_COUNT*2) {
							chunkPtr = terrain.get(chunkPtr.arrX, chunkPtr.arrZ+1);
							_z += Chunk.VERTEX_COUNT*2;
						}
						
						final Material mat = selected.getMaterial();
						
						byte specialFlags = 0;
						if (mat.isTiling()) {
							final float rx = (_x * TILE_SIZE) + cx;
							final float ry = (_y * TILE_SIZE);
							final float rz = (_z * TILE_SIZE) + cz;
							dx = ((cameraFacing & 3) == 0) ? TILE_SIZE : 0;
							dz = TILE_SIZE - dx;
							if ((cameraFacing & 1) != 0) dz *= -1;
							if ((cameraFacing & 32) != 0) dx *= -1;
							
							chunkPtr.setTile(_x, _y, _z, cameraFacing, mat, (byte) 0);
							tile = chunkPtr.getBuilding().get(_x,_y,_z);
							Material.setTilingFlags(tile, terrain, rx, ry, rz, dx, dz, mat, facingIndex, 0);
							chunkPtr.getBuilding().buildModel();
						} else {
							chunkPtr.setTile(_x, _y, _z, cameraFacing, mat, specialFlags);
						}
	
						inventory.consume(inventory.getSelectionPos());
						chunkPtr.rebuildWalls();
					}
				}
			}
		} else {
			if (Input.isPressed(Input.KEY_LMB) && inventory.getSelected() != Item.AIR && inventory.getSelected().getMaterial() == Material.NONE) {
				player.getSource().play(Resources.getSound("swing"));
			}
		}
		
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
	}

	@Override
	public void cleanUp() {
		ui.cleanUp();
		EntityControl.clearEntities();
		enviroment.cleanUp();
		
		skybox.cleanUp();
		
		SaveDataIO.writeSaveData(this);
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public boolean isLoading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void render(float px, float py, float pz, float pw) {
		if (returnToMenu) return;
		Vector3f weatherColor = enviroment.getWeather().determineSkyColor();
		
		
		skybox.render(camera, Enviroment.time, enviroment.getClosestBiome().getSkyColor(), weatherColor);
		enviroment.render(camera, selectionPt, cameraFacing, px, py, pz, pw);
		EntityControl.render(camera, enviroment.getLightDirection());	
	}

	public Enviroment getEnviroment() {
		return enviroment;
	}

	public PlayerEntity getPlayer() {
		return this.player;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setCamFacingByte(byte cameraFacing) {
		this.cameraFacing = cameraFacing;
	}

	public byte getCamFacingByte() {
		return this.cameraFacing;
	}

	public Vector3f getSelectionPoint() {
		return selectionPt;
	}
}
