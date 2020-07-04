package dev;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import core.Application;
import io.Input;
import scene.entity.EntityData;
import scene.overworld.inventory.Item;
import ui.Font;
import ui.Image;
import ui.UI;
import util.Colors;

public class Console {
	private static final int VISIBLE_LINES = 32, MAX_LINES = 200;
	private static int lineCopyInd = -1;

	private static int x = 20, y = 20;

	private static final int BORDER_WIDTH = 2;
	private static final int HEADER_HEIGHT = 20;
	private static final int WIDTH = 640;

	private static List<String> log = new ArrayList<String>();
	private static List<String> predictions = new ArrayList<String>();
	private static Image backdrop = new Image("none", 0, 0);
	private static Image border = new Image("none", 0, 0);

	private static boolean visible = false;
	private static boolean blockComment = false;
	private static String input = "";

	private static boolean drag = false;
	private static int dragX = 0, lastX = 0;
	private static int dragY = 0, lastY = 0;

	private static final float FONT_SIZE = .15f;
	private static final int FONT_HEIGHT = (int) (20 * (FONT_SIZE / .3f));

	private static boolean playerWasAlreadyDisabled = false;

	private static final Vector3f BACKGROUND_COLOR = Colors.BLACK;
	private static final Vector3f BORDER_COLOR = Colors.GUI_BORDER_COLOR;

	private static int lineViewInd = 0;

	private static PrintStream outStream;

	public static void clear() {
		log.clear();
	}

	public static void doSceneTick() {
		if (visible) {
			visible = false;
			Application.scene.update();
			visible = true;
		}
	}

	public static List<String> getLog() {
		return log;
	}

	public static void init() {
		outStream = new PrintStream(System.out) {
			@Override
			public void print(String x) {
				log(x);
			}

			@Override
			public void println(String x) {
				log(x);
			}

		};

		System.setOut(outStream);
		// System.setErr(errStream);
	}

	public static boolean isVisible() {
		return visible;
	}

	public static void log(Object... x) {
		String s = "";

		for (int i = 0; i < x.length; i++) {
			s += x[i].toString() + (i == x.length - 1 ? "" : ", ");
		}

		log(s);
	}

	public static void log(Object text) {
		final String[] lines = text.toString().split("\n");
		for (final String line : lines) {
			log.add(line);

			if (log.size() > MAX_LINES) {
				log.remove(0);
			}

			if (log.size() >= VISIBLE_LINES - 1 && log.size() < MAX_LINES) {
				lineViewInd++;
			}
		}
	}

	private static boolean mouseOver(int mx, int my) {
		return mx > x && my > y && mx < x + WIDTH && my < y + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT;
	}

	private static void predict(String input) {
		predictions.clear();
		if (input.equals("") || input.split(" ").length == 0) {
			return;
		}
		for (final CommandData commandData : CommandData.values()) {
			Command command = commandData.command;
			if (command.getName().indexOf(input.split(" ")[0]) == 0) {
				predictions.add(command.getName() + " " + command.getArgs());

				if (predictions.size() > 8) {
					return;
				}
			}
		}
	}

	public static void printStackTrace(Exception e) {
		System.err.println(e.toString());
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (final StackTraceElement element : stackTraceElements) {
			final String[] lines = element.toString().split("\r\n");
			for (final String line : lines) {
				if (line.length() != 0) {
					System.err.println(line);
				}
			}
		}
	}

	public static void send(String string) {
		if (blockComment && string.contains("*/")) {
			blockComment = false;
			string = string.substring(string.indexOf("*/") + 2);
		}

		if (string.length() == 0) {
			return;
		}

		if (string.charAt(0) == '#' || blockComment) {
			return;
		}

		if (string.contains("/*")) {
			string = string.substring(0, string.indexOf("/*"));
			blockComment = true;
		}

		if (string.contains("//")) {
			string = string.substring(0, string.indexOf("//"));
		}

		if (string.length() == 0) {
			return;
		}

		// Get args
		String read = "";
		final List<String> getArgs = new ArrayList<String>();
		boolean insideQuotes = false;
		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);

			if (c == '\"') {
				insideQuotes = !insideQuotes;

				if (!insideQuotes) {
					getArgs.add(read);
					read = "";
				}
			}

			else if (c == ' ' && !insideQuotes) {
				if (read.length() > 0) {
					getArgs.add(read);
				}
				read = "";
			}

