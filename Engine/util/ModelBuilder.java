package util;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;

import core.res.Model;
import core.res.TileableModel;
import core.res.Vbo;

public class ModelBuilder {

	private int indexRel = 0;
	private boolean hasColorChannel = false;
	
	public static Model buildQuad(float[] vertices) {
		final float[] normals = new float[vertices.length];
		final Vector3f normal = getNormal(vertices, 0);
		for (int i = 0; i < 4; i++) {
			normals[i * 3 + 0] = normal.x;
			normals[i * 3 + 1] = normal.y;
			normals[i * 3 + 2] = normal.x;
		}

		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, new float[] { 0, 0, 1, 0, 0, 1, 1, 1 }, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(new int[] { 0, 1, 3, 3, 1, 2 });
		model.unbind();

		return model;
	}

	public static Model createModel(float[] vertices, float[] uvs, float[] normals, float[] colors, int[] indices) {
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createAttribute(3, colors, 4);
		model.createIndexBuffer(indices);
		model.unbind();

		//model.setVertexData(indices, vertices);

		return model;
	}
	
	public static Model createModel(float[] vertices, float[] uvs, float[] normals, int[] indices) {
		final Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(indices);
		model.unbind();

		//model.setVertexData(indices, vertices);

		
		return model;
	}
	
	public Model finish() {
		indexRel = 0;
		return (hasColorChannel) ? createModel(vertices, uvs, normals, colors, indices) :
			createModel(vertices, uvs, normals, indices);
	}
	
	public Vbo[] asVbos() {
		indexRel = 0;
		return asVbos(vertices, uvs, normals, colors, indices);
	}
	
	public static Vbo[] asVbos(float[] vertices, float[] uvs, float[] normals, float[] colors, int[] indices) {
		final Vbo[] vbos = new Vbo[5];
		
		vbos[0] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[0].bind();
		vbos[0].storeData(vertices);
		vbos[0].unbind();
		
		vbos[1] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[1].bind();
		vbos[1].storeData(uvs);
		vbos[1].unbind();
		
		vbos[2] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[2].bind();
		vbos[2].storeData(normals);
		vbos[2].unbind();
		
		vbos[3] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[3].bind();
		vbos[3].storeData(colors);
		vbos[3].unbind();
		
		vbos[4] = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		vbos[4].bind();
		vbos[4].storeData(indices);
		vbos[4].unbind();
	
		return vbos;
	}

	public static Model createModel(List<Float> meshVerts, List<Float> meshUvs, List<Float> meshNorms,
			List<Float> meshColors, List<Integer> meshIndices) {
		final int len = meshVerts.size() / 3;
		final float[] _v = new float[meshVerts.size()];
		final float[] _u = new float[meshUvs.size()];
		final float[] _n = new float[meshNorms.size()];
		final float[] _c = new float[meshColors.size()];
		final int[] _i = new int[meshIndices.size()];
		for (int i = 0; i < len; i++) {
			_v[i * 3 + 0] = meshVerts.get(i * 3 + 0);
			_v[i * 3 + 1] = meshVerts.get(i * 3 + 1);
			_v[i * 3 + 2] = meshVerts.get(i * 3 + 2);

			_u[i * 2 + 0] = meshUvs.get(i * 2 + 0);
			_u[i * 2 + 1] = meshUvs.get(i * 2 + 1);

			_n[i * 3 + 0] = meshNorms.get(i * 3 + 0);
			_n[i * 3 + 1] = meshNorms.get(i * 3 + 1);
			_n[i * 3 + 2] = meshNorms.get(i * 3 + 2);
			
			_c[i * 4 + 0] = meshColors.get(i * 4 + 0);
			_c[i * 4 + 1] = meshColors.get(i * 4 + 1);
			_c[i * 4 + 2] = meshColors.get(i * 4 + 2);
			_c[i * 4 + 3] = meshColors.get(i * 4 + 3);
		}

		for (int i = 0; i < meshIndices.size(); i++) {
			_i[i] = meshIndices.get(i);
		}

		return createModel(_v, _u, _n, _c, _i);
	}
	
