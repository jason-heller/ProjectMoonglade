package scene.overworld;

import static map.building.BuildingTile.TILE_SIZE;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Window;
import gl.particle.Particle;
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
import map.tile.TileProperties;
import scene.MainMenu;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.EntityData;
import scene.entity.EntityHandler;
import scene.entity.PlayerEntity;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import scene.overworld.inventory.tool.Axe;
import scene.overworld.inventory.tool.EditorBoundsTool;
import scene.overworld.inventory.tool.Spade;
import scene.overworld.inventory.tool.Trowel;

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
		EntityHandler.init();
		
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
		EntityHandler.addEntity(player);
		
		load();
	}

	@Override
	public void load() {
	}
	
	@Override
	public void update() {
		camera.move();
		EntityHandler.update(enviroment.getTerrain());
	}

	@Override
	public void tick() {
		if (returnToMenu) {
			Application.changeScene(MainMenu.class);
			return;
		}
		
		ui.update();
		EntityHandler.tick(enviroment.getTerrain());
		
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
		
		TerrainIntersection terrainIntersection = null;
		if (!inventory.isOpen()) {
			terrainIntersection = enviroment.getTerrain().terrainRaycast(camera.getPosition(),
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
				Spade.interact(chunkPtr, terrain, terrainIntersection, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				break;
			case TROWEL:
				Trowel.interact(chunkPtr, terrain, terrainIntersection, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				break;
			case AXE:
				if (Debug.structureMode) {
					EditorBoundsTool.interact(selectionPt, lmb, rmb);
				} else {
					Axe.interact(chunkPtr, terrain, terrainIntersection, player, camera, selectionPt, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				}
				break;
				
			default:
				tile = chunkPtr.getBuilding().get(_x, _y, _z);
				
				byte facing = BuildingTile.getFacingByte(camera, Input.isDown("sneak"));
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
					
					if ((tile.getWalls() & facing) != 0) {
						EntityHandler.addEntity(new ItemEntity(exactSelectionPt, tile.getMaterial(facingIndex).getDrop(), 1));
					}
					
					chunkPtr.setTile(_x, _y, _z, facing,
							Material.NONE, (byte) 0);
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
						
						if (envTile.getMaterial() == Material.PLANKS) {
							
							TileProperties props = chunkPtr.getEnvTileProperties(relX, relZ);
							if (props.damage % 5 != 2) {
								props.damage--;
								
								for (int i = 0; i < 12; i++) {
									Vector3f pos = new Vector3f(exactSelectionPt);
									pos.y += envTile.getBounds().y;
									pos.x += -2f + (Math.random() * 4f);
									pos.z += -2f + (Math.random() * 4f);
									
									

									if (Math.random() < .1) {
										EntityHandler.addEntity(new ItemEntity(pos, Item.STICKS, 1));
									} else {
										new Particle(Resources.getTexture("particles"), pos, new Vector3f(), .005f, 100,
												(float) Math.random() * 360f, 1f, .5f + (float) (Math.random() * .5f), 3, 3);
									}
								}
							}
						}
					}
				}
				
				final Item selected = inventory.getSelected();
				if (rmb && selected != Item.AIR) {

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
						
						if (mat == Material.NONE) {
							if (selected.getEntityId() != -1) {
								Entity entity = EntityData.instantiate(selected.getEntityId());
								entity.position.set(selectionPt);
								EntityHandler.addEntity(entity);
								
							}
							
							return;
						}
						
						if (tile != null && (tile.getWalls() & facing) != 0) {
							EntityHandler.addEntity(new ItemEntity(exactSelectionPt, tile.getMaterial(facingIndex).getDrop(), 1));
						}
						
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
		EntityHandler.clearEntities();
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
		EntityHandler.render(camera, enviroment.getLightDirection());	
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
