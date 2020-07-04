package gl.res;

class MeshData {
	byte flags;
	
	float[] vertices;
	float[] uvs;
	float[] normals;
	
	int[] indices;
	
	public MeshData(byte flags, float[] vertices, float[] uvs, float[] normals, int[] indices) {
		this.vertices = vertices;
		this.uvs = uvs;
		this.normals = normals;
		this.indices = indices;
		this.flags = flags;
	}
}