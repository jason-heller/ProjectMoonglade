package gl.res;

import org.joml.Vector3f;

import map.Material;
import procedural.biome.types.BiomeColors;
import util.Colors;
import util.ModelBuilder;

public class TileModel {
	private MeshData[] models;
	
	private static boolean flipX = false, flipZ = false;
	
	public TileModel(int subModels) {
		this.models = new MeshData[subModels];
	}
	
	public void addSubModel(byte id, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		models[id] = new MeshData(id, vertices, uvs, normals, indices);
	}
	
	public int getNumSubmodels() {
		return models.length;
	}
	
	public float[] getVertices(int i) {
		return models[i].vertices;
	}

	public float[] getUvs(int i) {
		return models[i].uvs;
	}

	public float[] getNormals(int i) {
		return models[i].normals;
	}

	public int[] getIndices(int i) {
		return models[i].indices;
	}

	public void pass(float dx, float dy, float dz, ModelBuilder builder, Vector3f tex, byte walls, byte flags, byte slant, boolean isColorable) {
		int id  = getId(walls, slant);	
		
		
		
		MeshData mesh = models[id];
		float[] vertices = mesh.vertices;
		final int len = vertices.length / 3;
		
		float[] uvs = mesh.uvs;
		float[] normals = mesh.normals;
		int[] indices = mesh.indices;
		
		if (flipX || flipZ) {
			indices = new int[mesh.indices.length];
			for(int i = 0; i < indices.length; i += 3) {
				indices[i] = mesh.indices[i+2];
				indices[i+1] = mesh.indices[i+1];
				indices[i+2] = mesh.indices[i];
				
			}
		} else {
			indices = mesh.indices;
		}
		
		int flipSx = flipX ? -1 : 1;
		int flipSz = flipZ ? -1 : 1;
		int flipDx = flipX ? 1 : 0;
		int flipDz = flipZ ? 1 : 0;
		
		Vector3f color = isColorable ? getTileColor(flags) : Colors.WHITE;
		
		for(int i = 0; i < len; i++) {
			int i3 = i*3;
			int i2 = i*2;
			
			builder.addVertex(dx + flipDx + vertices[i3] * flipSx, dy + vertices[i3 + 1], dz + flipDz + vertices[i3 + 2] * flipSz);
			builder.addUv(uvs[i2] * (tex.z) + tex.x, uvs[i2 + 1] * (tex.z) + tex.y);
			builder.addNormal(normals[i3], normals[i3 + 1], normals[i3 + 2]);
			builder.add(3, color.x, color.y, color.z);
		}
		
		builder.addRelativeIndices(vertices.length/3, indices);
	}

	private Vector3f getTileColor(byte colorId) {
		
		switch(colorId) {
		case 1: return Colors.RED;
		case 2: return Colors.ORANGE;
		case 3: return Colors.YELLOW;
		case 4: return Colors.GREEN;
		case 5: return Colors.CYAN;
		case 6: return Colors.BLUE;
		case 7: return Colors.INDIGO;
		case 8: return Colors.VIOLET;
		case 9: return Colors.DK_GREY;
		case 10: return Colors.SILVER;
		case 11: return Colors.LT_SILVER;
		
		case 12: return BiomeColors.TAIGA_GRASS;
		
		case 13: return Colors.OAK;
		case 14: return Colors.CLAY;
		
		default: return Colors.WHITE;
		}
	}

	// left, right, top, bottom, front, back, slopeLR, slopeFB, gradLR1, gradLR2, gradFB1, grabFB2
	private int getId(byte walls, byte slant) {
		flipX = false;
		flipZ = false;
		
		if (slant == -1) {
			switch(walls) {
			case 1: return 0;
			case 2: return 1;
			case 4: return 2;
			case 8: return 3;
			case 16: return 4;
			case 32: return 5;
			}
		} else if (slant == 0) {
			switch(walls) {
			case 1: return 6;
			case 2: flipX = true; return 6; // flip
			case 16: return 7;
			case 32: flipZ = true; return 7; // flip
			}
		} else if (slant == 1) {
			//eh
		}
		
		return 0;
	}

	/*public Model toOpenGLModel() {
		
		MeshData sm = models[0];
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, sm.vertices, 3);
		model.createAttribute(1, sm.uvs, 2);
		model.createAttribute(2, sm.normals, 3);
		model.createIndexBuffer(sm.indices);
		model.unbind();
		return model;
	}*/
}