			else {
				read += c;
			}
		}

		if (read.length() > 0) {
			getArgs.add(read);
		}

		String[] strs = new String[getArgs.size()];
		strs = getArgs.toArray(strs);

		if (strs == null || strs.length == 0) {
			return;
		}

		final String command = strs[0];
		final String[] args = new String[strs.length - 1];
		if (strs.length > 1) {
			for (int i = 1; i < strs.length; i++) {
				args[i - 1] = strs[i];
			}
		}

		// Now check if you typed a command
		final Command cmd = CommandData.getCommand(command);
		if (cmd != null) {
			if (cmd.requiresCheats && Debug.debugMode || !cmd.requiresCheats) {
				cmd.execute(args);
			} else {
				log("Cheats must be enabled");
			}
			return;
		}

		log("No such command: " + command);
	}

	public static void toggle() {
		visible = !visible;
		lineCopyInd = -1;
		input = "";
		predictions.clear();

		playerWasAlreadyDisabled = false;// TODO: THIS

		if (visible) {
			Input.requestMouseRelease();
		} else {
			if (!playerWasAlreadyDisabled) {
			}
			Input.requestMouseGrab();
		}
	}

	public static void update() {
		if (visible) {
			final char[] keysIn = Input.getTypedKey();

			backdrop.setColor(BACKGROUND_COLOR);
			backdrop.x = x + BORDER_WIDTH;
			backdrop.y = y + HEADER_HEIGHT;
			backdrop.w = WIDTH;
			backdrop.h = (VISIBLE_LINES + 1) * FONT_HEIGHT;
			backdrop.setDepth(-9998);

			border.setColor(BORDER_COLOR);
			border.x = x;
			border.y = y;
			border.w = WIDTH + BORDER_WIDTH * 2;
			border.h = BORDER_WIDTH * 2 + HEADER_HEIGHT + (VISIBLE_LINES + 1) * FONT_HEIGHT;
			border.setDepth(-9997);

			for (final char in : keysIn) {
				if (in != '`') {
					if (in == '\b') {
						if (input.length() > 0) {
							input = input.substring(0, input.length() - 1);
						}
					} else {
						input += in;
					}

					predict(input);
				}
			}

			if (Input.isPressed(Keyboard.KEY_DOWN)) {
				lineCopyInd = Math.min(lineCopyInd + 1, predictions.size() - 1);
				if (lineCopyInd >= 0) {
					String pred = predictions.get(lineCopyInd);
					input = pred.substring(0, pred.indexOf(' '));
				} else {
					while (lineCopyInd < 0 && log.size() != 0) {
						input = log.get(log.size() + lineCopyInd);
						if (input.charAt(0) == ']' && input.length() > 1) {
							input = input.substring(1);
							predict(input);
							break;
						}

						lineCopyInd++;
					}

					if (lineCopyInd == 0) {
						input = "";
						predictions.clear();
					}
				}
			}
			
			if (Input.isPressed(Keyboard.KEY_TAB) && lineCopyInd == 1 && predictions.size() > 0) {
				input = predictions.get(lineCopyInd);
			} 

			if (Input.isPressed(Keyboard.KEY_UP)) {
				final int originalInd = lineCopyInd;
				lineCopyInd = Math.max(lineCopyInd - 1, -log.size());
				if (lineCopyInd >= 0 && predictions.size() > 0) {
					String pred = predictions.get(lineCopyInd);
					input = pred.substring(0, pred.indexOf(' '));
				} else {
					String newInput = "";
					while (lineCopyInd > -log.size()) {
						newInput = log.get(log.size() + lineCopyInd);
						if (newInput.charAt(0) == ']' && newInput.length() > 1) {
							break;
						}

						lineCopyInd--;
					}

					if (newInput.length() > 0 && newInput.charAt(0) == ']') {
						input = newInput.substring(1);
						predict(input);
					} else {
						lineCopyInd = originalInd;
					}
				}

			}

			if (Input.isPressed(Keyboard.KEY_HOME)) {
				lineViewInd = Math.max(log.size() - (VISIBLE_LINES - 1), 0);
			}

			if (Input.isPressed(Keyboard.KEY_END)) {
				lineViewInd = 0;
			}

			if (Input.isPressed(Keyboard.KEY_NEXT)) {
				lineViewInd = Math.min(lineViewInd + 8, Math.max(log.size() - (VISIBLE_LINES - 1), 0));
			}

			if (Input.isPressed(Keyboard.KEY_PRIOR)) {
				lineViewInd = Math.max(lineViewInd - 8, 0);
			}

			if (Input.isPressed(Keyboard.KEY_RETURN)) {
				log("]" + input);
				send(input);
				input = "";
				lineCopyInd = 0;
				predictions.clear();
			}

			UI.drawImage(border);
			UI.drawImage(backdrop);

			UI.drawString(Font.consoleFont, "Console", x + 2, y, .25f, 1280, false).setDepth(-9999);

			final int lineBottomViewInd = lineViewInd + VISIBLE_LINES - 1;
			for (int i = lineViewInd; i < log.size() && i < lineBottomViewInd; i++) {
				final int lineY = y + HEADER_HEIGHT + BORDER_WIDTH + (i - lineViewInd) * FONT_HEIGHT;
				UI.drawString(Font.consoleFont, log.get(i), x + BORDER_WIDTH * 2, lineY, FONT_SIZE, 1280, false)
						.setDepth(-9999);
			}

			int predWidth = 16;
			for (int i = 0; i < predictions.size(); i++) {
				predWidth = Math.max(predWidth, (int) (predictions.get(i).length() * (16 * (FONT_SIZE / .3f))));
			}

			UI.drawRect(x, y + HEADER_HEIGHT + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT, predWidth,
					predictions.size() * FONT_HEIGHT + BORDER_WIDTH, BORDER_COLOR).setDepth(-9998);
			for (int i = 0; i < predictions.size(); i++) {
				final int lineY = y + (VISIBLE_LINES + i + 3) * FONT_HEIGHT;

				final String color = lineCopyInd == i ? "#w" : "#s";

				UI.drawString(Font.consoleFont, color + predictions.get(i), x + BORDER_WIDTH * 2, lineY, FONT_SIZE,
						1280, false).setDepth(-9999);
			}

			final String blinker = System.currentTimeMillis() % 750 > 375 ? "|" : "";
			
			String[] inputSpace = input.split(" ");
			
			if (input.indexOf("give") == 0 && inputSpace.length >= 1  && inputSpace.length < 3) {
				predictEnum(inputSpace.length == 2 ? inputSpace[1] : "", Item.values());
			}
			
			UI.drawString(Font.consoleFont, ">" + input + blinker, x + BORDER_WIDTH * 2,
					y + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT, FONT_SIZE, 1280, false).setDepth(-99999);
		}

		final int mx = Input.getMouseX();
		final int my = Input.getMouseY();
		if (mouseOver(mx, my)) {

			if (visible && Input.isPressed(Input.KEY_LMB) && !drag) {
				drag = true;
				dragX = mx;
				dragY = my;
				lastX = x;
				lastY = y;
			}

			final int wheel = -Input.getMouseDWheel();
			final int speed = Input.isDown(Keyboard.KEY_LCONTROL) ? 8 : 1;

			if (wheel < 0) {
				lineViewInd = Math.max(lineViewInd - speed, 0);
			}
			if (wheel > 0) {
				lineViewInd = Math.min(lineViewInd + speed, Math.max(log.size() - (VISIBLE_LINES - 1), 0));
			}
		}

		if (Input.isReleased(Input.KEY_LMB) && drag) {
			drag = false;
		}

		if (drag) {
			x = lastX + Input.getMouseX() - dragX;
			y = lastY + Input.getMouseY() - dragY;
		}

		if (Input.isPressed(Keyboard.KEY_GRAVE)) {
			toggle();
		}
	}

	private static void predictEnum(String string, Enum<?>[] values) {
		List<String> preds = new ArrayList<String>();
		
		if (string.equals("")) {
			for(Enum<?> e : values) {
				preds.add(e.name().toLowerCase());
			}
		} else {
			for(Enum<?> e : values) {
				String name = e.name().toLowerCase();
				
				if (name.indexOf(string) == 0) {
					preds.add(name);
				}
			}
		}
		
		int boxY = (y + HEADER_HEIGHT + BORDER_WIDTH + (VISIBLE_LINES + 1) * FONT_HEIGHT) + predictions.size() * FONT_HEIGHT + BORDER_WIDTH;
		UI.drawRect(x+28, boxY, 70,
				preds.size()*10, BORDER_COLOR).setDepth(-9998);
		for (int i = 0; i < preds.size(); i++) {
			final int lineY = boxY + (i) * FONT_HEIGHT;

			UI.drawString(Font.consoleFont, "#s" + preds.get(i), x + 28 + BORDER_WIDTH * 2, lineY, FONT_SIZE,
					1280, false).setDepth(-9999);
		}
	}
}
