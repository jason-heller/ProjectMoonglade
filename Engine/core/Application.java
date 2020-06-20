package core;


import org.lwjgl.opengl.Display;

import audio.AudioHandler;
import dev.Console;
import dev.io.ObjToTilConverter;
import gl.Render;
import gl.Window;
import io.Controls;
import io.Input;
import io.Settings;
import scene.MainMenu;
import scene.Scene;
import ui.UI;

public class Application {
	
	public static final String VERSION = "0.1.1";
	
	public static Scene scene;
	private static Class<?> nextScene;

	private static float tickTimer = 0f;
	private static boolean forceClose;
	private static boolean inLoadingState;
	public static boolean paused = false;
	public static final int TICKS_PER_SECOND = 25;
	public static final float TICKRATE = 1f / TICKS_PER_SECOND;
	
	public static void changeScene(Class<?> sceneClass) {
		scene.cleanUp();
		UI.clear();
		nextScene = sceneClass;
	}

	public static void close() {
		forceClose = true;
	}

	private static void handleSceneLoad() {
		if (!inLoadingState) {
			// Window.update();
			inLoadingState = true;
		} else {
			scene.load();
			inLoadingState = false;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		AudioHandler.init();
		Settings.init();
		Controls.init();
		Window.create();
		Render.init();
		Console.init();

		Window.update();

		scene = new MainMenu();

		for (final String arg : args) {
			if (arg.toLowerCase().contains("tile.txt")) {
				ObjToTilConverter.tileTileParser(arg);
			} else {
				Console.send(arg);
			}
		}

		while (!Display.isCloseRequested() && !forceClose) {
			if (scene.isLoading()) {
				handleSceneLoad();
				continue;
			}
			
			Window.update();
			
			scene.update();
			
			if (nextScene != null) {
				try {
					scene = (Scene) nextScene.newInstance();
				} catch (final InstantiationException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}

				nextScene = null;
			} else {
				tickTimer += Window.deltaTime;
				if (tickTimer >= TICKRATE) {
					tickTimer -= TICKRATE;
					Input.poll();
					UI.update();
					
					Console.update();
					scene.tick();
					AudioHandler.update(scene.getCamera());
					
				}
				
				Render.render(scene);

				Render.postRender(scene);
			}
		}

		scene.cleanUp();
		Render.cleanUp();

		// Thread.sleep(50);
		Resources.cleanUp();
		AudioHandler.cleanUp();
		Window.destroy();
		Settings.save();
		Controls.save();
		System.exit(0);
	}
}
