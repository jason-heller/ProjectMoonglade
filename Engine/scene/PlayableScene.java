package scene;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import gl.Camera;
import gl.Window;
import map.Chunk;
import map.tile.Tile;
import scene.entity.EntityHandler;
import scene.entity.PlayerEntity;
import scene.overworld.inventory.Inventory;
import scene.overworld.inventory.Item;

public abstract class PlayableScene implements Scene {

	protected Camera camera;
	protected Inventory inventory;
	protected PlayerEntity player;
	
	protected PlayableSceneUI ui;

	protected boolean returnToMenu;
	
	protected byte facing;
	protected byte slopeSetting = 0, wallSetting = 0;
	protected Vector3f selectionPt, exactSelectionPt;
	
	public PlayableScene() {
		EntityHandler.init();
		Item.init();
		
		camera = new Camera();
		camera.setControlStyle(Camera.FIRST_PERSON);
		camera.setPosition(new Vector3f((Chunk.CHUNK_SIZE)/2f, 0, (Chunk.CHUNK_SIZE)/2f));
		camera.grabMouse();
		
		inventory = new Inventory();
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public void update() {
		if (Mouse.isGrabbed()) {
			Mouse.setCursorPosition(Window.getWidth()/2, Window.getHeight()/2);
		}
		
		facing = Tile.getFacingByte(camera, wallSetting == 1, slopeSetting == 1);
		selectionPt = null;
		exactSelectionPt = null;
		
		camera.move();
	}

	public void setTileShape(int wall, int slope) {
		this.slopeSetting = (byte)slope;
		this.wallSetting = (byte)wall;
	}
	
	public Vector3f getSelectionPoint() {
		return selectionPt;
	}

	public Vector3f getExactSelectionPoint() {
		return exactSelectionPt;
	}

	public abstract void onSceneEnd();

	public PlayableSceneUI getUi() {
		return ui;
	}
}
