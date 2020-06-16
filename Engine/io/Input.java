package io;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import gl.Window;
import ui.UI;

public enum Input {

	INPUT;

	private static final float KEY_TIMEOUT = 1f;
	
	private static int mouseScreenPosX;
	private static int mouseScreenPosY;

	private static float mouseDX, mouseDY;
	private static int mouseDWheel;

	private static float backSpaceTimer = 0;
	public final static int KEY_WHEEL_UP = 10100;

	public final static int KEY_WHEEL_DOWN = 10200;

	public final static int KEY_LMB = 11001;
	public final static int KEY_RMB = 11002;
	public final static int KEY_MMB = 11003;
	public static final long DEFAULT_POLL_RATE = 1000 / 20;

	private static long pollRate = DEFAULT_POLL_RATE; // Poll 20 times per second
	private static long lastPoll = System.currentTimeMillis();

	private static int mouseGrabs;

	public static void clear() {
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			INPUT.keys[i] = 0;
			INPUT.states[i] = 0;
		}

		/*
		 * for(int i = 0; i < 3; i++) { INPUT.mouse[i] = 0; }
		 */
	}

	public static int getAny() {
		for (int j = 0; j < INPUT.MAX_KEYS; j++) {
			if (INPUT.states[j] == INPUT.PRESSED) {
				return INPUT.keys[j];
			}
		}

		return -1;
	}

	public static char getChar(int code) {
		final boolean shift = Input.isDown(Keyboard.KEY_LSHIFT);

		switch (code) {
		case Keyboard.KEY_ADD:
			return '+';
		case Keyboard.KEY_SUBTRACT:
		case Keyboard.KEY_MINUS:
			return shift ? '_' : '-';
		case Keyboard.KEY_EQUALS:
			return shift ? '+' : '=';
		case Keyboard.KEY_LBRACKET:
			return shift ? '{' : '[';
		case Keyboard.KEY_RBRACKET:
			return shift ? '}' : ']';
		case Keyboard.KEY_BACKSLASH:
			return shift ? '|' : '\\';
		case Keyboard.KEY_SLASH:
			return shift ? '?' : '/';
		case Keyboard.KEY_PERIOD:
			return shift ? '>' : '.';
		case Keyboard.KEY_COMMA:
			return shift ? '<' : ',';
		case Keyboard.KEY_MULTIPLY:
			return '*';
		case Keyboard.KEY_APOSTROPHE:
			return shift ? '\"' : '\'';
		case Keyboard.KEY_SEMICOLON:
			return shift ? ':' : ';';
		case Keyboard.KEY_SPACE:
			return ' ';

		case Keyboard.KEY_1:
			return shift ? '!' : '1';
		case Keyboard.KEY_2:
			return shift ? '@' : '2';
		case Keyboard.KEY_3:
			return shift ? '#' : '3';
		case Keyboard.KEY_4:
			return shift ? '$' : '4';
		case Keyboard.KEY_5:
			return shift ? '%' : '5';
		case Keyboard.KEY_6:
			return shift ? '^' : '6';
		case Keyboard.KEY_7:
			return shift ? '&' : '7';
		case Keyboard.KEY_8:
			return shift ? '*' : '8';
		case Keyboard.KEY_9:
			return shift ? '(' : '9';
		case Keyboard.KEY_0:
			return shift ? ')' : '0';

		case Keyboard.KEY_BACK:
			return '\b';
		}

		String character = Input.getKeyName(code);

		if (!Input.isDown(Keyboard.KEY_LSHIFT)) {
			character = character.toLowerCase();
		}

		if (character.length() < 2) {
			return character.charAt(0);
		}

		return '`';
	}

	public static List<Integer> getHeldKeys() {
		final List<Integer> keys = new ArrayList<Integer>();

		if (Input.isDown(Keyboard.KEY_BACK)) {
			keys.add(Keyboard.KEY_BACK);
			return keys;
		}

		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] > 0 && INPUT.states[i] == INPUT.PRESSED || INPUT.states[i] == INPUT.HELD_DOWN) {
				keys.add(INPUT.keys[i]);
			}
		}

		return keys;
	}

	public static String getKeyName(int key) {
		switch (key) {
		case KEY_LMB:
			return "left mouse";
		case KEY_RMB:
			return "right mouse";
		case KEY_MMB:
			return "middle mouse";
		case KEY_WHEEL_UP:
			return "mouse wheel up";
		case KEY_WHEEL_DOWN:
			return "mouse wheel down";
		case Keyboard.KEY_LCONTROL:
			return "left control";
		case Keyboard.KEY_RCONTROL:
			return "right control";
		case Keyboard.KEY_LSHIFT:
			return "left shift";
		case Keyboard.KEY_RSHIFT:
			return "right shift";
		case Keyboard.KEY_LBRACKET:
			return "[";
		case Keyboard.KEY_RBRACKET:
			return "]";
		case Keyboard.KEY_LMENU:
			return "left menu";
		case Keyboard.KEY_RMENU:
			return "right menu";
		case Keyboard.KEY_MINUS:
			return "-";
		case Keyboard.KEY_EQUALS:
			return "=";
		case Keyboard.KEY_SLASH:
			return "/";
		case Keyboard.KEY_BACKSLASH:
			return "\\";
		case Keyboard.KEY_PERIOD:
			return ".";
		case Keyboard.KEY_COMMA:
			return ",";
		case Keyboard.KEY_APOSTROPHE:
			return "'";
		case Keyboard.KEY_SEMICOLON:
			return ";";
		case Keyboard.KEY_TAB:
			return "tab";
		case Keyboard.KEY_BACK:
			return "backspace";
		case Keyboard.KEY_CAPITAL:
			return "caps lock";
		default:
			return Keyboard.getKeyName(key);
		}
	}

	public static int getMouseDWheel() {
		return mouseDWheel;
	}

	public static float getMouseDX() {
		return mouseDX;
	}

	public static float getMouseDY() {
		return mouseDY;
	}

	public static int getMouseX() {
		return mouseScreenPosX;
	}

	public static int getMouseY() {
		return mouseScreenPosY;
	}

	public static char[] getTypedKey() {
		final char[] output = new char[INPUT.MAX_KEYS];
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] > 0 && INPUT.states[i] == INPUT.PRESSED) {
				output[i] = getChar(INPUT.keys[i]);
			} else {
				output[i] = '`';
			}
		}

		if (INPUT.keys[0] == Keyboard.KEY_BACK) {
			backSpaceTimer += Window.deltaTime;

			if (backSpaceTimer > .45f) {
				output[0] = '\b';
				backSpaceTimer = .41f;
			}
		} else {
			backSpaceTimer = 0f;
		}

		return output;
	}

	public static boolean isDown(int key) {
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] != INPUT.NOT_PRESSED) {
				return true;
			}
		}

		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] != INPUT.NOT_PRESSED) {
				return true;
			}
		}

		return false;
	}

	public static boolean isDown(String key) {
		return isDown(Controls.get(key));
	}

	public static boolean isPressed(int key) {
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] == INPUT.PRESSED) {
				return true;
			}
		}

		return false;
	}

	public static boolean isPressed(String key) {
		return isPressed(Controls.get(key));
	}

	public static boolean isReleased(int key) {
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] == INPUT.RELEASED) {
				return true;
			}
		}

		return false;
	}

	public static boolean isReleased(String key) {
		return isReleased(Controls.get(key));
	}

	public static int keyState(int key) {
		for (int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key) {
				return INPUT.states[i];
			}
		}

		return INPUT.NOT_PRESSED;
	}

	/*
	 * public static boolean isMouseDown(int key) { return INPUT.mouse[key] == 2; }
	 * 
	 * public static boolean isMousePressed(int key) { return INPUT.mouse[key] == 1;
	 * }
	 * 
	 * public static boolean isMouseReleased(int key) { return INPUT.mouse[key] ==
	 * 3; }
	 */

	public static void poll() {
		int i = 0;

		final int[] keys = INPUT.keys;
		final int[] states = INPUT.states;
		// int[] mouse = INPUT.mouse;
		
		if (Window.deltaTime > KEY_TIMEOUT) {
			for (; i < INPUT.MAX_KEYS; i++) {
				keys[i] = 0;
				states[i] = INPUT.NOT_PRESSED;
			}
		} else {

			for (; i < INPUT.MAX_KEYS; i++) {
				if (states[i] == INPUT.RELEASED) {
					keys[i] = 0;
				}
				if (states[i] == INPUT.PRESSED) {
					states[i] = INPUT.HELD_DOWN;
				}
			}
		}

		i = 0;

		final long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastPoll >= pollRate) {
			lastPoll = currentTimeMillis;
			while (Keyboard.next()) {
				final int key = Keyboard.getEventKey();

				if (Keyboard.getEventKeyState()) {
					for (; i < INPUT.MAX_KEYS; i++) {
						if (keys[i] == 0) {
							keys[i] = key;
							states[i] = INPUT.PRESSED;

							Controls.handleCustomBinds(key);
							break;
						}
					}
				} else {
					for (int j = 0; j < INPUT.MAX_KEYS; j++) {
						if (keys[j] == key) {
							if (states[j] == INPUT.HELD_DOWN) {
								states[j] = INPUT.RELEASED;
							} else {
								keys[j] = 0;
							}
							break;
						}
					}
				}

				i++;
			}
		}

		mouseDWheel = Mouse.getDWheel();
		mouseDX = Mouse.getDX();
		mouseDY = Mouse.getDY();

		mouseScreenPosX = (int) ((float) Mouse.getX() / Display.getWidth() * UI.width);
		mouseScreenPosY = (int) (UI.height - (float) Mouse.getY() / Display.getHeight() * UI.height);

		if (mouseDWheel > 0) {
			keys[INPUT.MAX_KEYS - 1] = KEY_WHEEL_UP;
			states[INPUT.MAX_KEYS - 1] = INPUT.PRESSED;
		} else if (mouseDWheel < 0) {
			keys[INPUT.MAX_KEYS - 1] = KEY_WHEEL_DOWN;
			states[INPUT.MAX_KEYS - 1] = INPUT.PRESSED;
		} else {
			keys[INPUT.MAX_KEYS - 1] = 0;
			states[INPUT.MAX_KEYS - 1] = INPUT.NOT_PRESSED;
		}

		for (i = 0; i < 3; i++) {
			final int key = KEY_LMB + i;
			if (Mouse.isButtonDown(i)) {
				for (int j = 0; j < INPUT.MAX_KEYS; j++) {
					if (keys[j] == key) {
						states[j] = INPUT.HELD_DOWN;
						break;
					} else if (keys[j] == 0) {
						keys[j] = key;
						states[j] = INPUT.PRESSED;

						Controls.handleCustomBinds(key);
						break;
					}
				}
			} else {
				for (int j = 0; j < INPUT.MAX_KEYS; j++) {
					if (keys[j] == key) {
						if (states[j] == INPUT.HELD_DOWN) {
							states[j] = INPUT.RELEASED;
						} else {
							keys[j] = 0;
						}
						break;
					}
				}
			}
		}
	}

	public static void requestMouseGrab() {
		mouseGrabs++;
		if (mouseGrabs > 0) {
			Mouse.setGrabbed(true);
		}
	}

	public static void requestMouseRelease() {
		mouseGrabs--;
		if (mouseGrabs <= 0) {
			Mouse.setGrabbed(false);
		}
	}

	public static void setPollRate(long rate) {
		pollRate = rate;
	}

	private int[] keys;

	private int[] states;
	// private int[] mouse;

	public final int NOT_PRESSED = 0;

	public final int HELD_DOWN = 1;

	public final int PRESSED = 2;

	public final int RELEASED = 3;

	private final int MAX_KEYS = 8;

	Input() {
		keys = new int[MAX_KEYS];
		states = new int[MAX_KEYS];
		// mouse = new int[3];
	}
}
