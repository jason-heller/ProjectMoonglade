package core.res;

public class TileableModel {
	private SubModel[] models;
	
	public TileableModel(int subModels) {
		this.models = new SubModel[subModels];
	}
	
	public void addSubModel(byte flags, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		for(int i = 0; i < models.length; i++) {
			if (models[i] == null) {
				models[i] = new SubModel(flags, vertices, uvs, normals, indices);
			}
		}
	}
	
	public int getNumSubmodels() {
		return models.length;
	}
	
	public byte getFlags(int i) {
		return models[i].flags;
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
}

class SubModel {
	byte flags;
	
	float[] vertices;
	float[] uvs;
	float[] normals;
	
	int[] indices;
	
	public SubModel(byte flags, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		this.vertices = vertices;
		this.uvs = uvs;
		this.normals = normals;
		this.indices = indices;
		this.flags = flags;
	}
}
