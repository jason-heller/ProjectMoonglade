package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.Console;

public class ObjToTilConverter {
	public static void convert(String filename) {
		BufferedReader reader;
		
		List<Vertex> glData = new ArrayList<Vertex>();
		List<Integer> glIndexOrder = new ArrayList<Integer>();
		
		List<float[]> positions = new ArrayList<float[]>();
		List<float[]> uvs = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<int[]> indices = new ArrayList<int[]>();
		
		File f = new File(filename);
		
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();

			while (line != null) {
				final String[] data = line.split(" ");
				
				if (data[0].equals("v")) {
					
					positions.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) });
				} else if (data[0].equals("vt")) {
					
					uvs.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]) });
				} else if (data[0].equals("vn")) {
					
					normals.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) });
				} else if (data[0].equals("f")) {
					
					for (byte i = 1; i < 4; i++) {
						final String[] faceData = data[i].split("/");
						final int[] index = new int[] { Integer.parseInt(faceData[0]) - 1,
								Integer.parseInt(faceData[1]) - 1, Integer.parseInt(faceData[2]) - 1 };

						int indexPosition = -1;

						for (int j = 0; j < indices.size(); j++) {
							final int[] check = indices.get(j);
							if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
								indexPosition = j;
								break;
							}
						}

						if (indexPosition == -1) {

							Vertex v = new Vertex();
							v.position = positions.get(index[0]);
							v.uv = uvs.get(index[1]);
							v.normal = normals.get(index[2]);
							
							glData.add(v);
							glIndexOrder.add(indices.size());

							indices.add(index);
						} else {
							glIndexOrder.add(indexPosition);
						}
					}
				}

				line = reader.readLine();
			}

			reader.close();
			DataOutputStream dos = null;
			
			try {
				dos = new DataOutputStream(new FileOutputStream(f.getPath().substring(0, f.getPath().indexOf(".")) + ".til"));
				Console.log(f.getPath().substring(0, f.getPath().indexOf(".")) + ".til");
				dos.writeChars("TIL");
				dos.writeByte(1);
				
				dos.writeByte(1);// \ TODO: allow multiple models to be fit into this
				dos.writeByte(0);// / (model #1 id, 0=always render)
				
				int vertexCount = glData.size();
				dos.writeShort(vertexCount);
				int indexCount = glIndexOrder.size();
				dos.writeShort(indexCount);
				
				for(Vertex vertex : glData) {
					dos.writeFloat(vertex.position[0]);
					dos.writeFloat(vertex.position[1]);
					dos.writeFloat(vertex.position[2]);
					
					dos.writeFloat(vertex.uv[0]);
					dos.writeFloat(1f-vertex.uv[1]);
					
					dos.writeFloat(vertex.normal[0]);
					dos.writeFloat(vertex.normal[1]);
					dos.writeFloat(vertex.normal[2]);
				}

				for(int i = 0; i < indexCount; i++) {
					dos.writeInt(glIndexOrder.get(i));
				}
			} catch (FileNotFoundException e) {
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
			
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Converted: "+filename+" to .TIL");
	}
}

class Vertex {
	float[] position;
	float[] uv;
	float[] normal;
}
