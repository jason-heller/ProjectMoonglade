package dev.io;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.joml.Vector3f;

import core.Application;
import io.FileUtils;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import map.tile.BuildData;
import map.tile.Tile;
import scene.overworld.Overworld;
import scene.overworld.inventory.tool.EditorBoundsTool;

public class StructureExporter {

	public static void export(boolean includeHeights, boolean includeBuildings, boolean includeEnvTiles) {
		Overworld ow = (Overworld) (Application.scene);

		Vector3f p1 = EditorBoundsTool.p1;
		Vector3f p2 = EditorBoundsTool.p2;

		Vector3f min = new Vector3f(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z));
		Vector3f max = new Vector3f(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z));

		byte flags = 0;
		flags |= (includeHeights) ? 1 : 0;
		flags |= (includeBuildings) ? 2 : 0;
		flags |= (includeEnvTiles) ? 4 : 0;
		
		int x = (int) min.x - 1;
		int y = (int) min.y;
		int z = (int) min.z - 1;

		int w = (int) (max.x - min.x) + 2;
		int h = (int) (max.y - min.y);
		int l = (int) (max.z - min.z) + 2;

		Enviroment env = ow.getEnviroment();
		Terrain t = env.getTerrain();

		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(FileUtils.WORKING_DIRECTORY + "/src/res/struct/struct.str"));
			dos.writeChars("STR");
			dos.writeByte(1);
			dos.writeByte(flags);
			dos.writeInt(w);
			dos.writeInt(h);
			dos.writeInt(l);

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < l; j++) {
					int rx = x + i;
					int rz = z + j;
					Chunk c = t.getChunkAt(rx, rz);
					BuildData b = c.getBuilding();
					int dx = rx - c.realX;
					int dz = rz - c.realZ;

					if (includeHeights)
						dos.writeFloat(c.heightmap[dx][dz]);
					if (includeEnvTiles)
						dos.writeInt(c.getChunkEntities().getProp(dx, dz).ordinal());

					if (includeBuildings) {
						for (int k = 0; k < h; k++) {
							Tile tile = b.get(dx, y + k, dz);
							if (tile == null) {
								dos.write((byte)-1);
							} else {
								dos.write(tile.getWalls());
								dos.write(tile.getSlope());
								
								for(int s = 0; s < 7; s++) {
									dos.writeShort(tile.getMaterial(s).ordinal());
									dos.write(tile.getFlags()[s]);
								}
							}
						}
					}

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
