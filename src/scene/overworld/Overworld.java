package scene.overworld;

import static map.building.Tile.TILE_SIZE;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Application;
import core.Globals;
import dev.Debug;
import dev.tracers.LineRenderer;
import gl.Camera;
import gl.Window;
import gl.skybox.Skybox;
import io.Controls;
import io.Input;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import map.building.Material;
import map.building.Tile;
import scene.MainMenu;
import scene.Scene;
import scene.entity.EntityControl;
import scene.entity.TestEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;

public class Overworld implements Scene {
	
	private Skybox skybox;
	
	private Enviroment enviroment;
	private Camera camera;
	private Inventory inventory;
	
	private PlayerEntity player;
		
	private final OverworldUI ui;

	protected boolean returnToMenu;
	
	public Overworld() {
		camera = new Camera();
		skybox = new Skybox();
		enviroment = new Enviroment(this);
		inventory = new Inventory();
		
		ui = new OverworldUI(this);
		
		EntityControl.init();
		LineRenderer.init();
		
		player = new PlayerEntity(this);
		camera.focusOn(player);
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.setPosition(new Vector3f((Chunk.CHUNK_SIZE)/2f, 0, (Chunk.CHUNK_SIZE)/2f));
		camera.grabMouse();
		player.position.set(camera.getPosition());
		
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
		
		camera.move();
		enviroment.update(this);
		inventory.update();
		EntityControl.update();
		
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		Vector3f buildRay = null;
		Vector3f terrainRay = null;
		if (inventory.getSelected().getMaterial() != Material.NONE) {
			buildRay = enviroment.getTerrain().buildingRaycast(camera.getPosition(), camera.getDirectionVector(), Chunk.POLYGON_SIZE*3);
		}
		
		terrainRay = enviroment.getTerrain().terrainRaycast(camera.getPosition(), camera.getDirectionVector(), Chunk.POLYGON_SIZE*3);
		
		if (Input.isPressed(Input.KEY_LMB)) {
			if (inventory.getSelected() != Item.SHOVEL) {
				if (buildRay != null) {
					
					Chunk chunkPtr = enviroment.getTerrain().getChunkAt(buildRay.x, buildRay.z);
					int cx = chunkPtr.x*Chunk.CHUNK_SIZE;
					int cz = chunkPtr.z*Chunk.CHUNK_SIZE;
					chunkPtr.destroy((int)((buildRay.x - cx)/TILE_SIZE),
							(int)(buildRay.y/TILE_SIZE),
							(int)((buildRay.z - cz)/TILE_SIZE),
							Tile.getFacingByte(camera, Input.isDown("sneak")), Material.NONE);
					chunkPtr.rebuildWalls();
				}
			}
			else {
				if (terrainRay != null) {
					float dx = camera.getPosition().x - terrainRay.x;
					float dy = camera.getPosition().y - terrainRay.y;
					float dz = camera.getPosition().z - terrainRay.z;
					
					if (dx*dx + dy*dy + dz*dz <= Chunk.POLYGON_SIZE*Chunk.POLYGON_SIZE * 9) {
						Terrain t = enviroment.getTerrain();
						Chunk c = t.getChunkAt(terrainRay.x, terrainRay.z);
						
						if (c != null) {
							int cx = c.x*Chunk.CHUNK_SIZE;
							int cz = c.z*Chunk.CHUNK_SIZE;
							
							if (inventory.getSelected() == Item.SHOVEL) {
								int relX = (int)(terrainRay.x - cx)/Chunk.POLYGON_SIZE;
								int relZ = (int)(terrainRay.z - cz)/Chunk.POLYGON_SIZE;
								if (!c.breakTile(relX, relZ)) {
									if (Input.isDown("sneak")) {
										c.smoothHeight(relX, relZ);
									} else {
										c.addHeight(relX, relZ, -Chunk.DIG_SIZE);
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (Input.isPressed(Input.KEY_RMB)) {
			if (inventory.getSelected() == Item.SHOVEL && terrainRay != null) {
				float dx = camera.getPosition().x - terrainRay.x;
				float dy = camera.getPosition().y - terrainRay.y;
				float dz = camera.getPosition().z - terrainRay.z;
				
				if (dx * dx + dy * dy + dz * dz <= Chunk.POLYGON_SIZE * Chunk.POLYGON_SIZE * 9) {
					Terrain t = enviroment.getTerrain();
					Chunk c = t.getChunkAt(terrainRay.x, terrainRay.z);
	
					if (c != null) {
						int cx = c.x * Chunk.CHUNK_SIZE;
						int cz = c.z * Chunk.CHUNK_SIZE;
						int relX = (int)(terrainRay.x - cx)/Chunk.POLYGON_SIZE;
						int relZ = (int)(terrainRay.z - cz)/Chunk.POLYGON_SIZE;
						
						if (Input.isDown("sneak")) {
							c.setHeight(relX, relZ, (int)terrainRay.y/Chunk.POLYGON_SIZE);
						} else {
							c.addHeight(relX, relZ, Chunk.DIG_SIZE);
						}
						
						c.breakTile(relX, relZ);
					}
				}
			} else if (inventory.getSelected().getMaterial() != Material.NONE && !(terrainRay == null && buildRay == null)) {
				if (buildRay == null) {
					buildRay = terrainRay;
					
				}
				Terrain t = enviroment.getTerrain();
				Chunk c = t.getChunkAt(buildRay.x, buildRay.z);
				
				if (c != null) {
					int cx = c.x * Chunk.CHUNK_SIZE;
					int cz = c.z * Chunk.CHUNK_SIZE;
					byte facing = Tile.getFacingByte(camera, Input.isDown("sneak"));
					//c.setHeight((int) ((buildRay.x - cx) / Chunk.POLYGON_SIZE), (int) ((buildRay.z - cz) / Chunk.POLYGON_SIZE), (float)Math.round(buildRay.y/TILE_SIZE)*TILE_SIZE);
					int _x = (int)Math.floor((buildRay.x-cx)/TILE_SIZE);
					int _y = (int)Math.round((buildRay.y)/TILE_SIZE);
					int _z = (int)Math.floor((buildRay.z-cz)/TILE_SIZE);
					
					Tile tile = c.getBuilding().get(_x,_y,_z);
	
					
					if (tile != null && (tile.getWalls() & facing) != 0) {
						float len = -Vector3f.sub(buildRay, camera.getPosition()).length();
						
						float rx = camera.getPosition().x + (camera.getDirectionVector().x*len);
						float ry = camera.getPosition().y + (camera.getDirectionVector().y*len);
						float rz = camera.getPosition().z + (camera.getDirectionVector().z*len);
						
						float dx = Math.abs(rx % TILE_SIZE);
						float dy = Math.abs(ry % TILE_SIZE);
						float dz = Math.abs(rz % TILE_SIZE);
						
						//EntityControl.addEntity(new TestEntity(rx,camera.getPosition().y+(camera.getDirectionVector().y*len),rz));
						
						if (Input.isDown(Controls.get("sneak"))) {
							if (dx < dz) {
								if (dx < .5f - dz) {
									_x--;
								} else {
									_z++;
								}
							} else {
								if (dx < .5f - dz) {
									_z--;
								} else {
									_x++;
								}
							}
						} else {
							_y++;
							/*boolean isXAligned = (tile.getWalls() & 3) != 0;
							float dxz = (isXAligned) ? dx : dz;
							if (dxz < dy) {
								if (dx < .5f - dy) {
									_z -= isXAligned ? 1 : 0;
									_x -= isXAligned ? 0 : 1;
								} else {
									_y++;
								}
							} else {
								if (dxz < .5f - dy) {
									_z += isXAligned ? 1 : 0;
									_x += isXAligned ? 0 : 1;
								} else {
									_y--;
								}
							}*/
						}
					}
					
					if (_x < 0) {
						c = t.get(c.arrX-1, c.arrZ);
						_x += Chunk.VERTEX_COUNT*2;
					} else if (_x > Chunk.VERTEX_COUNT*2) {
						c = t.get(c.arrX+1, c.arrZ);
						_x -= Chunk.VERTEX_COUNT*2;
					}
					
					if (_z < 0) {
						c = t.get(c.arrX, c.arrZ-1);
						_z += Chunk.VERTEX_COUNT*2;
					} else if (_z > Chunk.VERTEX_COUNT*2) {
						c = t.get(c.arrX, c.arrZ+1);
						_z += Chunk.VERTEX_COUNT*2;
					}
					c.build(_x, _y, _z, facing, inventory.getSelected().getMaterial());
					c.rebuildWalls();
				}
			}
		}
		
		if (Globals.debugMode) {
			Debug.uiDebugInfo(this);
		
		}
		
		ui.update();
	}

	@Override
	public void cleanUp() {
		ui.cleanUp();
		EntityControl.clearEntities();
		enviroment.cleanUp();
		
		skybox.cleanUp();
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

	public Vector3f getSunVector() {
		// TODO Auto-generated method stub
		return new Vector3f(0,1,0);
	}

	@Override
	public void render(float px, float py, float pz, float pw) {
		if (returnToMenu) return;
		Vector3f weatherColor = enviroment.getWeather().determineSkyColor();
		
		
		skybox.render(camera, Enviroment.time, enviroment.getClosestBiome().getSkyColor(), weatherColor);
		enviroment.render(camera, px, py, pz, pw);
		EntityControl.render(camera, this.getSunVector());
		
		LineRenderer.render(camera);
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
}
