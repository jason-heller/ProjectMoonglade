package io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import gl.res.Model;
import procedural.structures.StructureData;

public class StructureLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports

	private static StructureData extractData(DataInputStream is)
			throws IOException {

		byte flags = is.readByte();
		boolean readHeights = (flags & 0x01) != 0;
		boolean readBuilds = (flags & 0x02) != 0;
		boolean readEnvs = (flags & 0x04) != 0;

		int w = is.readInt();
		int h = is.readInt();
		int l = is.readInt();

		StructureData sd = new StructureData(w, h, l, flags);

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++) {
				if (readHeights)
					sd.setHeight(i, j, is.readFloat());
				if (readEnvs)
					sd.setEnvTile(i, j, is.readInt());

				if (readBuilds) {
					for (int k = 0; k < h; k++) {
						int tileWalls = is.read();
						if (tileWalls == 0)
							continue;
						int tileFlags = is.read();
						int[] mats = new int[6];
						for (int s = 0; s < 6; s++)
							mats[s] = is.readShort();

						sd.setBuildingTile(i, k, j, mats, tileWalls, tileFlags);
					}
				}
			}
		}

		return sd;
	}
	
	public static StructureData read(String path) {
		try {
			return read(new DataInputStream(new FileInputStream("src/res/struct/" + path)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static StructureData read(byte[] data) {
		return read(new DataInputStream(new ByteArrayInputStream(data)));
	}

	public static StructureData read(DataInputStream is) {
		StructureData sd = null;

		try {

			// Header
			final String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			final byte version = is.readByte();

			if (version != EXPECTED_VERSION) {
				return null;
			}

			if (!fileExtName.equals("STR")) {
				return null;
			}

			sd = extractData(is);

		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return sd;
	}
}
