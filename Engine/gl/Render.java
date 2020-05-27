package gl;

import org.lwjgl.opengl.GL11;

import core.Resources;
import gl.fbo.FboUtils;
import gl.fbo.FrameBuffer;
import gl.particle.ParticleHandler;
import gl.water.WaterRender;
import map.Enviroment;
import scene.Scene;
import scene.overworld.Overworld;
import ui.UI;

public class Render {
	private static FrameBuffer screenMultisampled;
	public static FrameBuffer screen;

	public static FrameBuffer reflection;
	public static FrameBuffer refraction;
	private static int waterQuality = 3;
	public static float waterLevel = -64;
	
	private static WaterRender waterRender;
	
	public static void cleanUp() {
		Resources.cleanUp();
		waterRender.cleanUp();
		//EntityControl.cleanUp();
		UI.cleanUp();
		ParticleHandler.cleanUp();
		//PostProcessing.cleanUp();
		screenMultisampled.cleanUp();
	}

	public static void init() {
		//EntityControl.init();
		UI.init();
		ParticleHandler.init();

		screen = new FrameBuffer(1280, 720, true, true, false, false, 1);
		screenMultisampled = new FrameBuffer(1280, 720, true, true, false, false, 1);
		
		reflection = FboUtils.createTextureFbo(640, 360);
		refraction = FboUtils.createTextureFbo(640, 360);
		//PostProcessing.init();
		
		waterRender = new WaterRender();

		Resources.addTexture("skybox", "default.png");
		Resources.addTexture("default", "default.png");
		Resources.addTexture("none", "flat.png");
		Resources.addModel("cube", "cube.mod", true);
		Resources.addSound("click", "lighter_click.ogg");

		initGuiTextures();
	}

	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
		Resources.addTexture("loading_screen", "gui/loading_screen.png");
	}

	public static void postRender(Scene scene) {
		/*screenMultisampled.unbind();
		FboUtils.resolve(screenMultisampled);
		
		if (PostProcessing.getNumActiveShaders() != 0) {
			FboUtils.resolve(GL30.GL_COLOR_ATTACHMENT0, screenMultisampled, screen);
			//PostProcessing.render();
		} else {
			FboUtils.resolve(screenMultisampled);
		}
		PostProcessing.render();*/
		UI.render(scene);
	}

	public static void render(Scene scene) {
		Camera camera = scene.getCamera();
		ParticleHandler.update(scene.getCamera());
		renderRefractions(scene, camera);
		renderReflections(scene, camera);
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		scene.render(0, 1, 0, -100000);
		if (scene instanceof Overworld) {
			Enviroment e = ((Overworld)scene).getEnviroment();
			waterRender.render(camera, e);
		}
		ParticleHandler.render(scene.getCamera());
		
		//screenMultisampled.bind();
	}

	private static void renderReflections(Scene scene, Camera camera) {
		float pitch = camera.getPitch();
		float offset = (camera.getPosition().y-waterLevel )*2;
		
		
		reflection.bind();
		camera.setPitch(-pitch);
		camera.getPosition().y -= offset;
		camera.updateViewMatrix();
		
		if (waterQuality  > 1) {
			
			scene.render(0, -1, 0, 0);
			if (waterQuality > 2) {
				//Terrain.render(camera, 0,1,0,-waterLevel);
				//ObjectControl.render(camera, 0,1,0,-waterLevel);
				//EntityRenderer.render(camera, 0,1,0,-waterLevel);
				//AnimatedModelRenderer.render(entities, camera, lightDir);
			}
		} else {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
		
		reflection.unbind();
		camera.setPitch(pitch);
		camera.getPosition().y += offset;
		camera.updateViewMatrix();
	}

	private static void renderRefractions(Scene scene, Camera camera) {
		refraction.bind();
		
		//Terrain.render(camera, 0, -1, 0, waterLevel);
		if (waterQuality > 0) {
			scene.render(0, 1, 0, 0);

			if (waterQuality > 2) {
				//ObjectControl.render(camera, 0, -1, 0, -waterLevel);
				//EntityRenderer.render(camera, 0, -1, 0, -waterLevel);
			}
			
		} else {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
		refraction.unbind();
	}
}
