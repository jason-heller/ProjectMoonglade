package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import core.Resources;
import core.res.Texture;
import io.FileUtils;

public class Font {

	private static final int PAD_TOP = 0;
	private static final int PAD_LEFT = 1;
	private static final int PAD_BOTTOM = 2;
	private static final int PAD_RIGHT = 3;

	protected static final float LINE_HEIGHT = 0.03f;
	protected static final int SPACE_ASCII = 32;

	private static final int DESIRED_PADDING = 3;
	public static final float defaultSize = .3f;
	public static Font defaultFont = new Font("font/verdana");
	public static Font consoleFont = defaultFont;

	private final Map<Integer, Character> metaData = new HashMap<Integer, Character>();

	private BufferedReader reader;
	private final Map<String, String> values = new HashMap<String, String>();

	private int[] padding;

	private int paddingWidth;
	private int paddingHeight;
	private float spaceWidth;

	private final Texture fontTexture;

	public Font(String pathNoExtension) {
		load("res/" + pathNoExtension + ".fnt");
		fontTexture = Resources.addTexture("verdana", pathNoExtension + ".png", GL11.GL_TEXTURE_2D, true, false, 1f,
				false, false, 0);
	}

	public Character getCharacter(int ascii) {
		return metaData.get(ascii);
	}

	public int getHeight() {
		return paddingHeight;
	}

	public int getNumCharacters() {
		return metaData.size();
	}

	public float getSpaceWidth() {
		return spaceWidth;
	}

	public Texture getTexture() {
		return fontTexture;
	}

	private int[] getValuesOfVariable(String variable) {
		final String[] numbers = values.get(variable).split(",");
		final int[] actualValues = new int[numbers.length];
		for (int i = 0; i < actualValues.length; i++) {
			actualValues[i] = Integer.parseInt(numbers[i]);
		}
		return actualValues;
	}

	public int getWidth() {
		return paddingWidth;
	}

	private void load(String path) {
		try {
			// Open file
			reader = FileUtils.getReader(path);

			// Load padding data
			readLine();
			this.padding = getValuesOfVariable("padding");
			this.paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT];
			this.paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM];

			// Line sizes
			readLine();
			// int lineHeightPixels = Integer.parseInt(values.get("lineHeight")) -
			// paddingHeight;

			final int imageWidth = Integer.parseInt(values.get("scaleW"));

			// Character data
			readLine();
			readLine();
			while (readLine()) {
				final Character c = loadCharacter(imageWidth);
				if (c != null) {
					metaData.put(c.getId(), c);
				}
			}

			reader.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't read font file!");
		}
	}

	private Character loadCharacter(int imageSize) {
		final int id = Integer.parseInt(values.get("id"));
		if (id == SPACE_ASCII) {
			this.spaceWidth = Integer.parseInt(values.get("xadvance")) - paddingWidth;
			return new Character(id, 0, 0, 0, 0, 0, 0, spaceWidth, 0, (int) spaceWidth);
		}
		if (id == 9) {
			return null;
		}
		final float xTex = ((float) Integer.parseInt(values.get("x")) + (padding[PAD_LEFT] - DESIRED_PADDING))
				/ imageSize;
		final float yTex = ((float) Integer.parseInt(values.get("y")) + (padding[PAD_TOP] - DESIRED_PADDING))
				/ imageSize;
		final int width = Integer.parseInt(values.get("width")) - (paddingWidth - 2 * DESIRED_PADDING);
		final int height = Integer.parseInt(values.get("height")) - (paddingHeight - 2 * DESIRED_PADDING);
		final float quadWidth = width;
		final float quadHeight = height;
		final float xTexSize = (float) width / imageSize;
		final float yTexSize = (float) height / imageSize;
		final int xOff = Integer.parseInt(values.get("xoffset")) + padding[PAD_LEFT] - DESIRED_PADDING;
		final int yOff = Integer.parseInt(values.get("yoffset")) + padding[PAD_TOP] - DESIRED_PADDING;
		final int xAdvance = Integer.parseInt(values.get("xadvance")) - paddingWidth;
		return new Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance);
	}

	private boolean readLine() {
		values.clear();
		String line = null;
		try {
			line = reader.readLine();
		} catch (final IOException e1) {
		}
		if (line == null) {
			return false;
		}
		for (final String part : line.split(" ")) {
			final String[] valuePairs = part.split("=");
			if (valuePairs.length == 2) {
				values.put(valuePairs[0], valuePairs[1]);
			}
		}
		return true;
	}
}
