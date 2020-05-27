package ui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class Text implements Component {
	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_RIGHT = 1;
	public static final byte ALIGN_TOP = 2;
	public static final byte ALIGN_BOTTOM = 3;
	private String text;
	private final float x, y;
	private float w;
	private float h;

	public float textSize = 1f;
	private boolean centered = false;
	private float opacity = 1f;

	private final Font font;
	private Image[] letters;
	private boolean temporary = false;
	private int depth = UI.DEPTH_SEQUENTIAL;

	private int alignment = ALIGN_LEFT;
	private int lineWidth = 1280;

	public Text(Font font, String text, int x, int y, float textSize, boolean centered) {
		this.font = font;
		this.x = x;
		this.y = y;
		this.textSize = textSize;
		this.centered = centered;
		setText(text);
	}

	public Text(Font font, String text, int x, int y, float textSize, int lineWidth, boolean centered, int... offsets) {
		this.font = font;
		this.x = x;
		this.y = y;
		this.textSize = textSize;
		this.lineWidth = lineWidth;
		this.centered = centered;

		for (final int offset : offsets) {
			this.lineWidth = Math.max(this.lineWidth, offset);
		}

		setText(text, offsets);
	}

	public Text(String text, int x, int y) {
		this(Font.defaultFont, text, x, y, .3f, false);
	}

	public Text(String text, int x, int y, float textSize, boolean centered) {
		this(Font.defaultFont, text, x, y, textSize, centered);
	}

	public void align(byte alignment) {
		switch (this.alignment) {
		case ALIGN_RIGHT:
			for (int i = 0; i < letters.length; i++) {
				letters[i].x += w;
			}
			break;
		case ALIGN_BOTTOM:
			for (int i = 0; i < letters.length; i++) {
				letters[i].y += h;
			}
			break;
		}

		this.alignment = alignment;

		switch (alignment) {
		case ALIGN_RIGHT:
			for (int i = 0; i < letters.length; i++) {
				letters[i].x -= w;
			}
			break;
		case ALIGN_BOTTOM:
			for (int i = 0; i < letters.length; i++) {
				letters[i].y -= h;
			}
			break;
		}
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public Font getFont() {
		return font;
	}

	public float getHeight() {
		return h;
	}

	public Image[] getLetters() {
		return letters;
	}

	public float getOpacity() {
		return opacity;
	}

	public String getText() {
		return text;
	}

	public float getWidth() {
		return w;
	}

	public boolean isCentered() {
		return centered;
	}

	@Override
	public boolean isTemporary() {
		return temporary;
	}

	@Override
	public void markAsTemporary() {
		temporary = true;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	@Override
	public Text setDepth(int depth) {
		this.depth = depth;
		UI.updateDepth(this);
		return this;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public void setText(String text, int... offsets) {
		if (text.equals(this.getText())) {
			return;
		}
		final List<Image> letterList = new ArrayList<Image>();
		Vector3f color = Colors.WHITE;

		float dx = x;
		float dy = y;

		int offset = 0;

		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == '#') {
				if (text.length() > i + 1) {
					color = Colors.getColor(text.charAt(i + 1), i + (int) (System.currentTimeMillis() / 60f % 1000));
				}
				i += 1;
			} else if (c == '\t' && offset < offsets.length) {
				dx = x + offsets[offset];
				offset++;
			} else if (c == '\n') {
				dx = x;
				dy += font.getCharacter('A').getyOffset() * textSize + 20;
			} else if (c >= 32 && c <= 126) {
				final Character character = font.getCharacter(c);

				final Image newLetter = new Image(font.getTexture(), dx + character.getxOffset() * textSize,
						dy + character.getyOffset() * textSize);
				newLetter.setUvOffset(character.getxTextureCoord(), character.getyTextureCoord(),
						character.getXMaxTextureCoord(), character.getYMaxTextureCoord());
				newLetter.w = character.getSizeX() * textSize;
				newLetter.h = character.getSizeY() * textSize;
				newLetter.setColor(color);

				letterList.add(newLetter);

				dx += character.getxAdvance() * textSize;
				w = Math.max(dx - x, w);
				h = Math.max(dy - y, h);

				if (dx - x > lineWidth && c == ' ') {
					dx = x;
					dy += newLetter.h + 20;
				}
			}
		}

		int j = 0;
		letters = new Image[letterList.size()];
		for (final Image img : letterList) {
			letters[j++] = img;
		}

		// if (letters.length > 0) {
		// h += font.getHeight();
		// }

		if (centered) {
			for (int i = 0; i < letters.length; i++) {
				letters[i].x -= w / 2f;
				letters[i].y -= h / 2f;
			}
		}
	}
}