	public static Model createModel(List<Float> meshVerts, List<Float> meshUvs, List<Float> meshNorms,
			List<Integer> meshIndices) {
		final int len = meshVerts.size() / 3;
		final float[] _v = new float[meshVerts.size()];
		final float[] _u = new float[meshUvs.size()];
		final float[] _n = new float[meshNorms.size()];
		final int[] _i = new int[meshIndices.size()];
		for (int i = 0; i < len; i++) {
			_v[i * 3 + 0] = meshVerts.get(i * 3 + 0);
			_v[i * 3 + 1] = meshVerts.get(i * 3 + 1);
			_v[i * 3 + 2] = meshVerts.get(i * 3 + 2);

			_u[i * 2 + 0] = meshUvs.get(i * 2 + 0);
			_u[i * 2 + 1] = meshUvs.get(i * 2 + 1);

			_n[i * 3 + 0] = meshNorms.get(i * 3 + 0);
			_n[i * 3 + 1] = meshNorms.get(i * 3 + 1);
			_n[i * 3 + 2] = meshNorms.get(i * 3 + 2);
		}

		for (int i = 0; i < meshIndices.size(); i++) {
			_i[i] = meshIndices.get(i);
		}

		return createModel(_v, _u, _n, _i);
	}
	
	public static Vbo[] asVbos(List<Float> meshVerts, List<Float> meshUvs, List<Float> meshNorms,
			List<Float> meshColors, List<Integer> meshIndices) {
		final int len = meshVerts.size() / 3;
		final float[] _v = new float[meshVerts.size()];
		final float[] _u = new float[meshUvs.size()];
		final float[] _n = new float[meshNorms.size()];
		final float[] _c = new float[meshColors.size()];
		final int[] _i = new int[meshIndices.size()];
		for (int i = 0; i < len; i++) {
			_v[i * 3 + 0] = meshVerts.get(i * 3 + 0);
			_v[i * 3 + 1] = meshVerts.get(i * 3 + 1);
			_v[i * 3 + 2] = meshVerts.get(i * 3 + 2);

			_u[i * 2 + 0] = meshUvs.get(i * 2 + 0);
			_u[i * 2 + 1] = meshUvs.get(i * 2 + 1);

			_n[i * 3 + 0] = meshNorms.get(i * 3 + 0);
			_n[i * 3 + 1] = meshNorms.get(i * 3 + 1);
			_n[i * 3 + 2] = meshNorms.get(i * 3 + 2);
			
			_c[i * 4 + 0] = meshColors.get(i * 4 + 0);
			_c[i * 4 + 1] = meshColors.get(i * 4 + 1);
			_c[i * 4 + 2] = meshColors.get(i * 4 + 2);
			_c[i * 4 + 3] = meshColors.get(i * 4 + 3);
		}

		for (int i = 0; i < meshIndices.size(); i++) {
			_i[i] = meshIndices.get(i);
		}

		return asVbos(_v, _u, _n, _c, _i);
	}

