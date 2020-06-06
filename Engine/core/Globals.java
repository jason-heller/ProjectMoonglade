package core;

import java.nio.file.Paths;

public class Globals {
	public static final String VERSION = "WIP Engine Build 2";

	// TODO: empty/get rid of this class
	
	/* Display related */
	public static int displayWidth = 1920;
	public static int displayHeight = 1080;
	public static int viewportWidth = 1280;
	public static int viewportHeight = 720;
	public static int maxFramerate = 120;
	public static String windowTitle = "yeet";

	/* Technical stuff */
	public static boolean debugMode = true;
	public static int fov = 90;

	public static float gravity = 10f;
	public static float maxGravity = -20f;
	public static float volume = 0.5f;
	public static boolean fullscreen = false;
	public static int maxParticles = 99;
	public static int chunkRenderDist = 7;
	public static int foliageRadius = 16;

	/* Lighting */
	public static int shadowResolution = 2048;

	/* File I/O */
	public static final String WORKING_DIRECTORY = Paths.get(".").toAbsolutePath().normalize().toString();
	public static final String SETTINGS_FOLDER = WORKING_DIRECTORY + "/" + "settings";

	/* Gui */
	public static final float guiWidth = 1280;
	public static final float guiHeight = 720;

	/* Settings */
	public static float mouseSensitivity = 1f;
	public static int fboSamplingAmt = 2;

}
