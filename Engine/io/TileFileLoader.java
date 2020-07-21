package io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import dev.Console;
import gl.res.TileModel;

public class TileFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports

	private static TileModel extractTileData(DataInputStream is) throws IOException {
		byte wallFlags = is.readByte();
		byte slopeFlags = is.readByte();
		byte numDefinedTiles = is.readByte();

		TileModel model = new TileModel(numDefinedTiles);
		
		for(int p = 0; p < numDefinedTiles; p++) {
			final int vertexCount = is.readShort();
			final int indexCount = is.readShort();
			
			// Mesh data
			final float[] vertices = new float[vertexCount * 3];
			final float[] uvs = new float[vertexCount * 2];
			final float[] normals = new float[vertexCount * 3];
			final int[] indices = new int[indexCount];
			//////

			int i;
			for (i = 0; i < vertexCount; i++) {
				vertices[i * 3 + 0] = is.readFloat();
				vertices[i * 3 + 1] = is.readFloat();
				vertices[i * 3 + 2] = is.readFloat();
				uvs[i * 2 + 0] = 1f-is.readFloat();
				uvs[i * 2 + 1] = 1f-is.readFloat();
				normals[i * 3 + 0] = is.readFloat();
				normals[i * 3 + 1] = is.readFloat();
				normals[i * 3 + 2] = is.readFloat();
			}

			for (i = 0; i < indexCount; i++) {
				indices[i] = is.readInt();
			}
			
			model.addSubModel((byte)p, vertices, uvs, normals, indices);
		}
		return model;
	}

	public static TileModel readTileFile(DataInputStream is) {
		TileModel model = null;

		try {

			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION) {
				Console.log("Warning: Asset is wrong version ("+version+")");
				return null;
			}

			if (!fileExtName.equals("TIL")) {
				Console.log("Warning: Asset is wrong file(?) ("+fileExtName+")");
				return null;
			}

			model = extractTileData(is);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return model;
	}

	/**
	 * Load a .MOD file
	 *
	 * @param path           the to the file's directory within the res folder, for
	 *                       example, setting this to "weps/gun.mod" would point to
	 *                       a file called "gun.mod" in the res/weps folder
	 * @param saveVertexData setting this to true bakes the vertex data into the
	 *                       model
	 * @return
	 */
	public static TileModel	readTileFile(String path) {
		try {
			return readTileFile(new DataInputStream(new FileInputStream("src/res/tile/" + path)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
