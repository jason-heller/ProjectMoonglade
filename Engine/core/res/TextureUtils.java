package core.res;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import io.FileUtils;

class TextureData {

	public int type;
	private final int width;
	private final int height;
	private final ByteBuffer buffer;

	private boolean clampEdges = false;
	private boolean mipmap = false;
	private boolean anisotropic = false;
	private boolean nearest = true;
	private boolean transparent;
	private float bias = 1f;
	private int numRows = 1;

	public TextureData(ByteBuffer buffer, int width, int height) {
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}

	public float getBias() {
		return bias;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public int getHeight() {
		return height;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getWidth() {
		return width;
	}

	public boolean isAnisotropic() {
		return anisotropic;
	}

	public boolean isClampEdges() {
		return clampEdges;
	}

	public boolean isMipmap() {
		return mipmap;
	}

	public boolean isNearest() {
		return nearest;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setAnisotropic(boolean anisotropic) {
		this.anisotropic = anisotropic;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public void setClampEdges(boolean clampEdges) {
		this.clampEdges = clampEdges;
	}

	public void setMipmap(boolean mipmap) {
		this.mipmap = mipmap;
	}

	public void setNearest(boolean nearest) {
		this.nearest = nearest;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}
}

public class TextureUtils {
	public static Texture createTexture(byte[] rgba, int width, int height) {
		final ByteBuffer buf = BufferUtils.createByteBuffer(rgba.length);
		buf.put(rgba);
		buf.flip();

		final TextureData textureData = new TextureData(buf, width, height);
		textureData.type = GL11.GL_TEXTURE_2D;
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, textureData.getWidth(), true, 0);
	}

	public static Texture createTexture(byte[][] rgba, int width, int height) {
		final int texID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);

		final ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
		for (int i = 0; i < 6; i++) {
			buf.put(rgba[i]);

			buf.flip();
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA,
					GL11.GL_UNSIGNED_BYTE, buf);
			buf.clear();
		}

		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);

		return new Texture(texID, GL13.GL_TEXTURE_CUBE_MAP, width * 6, false, 0);
	}

	public static Texture createTexture(String path) {
		final TextureData textureData = readTextureData(path);
		if (textureData == null) {
			return null;
		}
		textureData.type = GL11.GL_TEXTURE_2D;
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, textureData.getWidth(), true, 0);
	}

	public static Texture createTexture(String path, int type, boolean nearest, boolean mipmap, float bias,
			boolean clampEdges, boolean isTransparent, int numRows) {
		final TextureData textureData = readTextureData(path);
		textureData.type = type;
		textureData.setNearest(nearest);
		textureData.setMipmap(mipmap);
		textureData.setBias(bias);
		textureData.setClampEdges(clampEdges);
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}

	public static Texture createTexture(String path, int type, boolean isTransparent, int numRows) {
		final TextureData textureData = readTextureData(path);
		textureData.type = type;
		final int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}
	
	public static float[][][] getRawTextureData(String path) {
		TextureData td = readTextureData(path);
		
		int w = td.getWidth();
		int h = td.getHeight();
		
		// Haha super unoptimized
		ByteBuffer buf = td.getBuffer();
		float[][][] data = new float[w][h][3];
		byte[] byteArr = new byte[buf.remaining()];
		buf.get(byteArr);
		int byteArrInd = 0;
		
		for(int j = 0; j < h; j++) {
			for(int i = 0; i < w; i++) {
				// AGRB
				data[i][j][0] = (byteArr[byteArrInd+2] & 0xFF) / 255f;
				data[i][j][1] = (byteArr[byteArrInd+1] & 0xFF) / 255f;
				data[i][j][2] = (byteArr[byteArrInd+0] & 0xFF) / 255f;
				
				byteArrInd += 4;
			}
		}
		
		return data;
	}
	
	protected static TextureData readTextureData(String path) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			final InputStream in = FileUtils.getInputStream(path);
			final PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.BGRA);
			buffer.flip();
			in.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + path + " , didn't work");
			return null;
		}
		return new TextureData(buffer, width, height);
	}

	protected static int loadTextureToOpenGL(TextureData data) {
		final int texID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(data.type, texID);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		if (data.type == GL13.GL_TEXTURE_CUBE_MAP) {
			for (int i = 0; i < 6; i++) {
				GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.getWidth(),
						data.getHeight(), 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			}
		} else {
			GL11.glTexImage2D(data.type, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL12.GL_BGRA,
					GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}

		if (data.isMipmap()) {
			GL30.glGenerateMipmap(data.type);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);// GL11.
			GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, data.getBias());
			if (data.isAnisotropic() && GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
				GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, data.getBias());
				GL11.glTexParameterf(data.type, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);
			}
		} else if (data.isNearest()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		}
		
		if (data.isClampEdges()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}
		GL11.glBindTexture(data.type, 0);
		return texID;
	}

	public static void unbindTexture() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
