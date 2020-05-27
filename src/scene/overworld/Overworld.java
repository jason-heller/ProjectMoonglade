package scene.overworld;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import core.Globals;
import core.Window;
import dev.Console;
import dev.Debug;
import gl.Camera;
import gl.skybox.Skybox;
import io.Input;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import map.building.Tile;
import scene.Scene;
import scene.entity.EntityControl;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;
import ui.UI;
import util.HeightmapRaycaster;

public class Overworld implements Scene {
	
	private Skybox skybox;
	
	private Enviroment enviroment;
	private Camera camera;
	private Inventory inventory;
	
	private PlayerEntity player;
	
	public Overworld() {
		camera = new Camera();
		skybox = new Skybox();
		enviroment = new Enviroment(this);
		inventory = new Inventory();
		
		EntityControl.init();
		
		player = new PlayerEntity(this);
		camera.focusOn(player);
		camera.setControlStyle(Camera.FIRST_PERSON);
		float offset = Chunk.CHUNK_SIZE * Enviroment.chunkArrSize / 2;
		camera.setPosition(new Vector3f(offset,50,offset));
		camera.grabMouse();
		player.position.set(camera.getPosition());
		
		load();
	}

	@Override
	public void load() {
	}

	@Override
	public void update() {
		UI.drawString("+", 640, 360, true);
		
		camera.move();
		enviroment.update(this);
		inventory.update();
		EntityControl.update();
		
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		if (Input.isPressed(Input.KEY_LMB)) {
			Vector3f pt = null;
			if (inventory.getSelected() != Item.SHOVEL) {
				pt = enviroment.getTerrain().buildingRaycast(camera.getPosition(), camera.getDirectionVector(), Chunk.POLYGON_SIZE*3);
				if (pt != null) {
					
					Chunk chunkPtr = enviroment.getTerrain().getChunkAt(pt.x, pt.z);
					Console.log(pt, "AAAAAAAAAA");
					int cx = chunkPtr.x*Chunk.CHUNK_SIZE;
					int cz = chunkPtr.z*Chunk.CHUNK_SIZE;
					chunkPtr.destroy((int)((pt.x - cx)/Tile.TILE_SIZE),
							(int)(pt.y/Tile.TILE_SIZE),
							(int)((pt.z - cz)/Tile.TILE_SIZE),
							Tile.getFacingByte(camera, Input.isDown("sneak")), 0);
				}
			}
			else {
				pt = HeightmapRaycaster.raycast(camera, enviroment.getTerrain());
				if (pt != null) {
					float dx = camera.getPosition().x - pt.x;
					float dy = camera.getPosition().y - pt.y;
					float dz = camera.getPosition().z - pt.z;
					
					if (dx*dx + dy*dy + dz*dz <= Chunk.POLYGON_SIZE*Chunk.POLYGON_SIZE * 9) {
						Terrain t = enviroment.getTerrain();
						Chunk c = t.getChunkAt(pt.x, pt.z);
						
						if (c != null) {
							int cx = c.x*Chunk.CHUNK_SIZE;
							int cz = c.z*Chunk.CHUNK_SIZE;
							
							if (inventory.getSelected() == Item.SHOVEL) {
								if (Input.isDown("sneak")) {
									c.smoothHeight((int)(pt.x - cx)/Chunk.POLYGON_SIZE, (int)(pt.z - cz)/Chunk.POLYGON_SIZE);
								} else {
									c.addHeight((int)(pt.x - cx)/Chunk.POLYGON_SIZE, (int)(pt.z - cz)/Chunk.POLYGON_SIZE, -Chunk.DIG_SIZE);
								}
							}
						}
					}
				}
			}
		}
		
		if (Input.isPressed(Input.KEY_RMB)) {
			Vector3f pt = HeightmapRaycaster.raycast(camera, enviroment.getTerrain());
			
			if (pt != null) {
				float dx = camera.getPosition().x - pt.x;
				float dy = camera.getPosition().y - pt.y;
				float dz = camera.getPosition().z - pt.z;

				if (dx * dx + dy * dy + dz * dz <= Chunk.POLYGON_SIZE * Chunk.POLYGON_SIZE * 9) {
					Terrain t = enviroment.getTerrain();
					Chunk c = t.getChunkAt(pt.x, pt.z);

					if (c != null) {
						int cx = c.x * Chunk.CHUNK_SIZE;
						int cz = c.z * Chunk.CHUNK_SIZE;

						if (inventory.getSelected() == Item.SHOVEL) {
							if (Input.isDown("sneak")) {
								c.setHeight((int)(pt.x - cx)/Chunk.POLYGON_SIZE, (int)(pt.z - cz)/Chunk.POLYGON_SIZE, (int)pt.y/Chunk.POLYGON_SIZE);
							} else {
								c.addHeight((int) (pt.x - cx) / Chunk.POLYGON_SIZE, (int) (pt.z - cz) / Chunk.POLYGON_SIZE, Chunk.DIG_SIZE);
							}
						}
						else if (inventory.getSelected() != Item.AIR) {
							byte facing = Tile.getFacingByte(camera, Input.isDown("sneak"));
							//c.setHeight((int) ((pt.x - cx) / Chunk.POLYGON_SIZE), (int) ((pt.z - cz) / Chunk.POLYGON_SIZE), (int)pt.y/Chunk.POLYGON_SIZE);
							int _x = (int)((pt.x - cx)/Tile.TILE_SIZE);//Tile.TILE_SIZE
							int _y = (int)(pt.y/Tile.TILE_SIZE);
							int _z = (int)((pt.z - cz)/Tile.TILE_SIZE);
							
							c.build(_x, _y, _z, facing, 0);
						}
					}
				}
			}
		}
		
		if (Globals.debugMode) {
			Debug.uiDebugInfo(this);
		
		}
	}

	@Override
	public void cleanUp() {
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
		Vector3f weatherColor = enviroment.getWeather().determineSkyColor();
		
		skybox.render(camera, Enviroment.time, enviroment.getBiome().getSkyColor(), weatherColor);
		enviroment.render(camera, px, py, pz, pw);
	}

	public Enviroment getEnviroment() {
		return enviroment;
	}

	public PlayerEntity getPlayer() {
		return this.player;
	}
}
