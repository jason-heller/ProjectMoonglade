package procedural.terrain;

import static map.Chunk.POLYGON_SIZE;
import static map.Chunk.VERTEX_COUNT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL15;

import gl.res.Model;
import gl.res.Vbo;
import map.Chunk;

public class WaterMeshBuilder {
	
	private static int indsSize = 0;
	
	public static Model buildChunkMesh(Chunk chunk) {
		Vbo[] vbos = buildVbos(chunk);
		Model model = Model.create();
		model.bind();
		model.createAttribute(0, vbos[0], 3);
		model.createAttribute(1, vbos[1], 2);
		model.setIndexBuffer(vbos[2], indsSize);
		model.unbind();
		return model;
	}
	
	public static Vbo[] buildVbos(Chunk chunk) {
		final int x = chunk.realX;
		final int z = chunk.realZ;
		final float[][] table = chunk.waterTable;
		
		List<Float> vertices = new ArrayList<Float>();
		List<Float> uvs = new ArrayList<Float>();
		List<Integer> indices = new ArrayList<Integer>();
		indsSize = 0;
		
		int k = 0;
		for(int j = 0; j < VERTEX_COUNT-1; j++) {
			for (int i = 0; i < VERTEX_COUNT-1; i++) {
				if (table[i][j] == Float.MIN_VALUE)
					continue;
				
				vertices.add((float) x + ((i + 1) * POLYGON_SIZE));
				vertices.add(table[i+1][j]);
				vertices.add((float) z + ((j) * POLYGON_SIZE));

				vertices.add((float) x + ((i) * POLYGON_SIZE));
				vertices.add(table[i][j]);
				vertices.add((float) z + ((j) * POLYGON_SIZE));

				vertices.add((float) x + ((i) * POLYGON_SIZE));
				vertices.add(table[i][j+1]);
				vertices.add((float) z + ((j + 1) * POLYGON_SIZE));

				vertices.add((float) x + ((i + 1) * POLYGON_SIZE));
				vertices.add(table[i+1][j+1]);
				vertices.add((float) z + ((j + 1) * POLYGON_SIZE));

				uvs.add(.1f);
				uvs.add(0f);
				
				uvs.add(0f);
				uvs.add(0f);
				
				uvs.add(0f);
				uvs.add(.1f);
				
				uvs.add(.1f);
				uvs.add(.1f);
				
				indices.add(k+0);
				indices.add(k+1);
				indices.add(k+3);
				indices.add(k+3);
				indices.add(k+1);
				indices.add(k+2);
				indsSize += 6;
				k += 4;
			}
		}
		
		float[] verts = new float[vertices.size()];
		float[] texs = new float[uvs.size()];
		int[] inds = new int[indices.size()];
		int len = vertices.size()/3;

		for (int i = 0; i < len; i++) {
			verts[i * 3] = vertices.get(i * 3);
			verts[i * 3 + 1] = vertices.get(i * 3 + 1);
			verts[i * 3 + 2] = vertices.get(i * 3 + 2);
			texs[i * 2] = uvs.get(i * 2);
			texs[i * 2 + 1] = uvs.get(i * 2 + 1);
		}
		
		for(int i = 0; i < inds.length; i++) {
			inds[i] = indices.get(i);
		}
		
		final Vbo[] vbos = new Vbo[3];
		
		vbos[0] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[0].bind();
		vbos[0].storeData(verts);
		vbos[0].unbind();
		
		vbos[1] = Vbo.create(GL15.GL_ARRAY_BUFFER);
		vbos[1].bind();
		vbos[1].storeData(texs);
		vbos[1].unbind();
		
		vbos[2] = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		vbos[2].bind();
		vbos[2].storeData(inds);
		vbos[2].unbind();
	
		return new Vbo[] {vbos[0], vbos[1], vbos[2]};
	}
}
