package io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joml.Vector3f;

import dev.Console;
import gl.res.PropModel;

public class StaticEntityFileLoader {
	public static byte EXPECTED_VERSION = 2; // Version of .MOD files that this game supports

	private static PropModel extractModelData(DataInputStream is) throws IOException {
		Vector3f bounds = new Vector3f(is.readFloat()*2, is.readFloat()*2, is.readFloat()*2);

		float posVar = is.readFloat();
		float scaleVar = is.readFloat();
		
		final byte flags = is.readByte();
		
		PropModel model = new PropModel(bounds);
		
		//for(int p = 0; p < numParts; p++) {
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
				vertices[i * 3 + 0] = is.readFloat()*2;
				vertices[i * 3 + 1] = is.readFloat()*2;
				vertices[i * 3 + 2] = is.readFloat()*2;
				uvs[i * 2 + 0] = is.readFloat();
				uvs[i * 2 + 1] = is.readFloat();
				normals[i * 3 + 0] = is.readFloat();
				normals[i * 3 + 1] = is.readFloat();
				normals[i * 3 + 2] = is.readFloat();
			}

			for (i = 0; i < indexCount; i++) {
				indices[i] = is.readInt();
			}
			
			model.setMeshData(flags, vertices, uvs, normals, indices);
		//}
		model.setPositionVariance(posVar);
		model.setScaleVariance(scaleVar);
		return model;
	}

	public static PropModel readModFile(DataInputStream is) {
		PropModel model = null;

		try {

			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION) {
				Console.log("Warning: Asset is wrong version ("+version+")");
				return null;
			}

			if (!fileExtName.equals("SEF")) {
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
	public static PropModel readModFile(String path) {
		try {
			return readModFile(new DataInputStream(new FileInputStream("src/res/" + path)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