	public static int[] genIndexPattern(int vertexStripSize) {
		int pointer = 0;
		final int[] indices = new int[6 * (vertexStripSize - 1) * (vertexStripSize - 1)];
		for (int gz = 0; gz < vertexStripSize - 1; gz++) {
			for (int gx = 0; gx < vertexStripSize - 1; gx++) {
				final int topLeft = gz * vertexStripSize + gx;
				final int topRight = topLeft + 1;
				final int bottomLeft = (gz + 1) * vertexStripSize + gx;
				final int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}

		return indices;
	}

	private static Vector3f getNormal(float[] v, int i) {
		final Vector3f p1 = new Vector3f(v[i - 9], v[i - 8], v[i - 7]);
		final Vector3f p2 = new Vector3f(v[i - 6], v[i - 5], v[i - 4]);
		final Vector3f p3 = new Vector3f(v[i - 3], v[i - 2], v[i - 1]);

		return Vector3f.cross(Vector3f.sub(p2, p1), Vector3f.sub(p3, p1)).normalize();
	}

	private final List<Float> vertices, uvs, normals, colors;

	private final List<Integer> indices;

	public ModelBuilder() {
		vertices = new ArrayList<Float>();
		uvs = new ArrayList<Float>();
		normals = new ArrayList<Float>();
		colors = new ArrayList<Float>();
		indices = new ArrayList<Integer>();
	}

	public void addIndices(int... inds) {
		for (final int i : inds) {
			indices.add(i);
		}
	}
	
	public void addRelativeIndices(int jump, int... inds) {
		for (final int i : inds) {
			indices.add(indexRel+i);
		}
		
		indexRel += jump;
	}
	
	public void addRelativeIndices(int jump, byte... inds) {
		for (final byte i : inds) {
			indices.add(indexRel+i);
		}
		indexRel += jump;
	}
	
	public void addNormal(float nx, float ny, float nz) {
		normals.add(nx);
		normals.add(ny);
		normals.add(nz);
	}
	
	public void addNormal(Vector3f normal) {
		normals.add(normal.x);
		normals.add(normal.y);
		normals.add(normal.z);
	}

	public void addTextureCoord(float tx, float ty) {
		uvs.add(tx);
		uvs.add(ty);
	}

	public void addVertex(float x, float y, float z) {
		vertices.add(x);
		vertices.add(y);
		vertices.add(z);
	}
	
	public void addColor(Vector3f color) {
		colors.add(color.x);
		colors.add(color.y);
		colors.add(color.z);
		colors.add(1f);
		hasColorChannel = true;
	}
	
	public void addColor(float x, float y, float z) {
		colors.add(x);
		colors.add(y);
		colors.add(z);
		colors.add(1f);
		hasColorChannel = true;
	}
	
	public void addColor(float x, float y, float z, float w) {
		colors.add(x);
		colors.add(y);
		colors.add(z);
		colors.add(w);
		hasColorChannel = true;
	}

	public void addVertex(Vector3f v) {
		vertices.add(v.x);
		vertices.add(v.y);
		vertices.add(v.z);
	}

	public float[] getVertex(int index) {
		final int i = index * 3;
		return new float[] { vertices.get(i), vertices.get(i + 1), vertices.get(i + 2) };
	}

	public List<Float> getVertices() {
		return vertices;
	}

	public void addQuad(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f tex) {
			
		Vector3f normal = Vector3f.cross(Vector3f.sub(p3, p1), Vector3f.sub(p2, p1));
		normal.normalize().negate();
		
		addVertex(p1);
		addVertex(p2);
		addVertex(p3);
		addVertex(p4);

		addTextureCoord(tex.x + tex.z, tex.y);
		addTextureCoord(tex.x, tex.y);
		addTextureCoord(tex.x, tex.y + tex.z);
		addTextureCoord(tex.x + tex.z, tex.y + tex.z);

		addNormal(normal);
		addNormal(normal);
		addNormal(normal);
		addNormal(normal);
		
		addRelativeIndices(4, 0, 1, 3, 3, 1, 2);
	}

	public void addTileableModel(float rx, float ry, float rz, float scale, TileableModel tiledModel) {
		int len = tiledModel.getVertices(0).length / 3;
		float[] vertices = tiledModel.getVertices(0);
		float[] uvs = tiledModel.getUvs(0);
		float[] normals = tiledModel.getNormals(0);
		
		for (int i = 0; i < len; i++) {
			addVertex(rx + vertices[i * 3]*scale, ry + vertices[i * 3 + 1]*scale, rz + vertices[i * 3 + 2]*scale);
			addTextureCoord(uvs[i * 2], uvs[i * 2 + 1]);
			addNormal(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
			addColor(1, 1, 1, 0);
		}
		
		int[] indices = tiledModel.getIndices(0);
		addRelativeIndices(len, indices);

		/*this.addQuad(new Vector3f(rx,ry,rz-1),
				new Vector3f(rx-1,ry,rz),
				new Vector3f(rx,ry,rz),
				new Vector3f(rx-1,ry,rz-1), new Vector3f(rx,ry,rz));*/
	}
	
	public void addTileableModel(float rx, float ry, float rz, float scale, TileableModel tiledModel, byte flags) {
		// TODO finish this
	}

	public List<Float> getTextureCoords() {
		return this.uvs;
	}

	public List<Integer> getIndices() {
		return indices;
	}

	
}
