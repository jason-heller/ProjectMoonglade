package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import dev.Console;

public class Controls {

	public static File controlsFile = new File(FileUtils.SETTINGS_FOLDER + "/controls.ini");

	public static Map<String, Integer> controls = new LinkedHashMap<String, Integer>();
	public static Map<Integer, String> customBinds = new LinkedHashMap<Integer, String>();

	public static void bind(String keyName, String cmd) {
		for (final String id : controls.keySet()) {
			if (id.equals(keyName.toLowerCase())) {
				controls.put(id, Keyboard.getKeyIndex(cmd.toUpperCase()));
				return;
			}
		}

		final int key = Keyboard.getKeyIndex(keyName.toUpperCase());
		if (key == Keyboard.KEY_NONE) {
			return;
		}
		customBinds.put(key, cmd);
		save();
	}

	public static void defaults() {
		controls.clear();
		controls.put("walk_forward", Keyboard.KEY_W);
		controls.put("walk_left", Keyboard.KEY_A);
		controls.put("walk_backward", Keyboard.KEY_S);
		controls.put("walk_right", Keyboard.KEY_D);
		controls.put("jump", Keyboard.KEY_SPACE);
		controls.put("sneak", Keyboard.KEY_LCONTROL);
		controls.put("use_backpack", Keyboard.KEY_E);
		controls.put("pause", Keyboard.KEY_ESCAPE);
		
		for(int i = 1; i <= 9; i++) {
			controls.put("item slot "+i, Keyboard.KEY_1 + (i-1));
		}
	}

	public static int get(String id) {
		return Console.isVisible() ? 0xFF : controls.get(id);
	}

	public static void handleCustomBinds(int input) {
		if (Console.isVisible()) {
			return;
		}

		for (final int key : customBinds.keySet()) {
			if (input != key) {
				continue;
			}

			final String[] cmds = customBinds.get(key).split(";");
			for (String s : cmds) {
				s = s.replaceAll("\'", "\"");
				Console.send(s);
			}

		}
	}

	public static void init() {
		defaults();
		if (Settings.configFile.exists()) {
			load();
		}
	}

	public static void listBinds() {
		Console.log("Predefined binds:");
		for (final String key : controls.keySet()) {
			Console.log(key + " \"" + Keyboard.getKeyName(controls.get(key)) + "\"");
		}
		Console.log("Custom binds:");
		for (final int key : customBinds.keySet()) {
			Console.log(Keyboard.getKeyName(key) + " \"" + customBinds.get(key) + "\"");
		}
	}

	public static void load() {
		try (BufferedReader br = new BufferedReader(new FileReader(controlsFile))) {
			for (String line; (line = br.readLine()) != null;) {
				final String[] data = line.split("=");
				if (data[0].contains("custom")) {
					final int key = Keyboard.getKeyIndex(data[0].split(":")[1]);
					customBinds.put(key, data[1]);
				} else {
					if (controls.containsKey(data[0])) {
						controls.put(data[0], Integer.parseInt(data[1]));
					}
				}
			}
		} catch (final FileNotFoundException e) {
			return;
		} catch (final IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}

	public static void save() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(controlsFile, false))) {
			for (final String line : controls.keySet()) {
				bw.write(line + "=" + controls.get(line) + "\n");
			}
			for (final Integer key : customBinds.keySet()) {
				bw.write("custom:" + Keyboard.getKeyName(key) + "=" + customBinds.get(key) + "\n");
			}
		} catch (final IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}

	public static void set(String id, int key) {
		controls.put(id, key);
	}

	public static int size() {
		return controls.keySet().size();
	}

	public static void unbind(String keyName) {
		final int key = Keyboard.getKeyIndex(keyName.toUpperCase());
		customBinds.remove(key);
		save();
	}
}
