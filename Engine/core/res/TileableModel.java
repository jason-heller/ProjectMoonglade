package core.res;

import org.joml.Vector3f;

public class TileableModel {
	private Vector3f bounds;
	private SubModel[] models;
	private float positionVariance, scaleVariance;
	
	public TileableModel(int subModels, Vector3f bounds) {
		this.models = new SubModel[subModels];
		this.bounds = bounds;
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

	public void setPositionVariance(float positionVariance) {
		this.positionVariance = positionVariance;
	}
	
	public void setScaleVariance(float scaleVariance) {
		this.scaleVariance = scaleVariance;
	}
	
	public float getPositionVariance() {
		return positionVariance;
	}
	
	public float getScaleVariance() {
		return scaleVariance;
	}

	public Vector3f getBounds() {
		return bounds;
	}

	public Model toNonTileModel() {
		
		SubModel sm = models[0];
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, sm.vertices, 3);
		model.createAttribute(1, sm.uvs, 2);
		model.createAttribute(2, sm.normals, 3);
		model.createIndexBuffer(sm.indices);
		model.unbind();
		return model;
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
