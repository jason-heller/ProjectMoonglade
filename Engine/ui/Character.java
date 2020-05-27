package ui;

public class Character {
	private final int id;
	private final float xTextureCoord;
	private final float yTextureCoord;
	private final float xMaxTextureCoord;
	private final float yMaxTextureCoord;
	private final int xOffset;
	private final int yOffset;
	private final float sizeX;
	private final float sizeY;
	private final int xAdvance;

	public Character(int id, float xTextureCoord, float yTextureCoord, float xTexSize, float yTexSize, int xOffset,
			int yOffset, float sizeX, float sizeY, int xAdvance) {
		this.id = id;
		this.xTextureCoord = xTextureCoord;
		this.yTextureCoord = yTextureCoord;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.xMaxTextureCoord = xTexSize + xTextureCoord;
		this.yMaxTextureCoord = yTexSize + yTextureCoord;
		this.xAdvance = xAdvance;
	}

	public int getId() {
		return id;
	}

	public float getSizeX() {
		return sizeX;
	}

	public float getSizeY() {
		return sizeY;
	}

	public int getxAdvance() {
		return xAdvance;
	}

	public float getXMaxTextureCoord() {
		return xMaxTextureCoord;
	}

	public int getxOffset() {
		return xOffset;
	}

	public float getxTextureCoord() {
		return xTextureCoord;
	}

	public float getYMaxTextureCoord() {
		return yMaxTextureCoord;
	}

	public int getyOffset() {
		return yOffset;
	}

	public float getyTextureCoord() {
		return yTextureCoord;
	}
}
