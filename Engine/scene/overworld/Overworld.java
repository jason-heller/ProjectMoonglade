package scene.overworld;

import static map.tile.Tile.TILE_SIZE;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import core.Application;
import core.Resources;
import dev.Console;
import dev.Debug;
import dev.tracers.LineRender;
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
import scene.PlayableScene;
import scene.PlayableSceneUI;
import scene.entity.Entity;
import scene.entity.EntityData;
import scene.entity.EntityHandler;
import scene.entity.PlayerEntity;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.Item;
import scene.overworld.inventory.ItemData;
import ui.UI;
import util.Colors;

public class Overworld extends PlayableScene {
	
	public static final int PLAYER_REACH = Chunk.POLYGON_SIZE * 6;
	public static String worldName;
	public static String worldSeed;
	public static String worldFileName;

	private Enviroment enviroment;
	
	private final PlayableSceneUI ui;
	
	private float actionDelay = 0f;
	
	private int cx, cz, _x, _y, _z;
	private float dx, dz;
	
	public Overworld() {
		super();
		Enviroment.exactTime = 0f;
		
		SaveDataIO.readSaveData(this);
		
		OverworldResourcemanager.init();
		
		enviroment = new Enviroment(this);
		enviroment.tick(this);
		
		ui = new PlayableSceneUI(this);
		
		player = new PlayerEntity(this);
		camera.focusOn(player);
		player.position.set(camera.getPosition());
		EntityHandler.addEntity(player);
		
		load();
		
		if (Debug.structureMode) {
			LineRender.init();
			Enviroment.exactTime = Enviroment.DAY;
		}
	}

	@Override
	public void load() {
	}
	
	@Override
	public void update() {
		if (returnToMenu) {
			if (enviroment.getTerrain().getStreamer().isFinished()) {
				Application.changeScene(MainMenu.class);
			} else {
				enviroment.getTerrain().getStreamer().update();
				UI.drawRect(0, 0, 1280, 620, Colors.BLACK).setOpacity(.75f);
				UI.drawString("Saving..", 1280/2,720/2, true);
			}
			
			return;
		}
		
		if (player.isDisabled()) {
			camera.setControlStyle(Camera.NO_CONTROL);
			UI.drawRect(0, 300, 1280, 120, Colors.RED).setDepth(-999);
			UI.drawString("You're Dead!\nPress R to Respawn", 1280/2, 720/2, true).setDepth(-1000);
			if (Input.isPressed(Keyboard.KEY_R)) {
				player.setDisabled(false);
				player.heal(10);
				camera.setControlStyle(Camera.FIRST_PERSON);
				camera.focusOn(player);
			}
		}
		
		enviroment.update(this);
		
		super.update();
	
		if (Debug.debugMode) {
			Debug.uiDebugInfo(this);
		}
		
		boolean isFacingTile = false;
		
		// Check for terrain intersection (of pointer)
		TerrainIntersection terrainIntersection = null;
		if (!inventory.isOpen() && !ui.isPaused()) {
			terrainIntersection = enviroment.getTerrain().terrainRaycast(camera.getPosition(),
					camera.getDirectionVector(), PLAYER_REACH);
			if (terrainIntersection != null) {
				selectionPt = terrainIntersection.getPoint();
			}
				
			if (inventory.getSelected() != 0) {
				byte snapFlags = (byte) ((wallSetting == 1) ? 1 : 0);
				snapFlags = (byte) ((slopeSetting == 1) ? 2 : snapFlags);
				Vector3f pt = enviroment.getTerrain().buildingRaycast(this, camera.getPosition(), camera.getDirectionVector(), PLAYER_REACH, facing, snapFlags);
				if (pt != null && (selectionPt == null || Vector3f.distanceSquared(camera.getPosition(), pt) <
				Vector3f.distanceSquared(camera.getPosition(), selectionPt))) {
					selectionPt = pt;
					isFacingTile = true;
				}
			}
		}
		actionDelay = Math.max(actionDelay - Window.deltaTime, 0f);
		
		// Pointer interactions
		if (selectionPt != null) {
			
			exactSelectionPt = new Vector3f(selectionPt);
			selectionPt.set((float) Math.floor(selectionPt.x), (float) Math.floor(selectionPt.y), (float) Math.floor(selectionPt.z));
			selectionPt.y = Math.min(Math.max(selectionPt.y, BuildData.MIN_BUILD_HEIGHT+1), BuildData.MAX_BUILD_HEIGHT-1);
		}
		
		if (Input.isMouseGrabbed() && this.actionDelay == 0f) {
			final boolean lmb = Input.isPressed("attack");
			final boolean rmb = Input.isPressed("use");
			final ItemData selected = Item.get(inventory.getSelected());
			
			if (selectionPt != null) {
				final Terrain terrain = enviroment.getTerrain();
				Chunk chunkPtr = terrain.getChunkAt(selectionPt.x, selectionPt.z);
				cx = chunkPtr.realX;
				cz = chunkPtr.realZ;
				_x = (int) ((selectionPt.x - cx) / TILE_SIZE);
				_y = (int) (selectionPt.y / TILE_SIZE);
				_z = (int) ((selectionPt.z - cz) / TILE_SIZE);
				dx = camera.getPosition().x - selectionPt.x;
				dz = camera.getPosition().z - selectionPt.z;
				
				Tile tile = chunkPtr.getBuilding().get(_x, _y, _z);
				//byte facing = Tile.getFacingByte(camera, wallSetting == 1, slopeSetting == 1);
				int facingIndex = Tile.getFacingIndex(facing);
				
				if (this.slopeSetting != 0) {
					facingIndex = 6;
				}
				
				if (lmb) {
					actionDelay = .05f;
					lmbAction(chunkPtr, tile, selected, terrainIntersection, facingIndex, isFacingTile);
				}
				
				if (rmb && inventory.getSelected() != Item.AIR) {
					actionDelay = .05f;
					rmbAction(chunkPtr, tile, selected, terrainIntersection, facingIndex, isFacingTile);
				}
			} else {
				
				if (rmb && inventory.getSelected() != Item.AIR) {
					actionDelay = .05f;
					selected.doAction(this, terrainIntersection, null, null, Tile.getFacingIndex(facing), false);
				}
			}
		} 
		
		ui.update();
		inventory.update(ui);
	}

