package scene.overworld;

import static map.tile.Tile.TILE_SIZE;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Application;
import core.Resources;
import dev.Debug;
import gl.Camera;
import gl.Window;
import gl.particle.Particle;
import gl.particle.ParticleHandler;
import io.Input;
import io.SaveDataIO;
import map.Chunk;
import map.Enviroment;
import map.Material;
import map.Terrain;
import map.TerrainIntersection;
import map.prop.Props;
import map.prop.StaticProp;
import map.prop.StaticPropProperties;
import map.tile.BuildData;
import map.tile.Tile;
import scene.MainMenu;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.EntityData;
import scene.entity.EntityHandler;
import scene.entity.PlayerEntity;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import scene.overworld.inventory.ItemData;
import scene.overworld.inventory.tool.Axe;
import scene.overworld.inventory.tool.EditorBoundsTool;
import scene.overworld.inventory.tool.Spade;
import scene.overworld.inventory.tool.Trowel;

public class Overworld implements Scene {
	
	private static final int PLAYER_REACH = Chunk.POLYGON_SIZE * 6;
	public static String worldName;
	public static String worldSeed;
	public static String worldFileName;

	private Enviroment enviroment;
	private Camera camera;
	private Inventory inventory;
	
	private PlayerEntity player;
		
	private final OverworldUI ui;
	
	private Vector3f selectionPt, exactSelectionPt;
	private byte cameraFacing;

	protected boolean returnToMenu;
	
	private byte slopeSetting = 0, wallSetting = 0;
	
	private float actionDelay = 0f;
	
