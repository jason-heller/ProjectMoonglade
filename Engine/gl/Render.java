package gl;

import org.lwjgl.opengl.GL11;

import core.Resources;
import gl.fbo.FrameBuffer;
import gl.particle.ParticleHandler;
import gl.post.PostProcessing;
import gl.water.WaterRender;
import map.Enviroment;
import scene.Scene;
import scene.overworld.Overworld;
import ui.UI;

public class Render {
	//private static FrameBuffer screenMultisampled;
	public static FrameBuffer screen;
	
	private static WaterRender waterRender;
	
	public static void cleanUp() {
		Resources.cleanUp();
		waterRender.cleanUp();
		//EntityControl.cleanUp();
		UI.cleanUp();
		ParticleHandler.cleanUp();
		PostProcessing.cleanUp();
		//screenMultisampled.cleanUp();
	}

	public static void init() {
		//EntityControl.init();
		UI.init();
		ParticleHandler.init();

		screen = new FrameBuffer(1280, 720, true, true, false, false, 1);
		//screenMultisampled = new FrameBuffer(1280, 720, true, true, false, false, 1);
		
		PostProcessing.init();
		
		waterRender = new WaterRender();

		Resources.addTexture("skybox", "default.png");
		Resources.addTexture("default", "default.png");
		Resources.addTexture("none", "flat.png");
		Resources.addObjModel("cube", "cube.obj", true);
		Resources.addSound("click", "lighter_click.ogg");

		initGuiTextures();
	}

	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
	}

	public static void postRender(Scene scene) {
	screen.unbind();
		//FboUtils.resolve(screen);
		if (PostProcessing.getNumActiveShaders() != 0) {
			PostProcessing.render();
		}
		UI.render(scene);
	}

	public static void render(Scene scene) {
		Camera camera = scene.getCamera();
		ParticleHandler.update(scene.getCamera());
		screen.bind();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		scene.render();
		if (scene instanceof Overworld) {
			Overworld ow = ((Overworld)scene);
			Enviroment e = ow.getEnviroment();
			waterRender.render(camera, e);
			
			ow.getInventory().render(camera, e.getLightDirection());
		}
		ParticleHandler.render(scene.getCamera());
		
	}
}