	private void rmbAction(Chunk chunkPtr, Tile tile, ItemData selected, TerrainIntersection terrainIntersection,
			int facingIndex, boolean isFacingTile) {
		
		final Terrain terrain = enviroment.getTerrain();
		
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
					float rotation = EntityHandler.entRotationFromFacing(facing);
					Entity entity = EntityData.instantiate(selected.getEntityId(), selectionPt, rotation);
					//entity.position.set(selectionPt);
					EntityHandler.addEntity(entity);
					
				} else {
					if (tile != null/* && (tile.getWalls() & facing) != 0*/) {
						selected.doAction(this, terrainIntersection, tile, chunkPtr, facingIndex, false);
					} else {
						selected.doAction(this, terrainIntersection, null, chunkPtr, facingIndex, false);
					}
				}
				
				return;
			}
			
			//if (tile != null && (tile.getWalls() & facing) != 0) {
				//TODO: CRASHES
				//EntityHandler.addEntity(new ItemEntity(getTileDropPos(selectionPt), tile.getMaterial(facingIndex).getDrop(), 1));
			//}
			
			byte wallFlags = facing;
			byte slopeFlags = 0;
			if (slopeSetting == 1) {
				wallFlags = 0;
				slopeFlags = facing;
			} else if (slopeSetting == 2) {
				// TODO: This
			}
			
			byte specialFlags = mat.getInitialFlags();
			if (mat.isTiling()) {
				final float rx = (_x) + cx;
				final float ry = (_y);
				final float rz = (_z) + cz;
				dx = ((facing & 3) == 0) ? TILE_SIZE : 0;
				dz = TILE_SIZE - dx;
				if ((facing & 1) != 0) dz *= -1;
				if ((facing & 32) != 0) dx *= -1;
				
				chunkPtr.setTile(_x, _y, _z, wallFlags, slopeFlags, mat, (byte) 0);
				tile = chunkPtr.getBuilding().get(_x,_y,_z);
				Material.setTilingFlags(tile, terrain, rx, ry, rz, dx, dz, mat, facingIndex, 0);
				chunkPtr.getBuilding().buildModel();
			} else {
				chunkPtr.setTile(_x, _y, _z, wallFlags, slopeFlags, mat, specialFlags);
			}

			inventory.consume(inventory.getSelectionPos());
			chunkPtr.rebuildWalls();
		}
	}

	private void lmbAction(Chunk chunkPtr, Tile tile, ItemData selected, TerrainIntersection terrainIntersection,
			int facingIndex, boolean facingTile) {

		// Breaking tiles (lmb + tile)
		if (tile != null && tile.getMaterial(facingIndex) != Material.NONE) {
			
			boolean performedAction = selected.doAction(this, terrainIntersection, tile, chunkPtr, facingIndex, true);
			
			if (performedAction) {
				if (tile.getMaterial(facingIndex).isTiling()) {
					final float rx = (_x) + cx;
					final float ry = (_y);
					final float rz = (_z) + cz;
					dx = ((facing & 3) == 0) ? 1 : 0;
					dz = 1 - dx;
					if ((facing & 1) != 0) dz *= -1;
					if ((facing & 32) != 0) dx *= -1;
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
		}
		
		// Hitting props (lmb + prop/nothing)
		if (!facingTile && chunkPtr != null) {
			StaticProp prop = Props.get(terrainIntersection.getProp());
			
			boolean performedAction = selected.doAction(this, terrainIntersection, tile, chunkPtr, facingIndex, true);
			
			if (prop != null && performedAction) {
				int tool = prop.getTool();
				if (tool == Item.AIR) {
					ParticleHandler.addBurst("materials", 0, 0, exactSelectionPt);
					chunkPtr.destroyProp(terrainIntersection.getPropX(), terrainIntersection.getPropZ());
					
				}
				
				if (prop.getMaterial() == Material.PLANKS) {
					
					StaticPropProperties props = chunkPtr.getChunkPropProperties(terrainIntersection.getPropX(), terrainIntersection.getPropZ());
					if (props != null && props.damage % 5 != 2) {
						props.damage--;
						
						for (int i = 0; i < 12; i++) {
							Vector3f pos = new Vector3f(exactSelectionPt);
							pos.y += prop.getBounds().y;
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
		OverworldResourcemanager.cleanUp();
		
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
		enviroment.render(camera, selectionPt, facing);
		EntityHandler.updateAndRender(this, camera, enviroment.getLightDirection());	
		camera.getPrevPosition().set(camera.getPosition());
		if (Debug.structureMode) {
			LineRender.render(camera);
		}
	}

	public Enviroment getEnviroment() {
		return enviroment;
	}

	public void setCamFacingByte(byte cameraFacing) {
		this.facing = cameraFacing;
	}

	public byte getCamFacingByte() {
		return this.facing;
	}

	@Override
	protected void onSceneEnd() {
		enviroment.getTerrain().cleanUp();
	}
}
