package dev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.joml.Vector3f;

import audio.AudioHandler;
import core.Application;
import core.Globals;
import gl.Camera;
import map.Enviroment;
import scene.overworld.Overworld;

public class CommandMethods {
	public static void logMessage(String msg) {
		Console.log(msg);
	}
	
	public static void volume(float value) {
		Globals.volume = value;
		AudioHandler.changeMasterVolume();

	}
	
	public static void run(String file) {
		try {
			List<String> lines = Files.readAllLines(new File("cmds/" + file + ".txt").toPath());
			for(String line : lines) {
				Console.send(line);
			}
		} catch (IOException e) {
			Console.log("Could not read file: "+file);
		}
	}
	
	public static void noclip() {
		if (Application.scene.getCamera().getControlStyle() == Camera.FIRST_PERSON) {
			Application.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
	}
	
	public static void chunk_distance(int distance) {
		distance = Math.max(distance, 1);
		Globals.chunkRenderDist = distance;
		if (Application.scene instanceof Overworld) {
			Overworld scene = ((Overworld)Application.scene);
			scene.getEnviroment().reloadTerrain();
		}
	}
	
	public static void time(String action, float value) {
		if (!(Application.scene instanceof Overworld)) {
			Console.log("Cannot use this command outside gameplay");
			return;
		}
		
		Overworld overworld = (Overworld) Application.scene;
		Enviroment e = overworld.getEnviroment();
		
		if (action.equals("set")) {
			e.setTime((int)value);
		}
		else if (action.equals("add")) {
			e.setTime((int)(e.getTime() + value));
		}
		else if (action.equals("freeze")) {
			e.toggleTime();
		}
		else {
			incorrectParams("time", "set/add/freeze", "value");
		}
	}
	
	public static void fov(int fov) {
		Globals.fov = fov;
		Application.scene.getCamera().updateProjection();
	}

	public static void quit() {
		Application.close();
	}
	
	public static void exit() {
		Application.close();
	}
	
	private static void incorrectParams(String cmd, String ... strings) {
		String s = "Usage: "+cmd+" ";
		for(int i = 0; i < strings.length; i++) {
			s += "<"+strings[i]+"> ";
		}
		Console.log(s);
	}
}
