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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joml.Vector3f;

import dev.Console;

public class ObjToSefConverter {
	public static void sefFileParser(String filename) {
		List<String> lines;
		String path = "";
		float posVar = 0, scaleVar = 0;
		float width = Float.NaN, height = Float.NaN, length = Float.NaN;
		int minMiddles = 1, maxMiddles = 3;
		try {
			lines = Files.readAllLines(new File(filename).toPath());
			
			for(int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				
				if (line.length() == 0 || line.charAt(0) == '#') continue;

				if (line.contains("{")) {
					String data = line.replaceAll(" ", "").replaceAll("\t", "").replace("{","");
					path = data;
					
				} else if (line.contains("pos_variance")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					posVar = Float.parseFloat(data[1]);
					
				} else if (line.contains("scale_variancle")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					scaleVar = Float.parseFloat(data[1]);
					
				} else if (line.contains("width")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					width = Float.parseFloat(data[1]);
					
				} else if (line.contains("height")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					height = Float.parseFloat(data[1]);
					
				} else if (line.contains("length")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					length = Float.parseFloat(data[1]);
					
				} else if (line.contains("min_middles")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					minMiddles = Integer.parseInt(data[1]);
					
				} else if (line.contains("max_middles")) {
					String[] data = line.replaceAll(" ", "").replaceAll("\t", "").split("=");
					maxMiddles = Integer.parseInt(data[1]);
					
				} else if (line.contains("}")) {
					convert(path, posVar, scaleVar, width, height, length, minMiddles, maxMiddles);
					posVar = 0;
					scaleVar = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void convert(String filename, float posVar, float scaleVar, float width, float height, float length, int minMiddles, int maxMiddles) {
		BufferedReader reader;
		
		List<Vertex> glData = new ArrayList<Vertex>();
		List<Integer> glIndexOrder = new ArrayList<Integer>();
		
		List<float[]> positions = new ArrayList<float[]>();
		List<float[]> uvs = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<int[]> indices = new ArrayList<int[]>();
		Map<Integer, int[]> partitionOffsets = new TreeMap<Integer, int[]>();
		
		int lastOffset = -1;
		
		float w = width;
		float h = height;
		float l = length;
		
		Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		
		File f = new File(filename);
		
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();

			while (line != null) {
				final String[] data = line.split(" ");
				
				if (data[0].equals("v")) {
					float[] v = new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) };
					
					positions.add(v);
					
					max.x = Math.max(max.x, v[0]);
					max.y = Math.max(max.y, v[1]);
					max.z = Math.max(max.z, v[2]);
					
					min.x = Math.min(min.x, v[0]);
					min.y = Math.min(min.y, v[1]);
					min.z = Math.min(min.z, v[2]);
				} else if (data[0].equals("vt")) {
					
					uvs.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]) });
				} else if (data[0].equals("vn")) {
					
					normals.add(new float[] { Float.parseFloat(data[1]), Float.parseFloat(data[2]),
							Float.parseFloat(data[3]) });
				} else if (data[0].equals("o")) {
					if (lastOffset != -1)
						partitionOffsets.get(lastOffset)[1] = glIndexOrder.size();
					
					if (data[1].toLowerCase().contains("bottom")) {
						partitionOffsets.put(0, new int[] {glIndexOrder.size(), 0});
						lastOffset = 0;
					} else if (data[1].toLowerCase().contains("middle")) {
						partitionOffsets.put(1, new int[] {glIndexOrder.size(), 0});
						lastOffset = 1;
					} else if (data[1].toLowerCase().contains("top")) {
						partitionOffsets.put(2, new int[] {glIndexOrder.size(), 0});
						lastOffset = 2;
					}
					
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
			
			if (Float.isNaN(width))
				w = (max.x - min.x) / 2f;
			if (Float.isNaN(height))
				h = (max.y - min.y) / 2f;
			if (Float.isNaN(length))
				l = (max.z - min.z) / 2f;
			
			if (partitionOffsets.size() != 0) {
				partitionOffsets.get(lastOffset)[1] = glIndexOrder.size();
			}
			
			try {
				dos = new DataOutputStream(new FileOutputStream(f.getPath().substring(0, f.getPath().indexOf(".")) + ".sef"));
				Console.log(f.getPath().substring(0, f.getPath().indexOf(".")) + ".sef");
				dos.writeChars("SEF");// Static Entity File
				dos.writeByte(2);
				
				dos.writeFloat(w);
				dos.writeFloat(h);
				dos.writeFloat(l);
				
				dos.writeFloat(posVar);
				dos.writeFloat(scaleVar);
				
				dos.writeByte(0);		// Flags
	
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
		
		System.out.println("Converted: "+filename+" to .SEF");
	}
}

class Vertex {
	float[] position;
	float[] uv;
	float[] normal;
}
