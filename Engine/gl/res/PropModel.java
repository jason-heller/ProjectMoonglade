package gl.res;

import org.joml.Vector3f;

public class PropModel {
	private Vector3f bounds;
	private MeshData[] models;
	private float positionVariance, scaleVariance;
	
	public PropModel(int subModels, Vector3f bounds) {
		this.models = new MeshData[subModels];
		this.bounds = bounds;
	}
	
	public void addSubModel(byte flags, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		for(int i = 0; i < models.length; i++) {
			if (models[i] == null) {
				models[i] = new MeshData(flags, vertices, uvs, normals, indices);
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

	public Model toOpenGLModel() {
		
		MeshData sm = models[0];
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
