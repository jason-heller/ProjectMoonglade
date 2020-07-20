package gl.res;

import org.joml.Vector3f;

public class PropModel {
	private Vector3f bounds;
	private MeshData meshData;
	private float positionVariance, scaleVariance;
	
	public PropModel(Vector3f bounds) {
		this.bounds = bounds;
	}
	
	public void setMeshData(byte flags, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		meshData = new MeshData(flags, vertices, uvs, normals, indices);
	}
	
	public byte getFlags() {
		return meshData.flags;
	}

	public float[] getVertices() {
		return meshData.vertices;
	}

	public float[] getUvs() {
		return meshData.uvs;
	}

	public float[] getNormals() {
		return meshData.normals;
	}

	public int[] getIndices() {
		return meshData.indices;
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
		
		MeshData sm = meshData;
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, sm.vertices, 3);
		model.createAttribute(1, sm.uvs, 2);
		model.createAttribute(2, sm.normals, 3);
		if ((sm.flags & 1) == 0) {
			model.createIndexBuffer(sm.indices);
		} else {
			
		}
		model.unbind();
		return model;
	}

	public PropModel copyAndShiftTexture(float dtx, float dty) {
		PropModel newPropModel = new PropModel(this.bounds);
		if (meshData != null) {
			float[] uvs = new float[meshData.uvs.length];
			for(int j = 0; j < meshData.uvs.length; j += 2) {
				uvs[j] = meshData.uvs[j] + dtx;
				uvs[j+1] = meshData.uvs[j+1] + dty;
			}
			newPropModel.setMeshData(meshData.flags, meshData.vertices, uvs, meshData.normals, meshData.indices);
		}
		
		return newPropModel;
	}

	public PropModel copy(PropModel propModel) {
		PropModel newPropModel = new PropModel(this.bounds);
		newPropModel.setMeshData(meshData.flags, meshData.vertices, meshData.uvs, meshData.normals, meshData.indices);
		
		return newPropModel;
	}
}
