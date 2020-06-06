package dev;

import static dev.CommandType.GETTER;
import static dev.CommandType.SETTER;
import static dev.CommandType.METHOD;

import core.Globals;
import gl.Camera;
import gl.Window;
import io.Controls;
import map.Enviroment;
import map.weather.Weather;

enum CommandData {
	// Methods
	quit(false),
	exit(false),
	noclip(true),
	volume(false),
	time(true, "set/add/freeze", "float"),
	run(false),
	bind(Controls.class, false, "key", "action"),
	chunk_distance(false),
	tp(true),
	weather(true, "set/add/freeze", "float"),
	give(true, "id/item", "amount"),
	fps(false),
	
	// Getters
	version("VERSION", Globals.class, GETTER, false),
	
	// Setters
	timescale("timeScale", Window.class, SETTER, false),
	debug("debugMode", Globals.class, SETTER, false),
	cam_speed("cameraSpeed", Camera.class, SETTER, true),
	time_speed("timeSpeed", Enviroment.class, SETTER, false),
	terrain_wireframe("terrainWireframe", Debug.class, SETTER, true);
	
	Command command;
	
	private CommandData(boolean cheats) {
		command = new Command(name(), cheats);
	}
	
	private CommandData(boolean cheats, String ... paramNames) {
		command = new Command(name(), cheats, paramNames);
	}
	
	private CommandData(Class<?> varLoc, boolean cheats, String ... paramNames) {
		command = new Command(name(), varLoc, cheats, paramNames);
	}
	
	private CommandData(String varName, Class<?> varLoc, CommandType type, boolean cheats, String ...paramNames) {
		command = new Command(name(), varName, varLoc, type, cheats, paramNames);
	}
	
	public static Command getCommand(String name) {
		for(CommandData command : values() ) {
			Command c = command.command;
			if (c.getName().equals(name)) {
				return c;
			}
		}
		
		return null;
	}
}
