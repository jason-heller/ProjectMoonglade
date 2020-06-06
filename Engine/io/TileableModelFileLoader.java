package io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import core.res.TileableModel;
import dev.Console;

public class TileableModelFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports

	private static TileableModel extractModelData(DataInputStream is) throws IOException {
		final byte numParts = is.readByte();
		
		TileableModel model = new TileableModel(numParts);
		
		for(int p = 0; p < numParts; p++) {
			final byte flags = is.readByte();
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
				uvs[i * 2 + 0] = is.readFloat();
				uvs[i * 2 + 1] = is.readFloat();
				normals[i * 3 + 0] = is.readFloat();
				normals[i * 3 + 1] = is.readFloat();
				normals[i * 3 + 2] = is.readFloat();
			}

			for (i = 0; i < indexCount; i++) {
				indices[i] = is.readInt();
			}
			
			model.addSubModel(flags, vertices, uvs, normals, indices);
		}
		
		return model;
	}

	public static TileableModel readModFile(DataInputStream is) {
		TileableModel model = null;

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

			model = extractModelData(is);

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
	public static TileableModel readModFile(String path) {
		try {
			return readModFile(new DataInputStream(new FileInputStream("src/res/" + path)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
