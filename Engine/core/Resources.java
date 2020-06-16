package core;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.GL11;

import anim.Animation;
import audio.AudioHandler;
import core.res.Model;
import core.res.ModelUtils;
import core.res.Texture;
import core.res.TextureUtils;
import dev.Console;
import gl.fbo.FrameBuffer;
import gr.zdimensions.jsquish.Squish;
import io.AniFileLoader;
import io.ModFileLoader;

public class Resources {
	public static final Model QUAD2D = ModelUtils.quad2DModel();

	private static Map<String, Texture> textureMap = new HashMap<String, Texture>();
	private static Map<String, Model> modelMap = new HashMap<String, Model>();
	private static Map<String, Animation> animationMap = new HashMap<String, Animation>();
	private static Map<String, Integer> soundMap = new HashMap<String, Integer>();

	public static Texture addCompressedTexture(String key, String path) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(path));
			final int width = in.readShort();
			final int height = in.readShort();
			final int dataLen = in.readInt();
			final byte material = in.readByte();

			final byte[] textureData = new byte[dataLen];

			for (int l = 0; l < dataLen; l++) {
				textureData[l] = in.readByte();
			}

			final byte[] data = Squish.decompressImage(null, width, height, textureData, Squish.CompressionType.DXT3);

			return addTexture(key, material, data, width, height);
		} catch (final IOException e) {
			Console.printStackTrace(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public static Model addModel(String key, byte[] data, boolean saveVertexData) {
		final Model mdl = ModFileLoader.readModFile(key, data, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}
	
	public static void addAnimation(String key, Animation animation) {
		animationMap.put(key, animation);
	}

	public static void addAnimations(String key, String path) {
		AniFileLoader.readAniFile(key, path);
	}

	/**
	 * Imports a .mod model into the resource dictionary
	 *
	 *
	 * @param key  the key identifying this resource
	 * @param path the path to the resource's file
	 * @return the resource
	 */
	public static Model addModel(String key, String path) {
		return addModel(key, path, false);
	}

	/**
	 * Imports a .mod model into the resource dictionary
	 *
	 *
	 * @param key            the key identifying this resource
	 * @param path           the path to the resource's file
	 * @param saveVertexData bakes the vertex data into the resource's object
	 * @return the resource
	 */
	public static Model addModel(String key, String path, boolean saveVertexData) {
		final Model mdl = ModFileLoader.readModFile(key, path, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}

	/**
	 * Imports an obj model into the resource dictionary
	 *
	 * @deprecated Use addModel() instead (app should only use the engine-specific
	 *             .MOD files
	 *
	 * @param key  the key identifying this resource
	 * @param path the path to the resource's file
	 * @return the resource
	 */
	@Deprecated
	public static Model addObjModel(String key, String path) {
		return addObjModel(key, path, false);
	}

	/**
	 * Imports an obj model into the resource dictionary
	 *
	 * @deprecated Use addModel() instead (app should only use the engine-specific
	 *             .MOD files
	 *
	 * @param key            the key identifying this resource
	 * @param path           the path to the resource's file
	 * @param saveVertexData save vertex data into resource object
	 * @return the resource
	 */
	@Deprecated
	public static Model addObjModel(String key, String path, boolean saveVertexData) {
		final Model mdl = ModelUtils.loadObj("res/" + path, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}

	public static int addSound(String key, String path) {
		return addSound(key, path, false);
	}
	
	public static int addSound(String key, String path, boolean doPitchVariance) {
		int buffer = -1;
		if (path.charAt(path.length() - 1) == 'g') {
			buffer = AudioHandler.loadOgg("res/sfx/" + path);
		} else {
			buffer = AudioHandler.loadWav("res/sfx/" + path);
		}
		buffer |= (doPitchVariance ? 1 : 0) << 20;
		soundMap.put(key, buffer);
		return buffer;
	}
	
	public static int addSound(String key, String path, int versions) {
		return addSound(key, path, versions, false);
	}
	
	// uuuuuuuuuuuvnnnn################
	// # = sound buffer id number
	// n = number of variations (step_grass1, step_grass2 etc)
	// v = pitch variance flag
	public static int addSound(String key, String path, int numVersions, boolean doPitchVariance) {
		int buffer = -1;
		for(int version = numVersions; version != 0; version--) {
			if (path.charAt(path.length() - 1) == 'g') {
				buffer = AudioHandler.loadOgg("res/sfx/" + path.replace(".", version + "."));
			} else {
				buffer = AudioHandler.loadWav("res/sfx/" + path.replace(".", version + "."));
			}
			buffer |= ((numVersions - version) & 0xff) << 16; // First 16 bits are id, rest are flags
			buffer |= (doPitchVariance ? 1 : 0) << 20;
			soundMap.put(key, buffer);
		}
		return buffer;
	}

	public static Texture addTexture(String key, byte material, byte[] decompressedData, int width, int height) {
		final Texture tex = TextureUtils.createTexture(decompressedData, width, height);
		return addTexture(key, tex);
	}

	public static Texture addTexture(String key, FrameBuffer fbo) {
		if (fbo.hasTextureBuffer()) {
			return addTexture(key, fbo, false);
		}

		return addTexture(key, fbo, true);
	}

	public static Texture addTexture(String key, FrameBuffer fbo, boolean isDepthTexBuffer) {
		return textureMap.put(key, new Texture(isDepthTexBuffer ? fbo.getDepthBufferTexture() : fbo.getTextureBuffer(),
				fbo.getWidth(), false, 1));
	}

	public static Texture addTexture(String key, String path) {
		final Texture tex = TextureUtils.createTexture("res/" + path);
		textureMap.put(key, tex);
		return tex;
	}

	public static Texture addTexture(String key, String path, boolean nearest, boolean isTransparent,
			boolean clampEdges, boolean mipmap, float offset) {
		final Texture tex = TextureUtils.createTexture("res/" + path, GL11.GL_TEXTURE_2D, nearest, mipmap, offset,
				clampEdges, isTransparent, 0);
		textureMap.put(key, tex);
		return tex;
	}
	// String path, int type, boolean nearest, boolean mipmap, float bias, boolean
	// clampEdges, boolean isTransparent, int numRows

	public static Texture addTexture(String key, String path, int type, boolean nearest, boolean mipmap, float bias,
			boolean clampEdges, boolean isTransparent, int numRows) {
		final Texture tex = TextureUtils.createTexture("res/" + path, type, nearest, mipmap, bias, clampEdges,
				isTransparent, numRows);
		return addTexture(key, tex);
	}

	public static Texture addTexture(String key, String path, int type, boolean isTransparent, int numRows) {
		final Texture tex = TextureUtils.createTexture("res/" + path, type, isTransparent, numRows);
		textureMap.put(key, tex);
		return tex;
	}
	
	public static Texture addCubemap(String key, String ... paths) {
		byte[][] data = TextureUtils.getRawTextureData(paths);
		int wid = (int) Math.sqrt(data[0].length / 3);
		final Texture tex = TextureUtils.createTexture(data, wid, wid);
		textureMap.put(key, tex);
		return tex;
	}
	
	public static Texture addTexture(String key, Texture texture) {
		textureMap.put(key, texture);
		return texture;
	}

	public static void cleanUp() {
		for (final Model model : modelMap.values()) {
			model.cleanUp();
		}

		for (final Texture texture : textureMap.values()) {
			texture.delete();
		}

		for (final int buffer : soundMap.values()) {
			AL10.alDeleteBuffers(buffer);
		}

	}

	public static Collection<Integer> getAllSounds() {
		return soundMap.values();
	}
	
	public static Animation getAnimation(String key) {
		return animationMap.get(key);
	}

	public static Animation getAnimation(String modelKey, String animationKey) {
		return animationMap.get(modelKey + "_" + animationKey);
	}

	public static Model getModel(String key) {
		return modelMap.get(key);
	}

	public static String getSound(int index) {
		for (final String key : soundMap.keySet()) {
			if (soundMap.get(key) == index) {
				return key;
			}
		}

		return "";
	}

	public static int getSound(String key) {
		final Integer i = soundMap.get(key);
		return i == null ? -1 : i;
	}

	public static Texture getTexture(String key) {
		final Texture texture = textureMap.get(key);
		return texture == null ? Resources.getTexture("default") : texture;
	}

	public static void removeTextureReference(String key) {
		textureMap.get(key).delete();
		textureMap.remove(key);
	}
	
	public static void removeSound(String sound) {
		int mapData = soundMap.remove(sound);
		int buffer = (mapData & 0xffff);
		AL10.alDeleteBuffers(buffer);
		int variationNum = ((mapData >> 16) & 0xf);
		
		for(int i = 1; i <= variationNum; i++) {
			AL10.alDeleteBuffers(buffer - i);
			Console.log("remove", sound, buffer - i);
			
		}
	}
	
	public static void removeAllSounds() {
		for(int sound : soundMap.values()) {
			AL10.alDeleteBuffers(sound);
		}
	}
}
