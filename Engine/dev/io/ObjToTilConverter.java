package dev.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import dev.Console;

public class ObjToTilConverter {
	final static float PI = (float)Math.PI;
	final static float PI2 = PI / 2f;
	
	private static final int[] idToFacing = new int[] {
			1, 1, 2, 3, 3, 3, 3
	};
	
	// left, right, top, bottom, front, back, slopeLR, slopeFB, gradLR1, gradLR2, gradFB1, grabFB2
	private static final Vector3f[] rotAxis = new Vector3f[] {
			Vector3f.Y_AXIS, null, null, Vector3f.Y_AXIS, Vector3f.Y_AXIS, Vector3f.Y_AXIS, Vector3f.Y_AXIS
	};
	
	private static final float[] rotRad = new float[] {
		PI2, 0f, 0f, -PI2, PI2, PI, 0
	};
	
	private static final float[] dx = new float[] {
			1, 0, 0, 0, 1, 0, 0
		};
	
	private static final float[] dz = new float[] {
			1, 1, 1, 1, 1, 0, 1
		};
	
	public static void tileFileParser(String filename) {
		List<String> lines;
		String path = "";
		
		byte wallFlags = 0, slopeFlags = 0;
		
		try {
			lines = Files.readAllLines(new File(filename).toPath());
			
			for(int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				
				if (line.length() == 0 || line.charAt(0) == '#') continue;

				if (line.contains("{")) {
					String data = line.replaceAll(" ", "").replaceAll("\t", "").replace("{","");
					path = data;
					
				} else if (line.contains("wallFlags")) {
					String[] data = line.split("=");
					wallFlags = (byte)Float.parseFloat(data[1]);
				} else if (line.contains("slopeFlags")) {
					String[] data = line.split("=");
					slopeFlags = (byte)Float.parseFloat(data[1]);
				}
				else if (line.contains("}")) {
					convert(path, wallFlags, slopeFlags);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void convert(String filename, byte wallFlags, byte slopeFlags) {
		BufferedReader reader;
		
		List<float[]> vertexPool = new ArrayList<float[]>();
		List<float[]> uvPool = new ArrayList<float[]>();
		List<float[]> normalPool = new ArrayList<float[]>();
		
		int currentId = -1;
		
		Map<Integer, List<Vertex>> glData = new HashMap<Integer, List<Vertex>>();
		Map<Integer, List<int[]>> indices = new HashMap<Integer, List<int[]>>();
		Map<Integer, List<Integer>> glIndexOrder = new HashMap<Integer, List<Integer>>();
		
		File f = new File(filename);
		
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();

			while (line != null) {
				final String[] data = line.split(" ");
				
				if (data[0].equals("v")) {
					float[] v = new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) };
					
					vertexPool.add(v);
				} else if (data[0].equals("vt")) {
					
					uvPool.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]) });
				} else if (data[0].equals("vn")) {
					
					normalPool.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) });
				} else if (data[0].equals("usemtl")) {
					currentId = getId(data[1]);
					glData.put(currentId, new ArrayList<Vertex>());
					indices.put(currentId, new ArrayList<int[]>());
					glIndexOrder.put(currentId, new ArrayList<Integer>());
				} else if (data[0].equals("f")) {
					
					for (byte i = 1; i < 4; i++) {
						final String[] faceData = data[i].split("/");
						final int[] index = new int[] { Integer.parseInt(faceData[0]) - 1,
								Integer.parseInt(faceData[1]) - 1, Integer.parseInt(faceData[2]) - 1 };

						int indexPosition = -1;

						for (int j = 0; j < indices.get(currentId).size(); j++) {
							final int[] check = indices.get(currentId).get(j);
							if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
								indexPosition = j;
								break;
							}
						}

						if (indexPosition == -1) {

							Vertex v = new Vertex();
							v.position = vertexPool.get(index[0]);
							v.uv = uvPool.get(index[1]);
							v.normal = normalPool.get(index[2]);
							glData.get(currentId).add(v);
							glIndexOrder.get(currentId).add(indices.get(currentId).size());

							indices.get(currentId).add(index);
						} else {
							glIndexOrder.get(currentId).add(indexPosition);
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
				dos.writeChars("TIL");// Static Entity File
				dos.writeByte(1);
				
				dos.writeByte(wallFlags);
				dos.writeByte(slopeFlags);
				dos.writeByte(idToFacing.length);
				
				for(int i = 0; i < idToFacing.length; i++) {
					int id = idToFacing[i];
					Vector3f axis = rotAxis[i];
					float thetaRadians = rotRad[i];
					if (glData.get(id).isEmpty()) continue;
					
					dos.writeShort(glData.get(id).size());
					dos.writeShort(glIndexOrder.get(id).size());
					
					for(Vertex vertex : glData.get(id)) {
						Vector3f vert = new Vector3f(vertex.position[0], vertex.position[1], vertex.position[2]);
						Vector3f norm = new Vector3f(vertex.normal[0], vertex.normal[1], vertex.normal[2]);
						if (axis != null) {
							vert = Vector3f.rotate(vert, axis, thetaRadians);
							norm = Vector3f.rotate(norm, axis, thetaRadians);
						}
						
						dos.writeFloat(vert.x+dx[i]);
						dos.writeFloat(vert.y);
						dos.writeFloat(vert.z+dz[i]);
						
						dos.writeFloat(vertex.uv[0]);
						dos.writeFloat(1f-vertex.uv[1]);
						
						dos.writeFloat(norm.x);
						dos.writeFloat(norm.y);
						dos.writeFloat(norm.z);
					}

					for(int j = 0; j < glIndexOrder.get(id).size(); j++) {
						dos.writeInt(glIndexOrder.get(id).get(j));
					}
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

	private static int getId(String name) {
		if (name.contains("WALL")) return 1;
		if (name.contains("FLOOR")) return 2;
		if (name.contains("SLOPE")) return 3;
		
		return -1;
	}
	
	
}
