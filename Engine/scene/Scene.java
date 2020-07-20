package scene;

import gl.Camera;

public interface Scene {
	public void load();
	public void update();
	public void tick();
	public void cleanUp();
	public Camera getCamera();
	public boolean isLoading();
	public void render();
	public boolean hasHolds();		// If a scene has a hold, it cannot be forced to close/change
	public void onSceneEnd();
}
