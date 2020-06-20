package gl.res;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture {
	public final int id;
	public int size;

	private final int type;

	private int atlasRows = 1;

	private boolean transparent = false;

	public Texture(int id, int size, boolean transparent, int atlasRows) {
		this.id = id;
		this.size = size;
		this.type = GL11.GL_TEXTURE_2D;
		this.transparent = transparent;
		this.atlasRows = atlasRows;
	}

	public Texture(int id, int type, int size, boolean transparent, int atlasRows) {
		this.id = id;
		this.size = size;
		this.type = type;
		this.transparent = transparent;
		this.atlasRows = atlasRows;
	}

	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(type, id);
	}

	public void delete() {
		GL11.glDeleteTextures(id);
	}

	public int getTextureAtlasRows() {
		return atlasRows;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void unbind() {
		GL11.glBindTexture(type, 0);
	}
}
