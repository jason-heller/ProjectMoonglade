package core;


import org.lwjgl.opengl.Display;

import audio.AudioHandler;
import dev.Console;
import gl.Render;
import io.Controls;
import io.Input;
import scene.MainMenu;
import scene.Scene;
import ui.UI;

public class Application {
	public static Scene scene;
	private static Class<?> nextScene;

	private static float tickTimer = 0f;
	private static boolean forceClose;
	private static boolean inLoadingState;
	public static boolean paused = false;
	public static final int TICKS_PER_SECOND = 120;
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
		Settings.init();
		Controls.init();
		Window.create();
		AudioHandler.init();
		Render.init();
		Console.init();

		Window.update();

		scene = new MainMenu();

		for (final String arg : args) {
			Console.send(arg);
		}

		while (!Display.isCloseRequested() && !forceClose) {
			if (scene.isLoading()) {
				handleSceneLoad();
				continue;
			}

			Window.update();
			tickTimer += Window.deltaTime;
			if (tickTimer >= TICKRATE) {
				tickTimer -= TICKRATE;

				scene.update();
				Render.render(scene);

				Input.poll();
				Console.update();
				AudioHandler.update(scene.getCamera());
				
				Render.postRender(scene);

				if (nextScene != null) {
					try {
						scene = (Scene) nextScene.newInstance();
					} catch (final InstantiationException e) {
						e.printStackTrace();
					} catch (final IllegalAccessException e) {
						e.printStackTrace();
					}

					nextScene = null;
				}

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