	public Overworld() {

		Enviroment.exactTime = 0f;
		EntityHandler.init();
		
		camera = new Camera();
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.setPosition(new Vector3f((Chunk.CHUNK_SIZE)/2f, 0, (Chunk.CHUNK_SIZE)/2f));
		camera.grabMouse();
		
		inventory = new Inventory();
		
		SaveDataIO.readSaveData(this);
		
		enviroment = new Enviroment(this);
		enviroment.tick(this);
		
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
		if (returnToMenu) {
			Application.changeScene(MainMenu.class);
			return;
		}
		
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		cameraFacing = Tile.getFacingByte(camera, wallSetting == 1);
		selectionPt = null;
		exactSelectionPt = null;
		
		camera.move();
		EntityHandler.update(enviroment.getTerrain());
	
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
		boolean facingTile = false;
		
		TerrainIntersection terrainIntersection = null;
		if (!inventory.isOpen()) {
			terrainIntersection = enviroment.getTerrain().terrainRaycast(camera.getPosition(),
					camera.getDirectionVector(), PLAYER_REACH);
			if (terrainIntersection != null) {
				selectionPt = terrainIntersection.getPoint();
			}
				
			if (inventory.getSelected() != 0) {
				byte snapFlags = (byte) ((wallSetting == 1) ? 1 : 0);
				snapFlags = (byte) ((slopeSetting == 1) ? 2 : snapFlags);
				Vector3f pt = enviroment.getTerrain().buildingRaycast(this, camera.getPosition(), camera.getDirectionVector(), PLAYER_REACH, cameraFacing, snapFlags);

				
				if (pt != null && (selectionPt == null || Vector3f.distanceSquared(camera.getPosition(), pt) <
				Vector3f.distanceSquared(camera.getPosition(), selectionPt))) {
					selectionPt = pt;
					facingTile = true;
				}
			}
		}
		actionDelay = Math.max(actionDelay - Window.deltaTime, 0f);
		
		if (selectionPt != null && Input.isMouseGrabbed() && actionDelay == 0f) {
			exactSelectionPt = new Vector3f(selectionPt);
			selectionPt.set((float) Math.floor(selectionPt.x), (float) Math.floor(selectionPt.y),
					(float) Math.floor(selectionPt.z));

			selectionPt.y = Math.min(Math.max(selectionPt.y, BuildData.MIN_BUILD_HEIGHT+1), BuildData.MAX_BUILD_HEIGHT-1);
			
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
			
			Tile tile;
			
			if (lmb || rmb) {
				actionDelay = .05f;
			}
			
			//TODO: Refactor this
			switch(inventory.getSelected()) {
			case Item.SPADE:
				Spade.interact(chunkPtr, terrain, terrainIntersection, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				break;
			case Item.TROWEL:
				Trowel.interact(chunkPtr, terrain, terrainIntersection, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				break;
			case Item.AXE:
				if (Debug.structureMode) {
					EditorBoundsTool.interact(selectionPt, lmb, rmb);
				} else {
					Axe.interact(chunkPtr, terrain, terrainIntersection, player, camera, selectionPt, exactSelectionPt, cx, cz, facingTile, withinRange, lmb, rmb);
				}
				break;
				
			default:
				tile = chunkPtr.getBuilding().get(_x, _y, _z);
				
				byte facing = Tile.getFacingByte(camera, wallSetting == 1);
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
						EntityHandler.addEntity(new ItemEntity(getTileDropPos(selectionPt), tile.getMaterial(facingIndex).getDrop(), 1));
					}
					
					if (slopeSetting != 0) {
						chunkPtr.setTile(_x, _y, _z, (byte) 0, facing,
								Material.NONE, (byte) 0);
					} else {
						chunkPtr.setTile(_x, _y, _z, facing, (byte) 0,
								Material.NONE, (byte) 0);
					}
				}
				
				if (!facingTile && chunkPtr != null && lmb) {
					StaticProp envTile = Props.get(terrainIntersection.getTile());
					
					if (envTile != null) {
						int relX = (int)(selectionPt.x - cx)/Chunk.POLYGON_SIZE;
						int relZ = (int)(selectionPt.z - cz)/Chunk.POLYGON_SIZE;
						
						int tool = envTile.getTool();
						if (tool == Item.AIR) {
							ParticleHandler.addBurst("materials", 0, 0, exactSelectionPt);
							chunkPtr.destroyProp(relX, relZ);
						}
						
						if (envTile.getMaterial() == Material.PLANKS) {
							
							StaticPropProperties props = chunkPtr.getChunkPropProperties(relX, relZ);
							if (props.damage % 5 != 2) {
								props.damage--;
								
								for (int i = 0; i < 12; i++) {
									Vector3f pos = new Vector3f(exactSelectionPt);
									pos.y += envTile.getBounds().y;
									pos.x += -2f + (Math.random() * 4f);
									pos.z += -2f + (Math.random() * 4f);
									
									if (Math.random() < .1) {
										EntityHandler.addEntity(new ItemEntity(pos, "stick", 1));
									} else {
										new Particle(Resources.getTexture("particles"), pos, new Vector3f(), .005f, 100,
												(float) Math.random() * 360f, 1f, .5f + (float) (Math.random() * .5f), 3, 3);
									}
								}
							}
						}
					}
				}
				
				final ItemData selected = Item.get(inventory.getSelected());
				if (rmb && inventory.getSelected() != Item.AIR) {

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
								
							} else {
								if (tile != null/* && (tile.getWalls() & facing) != 0*/) {
									selected.doAction(tile, facingIndex, chunkPtr);
								} else {
									selected.doAction(null, facingIndex, chunkPtr);
								}
							}
							
							return;
						}
						
						if (tile != null && (tile.getWalls() & facing) != 0) {
							//TODO: CRASHES
							//EntityHandler.addEntity(new ItemEntity(getTileDropPos(selectionPt), tile.getMaterial(facingIndex).getDrop(), 1));
						}
						
						byte wallFlags = cameraFacing;
						byte slopeFlags = 0;
						if (slopeSetting == 1) {
							wallFlags = 0;
							slopeFlags = cameraFacing;
						} else if (slopeSetting == 2) {
							// TODO: This
						}
						
						byte specialFlags = mat.getInitialFlags();
						if (mat.isTiling()) {
							final float rx = (_x * TILE_SIZE) + cx;
							final float ry = (_y * TILE_SIZE);
							final float rz = (_z * TILE_SIZE) + cz;
							dx = ((cameraFacing & 3) == 0) ? TILE_SIZE : 0;
							dz = TILE_SIZE - dx;
							if ((cameraFacing & 1) != 0) dz *= -1;
							if ((cameraFacing & 32) != 0) dx *= -1;
							
							chunkPtr.setTile(_x, _y, _z, wallFlags, slopeFlags, mat, (byte) 0);
							tile = chunkPtr.getBuilding().get(_x,_y,_z);
							Material.setTilingFlags(tile, terrain, rx, ry, rz, dx, dz, mat, facingIndex, 0);
							chunkPtr.getBuilding().buildModel();
						} else {
							chunkPtr.setTile(_x, _y, _z, wallFlags, slopeFlags, mat, specialFlags);
						}
						
						if ((wallFlags & 12) != 0 && Math.abs(chunkPtr.heightLookup(_x, _z) - _y) <= .1f) {
							chunkPtr.setHeight(_x, _z, _y - .025f);
						}
	
						inventory.consume(inventory.getSelectionPos());
						chunkPtr.rebuildWalls();
					}
				}
			}
		} else {
			if (Input.isPressed(Input.KEY_LMB) && inventory.getSelected() != Item.AIR && Item.get(inventory.getSelected()).getMaterial() == Material.NONE) {
				player.getSource().play(Resources.getSound("swing"));
			}
		}
		
		ui.update();
		inventory.update();
		
	}

	private Vector3f getTileDropPos(Vector3f pos) {
		return new Vector3f((int)pos.x + .5f, (int)pos.y + .5f, (int)pos.z + .5f);
	}

	@Override
	public void tick() {
		EntityHandler.tick(enviroment.getTerrain());
		
		if (ui.isPaused()) {
			return;
		}
		
		enviroment.tick(this);
	}

	@Override
	public void cleanUp() {
		ui.cleanUp();
		EntityHandler.clearEntities();
		enviroment.cleanUp();
		
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
	public void render() {
		if (returnToMenu) return;
		
		enviroment.render(camera, selectionPt, cameraFacing);
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

	public void setTileShape(int wall, int slope) {
		this.slopeSetting = (byte)slope;
		this.wallSetting = (byte)wall;
	}
}
