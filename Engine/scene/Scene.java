package scene;

import gl.Camera;

public interface Scene {
	public void load();
	public void update();
	public void tick();
	public void cleanUp();
	public Camera getCamera();
	public boolean isLoading();
	public void render(float px, float py, float pz, float pw);
}
