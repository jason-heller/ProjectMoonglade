package ui;

import org.joml.Vector3f;
import org.joml.Vector4f;

import core.Resources;
import gl.Window;
import gl.res.Texture;

public class Image implements Component {

	public Texture gfx;
	public float x, y, w, h;

	private Vector3f color;
	private float opacity = 1f;
	private int numRows;
	private Vector4f uvOffset = new Vector4f(0, 0, 0, 0);
	private boolean centered = false;
	private float rotation;

	private float index;
	private int animationCap;
	private int depth = UI.DEPTH_SEQUENTIAL;
	private boolean temporary = false;

	public Image(String texture, float x, float y) {
		gfx = Resources.getTexture(texture);
		this.x = x;
		this.y = y;
		this.w = gfx.size;
		this.h = gfx.size;
		color = Colors.WHITE;
	}

	public Image(Texture texture, float x, float y) {
		gfx = texture;
		this.x = x;
		this.y = y;
		this.w = gfx.size;
		this.h = gfx.size;
		color = Colors.WHITE;
	}

	public Image(Texture texture, float x, float y, float w, float h) {
		gfx = texture;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		color = Colors.WHITE;
	}

	public void addIndexOffset(float dx, float dy) {
		this.index += dx + dy * numRows;
		final float size = 1f / numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}

	public Vector3f getColor() {
		return color;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public float getNumRows() {
		return numRows;
	}

	public float getOpacity() {
		return opacity;
	}

	public float getRotation() {
		return rotation;
	}

	public Texture getTexture() {
		return gfx;
	}

	public Vector4f getTransform() {
		final float scaledWidth = w / UI.width * Window.displayWidth;
		final float scaledHeight = h / UI.height * Window.displayHeight;
		final float scaledX = x / UI.width * Window.displayWidth;
		final float scaledY = y / UI.height * Window.displayHeight;

		final float width = scaledWidth / (Window.displayWidth / 2f);
		final float height = scaledHeight / (Window.displayHeight / 2f);
		// float rotCos = (float)Math.cos(rotation);
		// float rotSin = (float)Math.sin(rotation);
		// float rx = rotCos * width + (-rotSin) * height;
		// float ry = rotSin * width + rotCos * height;
		return new Vector4f(-0.5f + scaledX / Window.displayWidth,
				-(0.5f - height / 2f) + scaledY / Window.displayHeight, width, height);
	}

	public Vector4f getUvOffset() {
		return uvOffset;
	}

	private float getXOffset() {
		final int col = (int) index % numRows;
		return (float) col / (float) numRows;
	}

	private float getYOffset() {
		final int row = (int) index / numRows;
		return (float) row / (float) numRows;
	}

	public void incIndex(float incrementation) {
		index = Math.min(index + incrementation * Window.deltaTime, animationCap);
		final float size = 1f / numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}

	public boolean isCentered() {
		return centered;
	}

	/**
	 * @return whether or not this is a temporary component
	 */
	@Override
	public boolean isTemporary() {
		return temporary;
	}

	/**
	 * Marks this component as temporary
	 */
	@Override
	public void markAsTemporary() {
		temporary = true;
	}

	public void setAnimCap(int cap) {
		animationCap = cap;
	}

	public Image setCentered(boolean centered) {
		this.centered = centered;
		return this;
	}

	public Image setColor(Vector3f color) {
		this.color = color;
		return this;
	}

	@Override
	public Image setDepth(int depth) {
		this.depth = depth;
		UI.updateDepth(this);
		return this;
	}

	public void setIndex(float index) {
		this.index = index;
		final float size = 1f / numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public Image setOpacity(float opacity) {
		this.opacity = opacity;
		return this;
	}

	public Image setRotation(float rotation) {
		this.rotation = rotation;
		return this;
	}

	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
	}

	public void setUvOffset(float x, float y, float z, float w) {
		this.uvOffset = new Vector4f(x, y, z, w);
	}

	public Image setUvOffset(Vector4f offset) {
		this.uvOffset = offset;
		return this;
	}

}
