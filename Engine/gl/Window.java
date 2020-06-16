package gl;


import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import core.Application;

public class Window {
	private static long lastFrameTime;
	private static float aspectRatio;
	private static long lastFramerate;

	public static float deltaTime;
	private static int frameTime;
	public static float framerate;
	public static float timeScale = 1f;
	
	public static int maxFramerate = 120;
	public static boolean hasBorder = false;
	
	public static int viewportWidth, viewportHeight;
	public static boolean fullscreen;
	public static int displayWidth = 1920;
	public static int displayHeight = 1080;
	public static String windowTitle = "";

	/**
	 * Creates the display for the game
	 */
	public static void create() {
		// Globals.displayWidth = Settings.getInt("display width");
		// Globals.displayHeight = Settings.getInt("display height");
		// Globals.maxFramerate = Settings.getInt("target fps");
		// Globals.fov = Settings.getInt("fov");

		try {
			System.setProperty("org.lwjgl.opengl.Window.undecorated", hasBorder ? "true" : "false");
			ContextAttribs attribs = new ContextAttribs(3,3).withProfileCore(true).withForwardCompatible(true);
			Display.create(new PixelFormat(), attribs);
			Display.setTitle(windowTitle);
			Display.setInitialBackground(1, 1, 1);
			Display.setVSyncEnabled(false);
			// Display.setLocation(0, 0);
			// Display.setResizable(true);
			setDisplayMode(Window.getWidth(), Window.getHeight(), fullscreen);

			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (final LWJGLException e) {
			e.printStackTrace();
			System.err.println("Couldn't create display!");
			System.exit(-1);
		}
		GL11.glViewport(0, 0, viewportWidth, viewportHeight);
		lastFrameTime = getCurrentTime();
		lastFramerate = lastFrameTime;
	}

	/**
	 * Closes the game's display
	 */
	public static void destroy() {
		Display.destroy();
	}

	public static float getAspectRatio() {
		return aspectRatio;
	}

	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

	public static DisplayMode[] getDisplayModes() {
		try {
			final DisplayMode[] modes = Display.getAvailableDisplayModes();
			final List<DisplayMode> prunedModes = new ArrayList<DisplayMode>();
			prunedModes.add(Display.getDesktopDisplayMode());
			for (final DisplayMode m : modes) {
				if (m.getWidth() < 640 || m.getHeight() < 480) {
					continue;
				}
				final float ratio = m.getWidth() / (float) m.getHeight();
				if (ratio != 4f / 3f && ratio != 16f / 9f) {
					continue;
				}
				boolean add = true;
				for (final DisplayMode m2 : prunedModes) {
					if (m2.getWidth() == m.getWidth() && m2.getHeight() == m.getHeight()) {
						add = false;
						break;
					}
				}

				if (add) {
					prunedModes.add(m);
				}
			}

			return prunedModes.toArray(new DisplayMode[0]);
		} catch (final LWJGLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static int getHeight() {
		return Display.getDisplayMode().getHeight();
	}

	public static int getWidth() {
		return Display.getDisplayMode().getWidth();
	}

	public static void refresh() {
		deltaTime = 0;
		lastFrameTime = getCurrentTime();
		if (Display.wasResized()) {
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		}

		// Update Display
		Display.sync(maxFramerate);
		Display.update();
	}

	public static void resize(int width, int height) {
		try {
			final DisplayMode targetDisplayMode = new DisplayMode(width, height);
			Display.setDisplayMode(targetDisplayMode);
			aspectRatio = width / (float) height;
		} catch (final LWJGLException e) {
			System.out.println("Unable to setup mode [" + width + "," + height + "]");
		}
	}

	public static void setDisplayMode(DisplayMode displayMode) {
		try {
			Display.setDisplayMode(displayMode);
		} catch (final LWJGLException e) {
			e.printStackTrace();
		}
	}

	public static void setDisplayMode(int width, int height, boolean fullscreen) {
		// return if requested DisplayMode is already set
		if (Display.getDisplayMode().getWidth() == width && Display.getDisplayMode().getHeight() == height
				&& Display.isFullscreen() == fullscreen) {
			return;
		}

		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				final DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (final DisplayMode current : modes) {
					if (current.getWidth() == width && current.getHeight() == height) {
						if (targetDisplayMode == null || current.getFrequency() >= freq) {
							if (targetDisplayMode == null
									|| current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel()) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if (current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()
								&& current.getFrequency() == Display.getDesktopDisplayMode().getFrequency()) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);

		} catch (final LWJGLException e) {
			System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
		}
	}

	public static void setFullscreen(boolean isFullscreen) {
		if (Display.isFullscreen() == isFullscreen) {
			return;
		}

		try {
			Display.setFullscreen(isFullscreen);
		} catch (final LWJGLException e) {
			System.out.println("Unable to setup mode [fs=" + isFullscreen + "]");
		}
	}

	public static void update() {
		if (Display.wasResized()) {
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		}

		// Update Display
		Display.update();
		Display.sync(maxFramerate);

		// Get delta time
		final long currentFrameTime = getCurrentTime();
		deltaTime = (currentFrameTime - lastFrameTime) / 1000f * timeScale;
		lastFrameTime = currentFrameTime;

		// Get framerate
		if (currentFrameTime - lastFramerate > 1000) {
			framerate = frameTime;
			frameTime = 0;
			lastFramerate += 1000;
		}
		frameTime++;
	}
	
	public static void setBorder(boolean border) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", border ? "true" : "false");
		Window.setDisplayMode(Display.getDisplayMode());
	}
}
