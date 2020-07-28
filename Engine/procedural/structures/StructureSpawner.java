package procedural.structures;

import java.util.Random;

import core.Application;
import dev.Debug;
import gl.Camera;
import map.Chunk;
import map.Terrain;
import procedural.NoiseUtil;
import procedural.biome.BiomeData;
import procedural.biome.BiomeVoronoi;

public class StructureSpawner {
	
	private int[] x, z;		// Top left
	private int size;
	private StructureHandler handler;
	private BiomeVoronoi biomeVoronoi;
	private Random r;
	private long seed;
	
	public static final int[] quadrants = new int[] {1, 2, 4, 16};
	
	public StructureSpawner(StructureHandler handler, BiomeVoronoi biomeVoronoi, long seed) {
		this.handler = handler;
		this.biomeVoronoi = biomeVoronoi;
		this.seed = seed;
		
		int numQuadrants = quadrants.length;
		x = new int[numQuadrants];
		z = new int[numQuadrants];
		
		r = new Random(pair3(seed, x[0], z[0]));
		resize();
		
		if (!Debug.structureMode) {
			for(int i = 0; i < numQuadrants; i++) {
				doArea(x[i], z[i], i);
			}
		}
	}

	public void resize() {
		final int newSize = (Terrain.size);
		
		Camera camera = Application.scene.getCamera();
		int rad = (size / 2) * Chunk.CHUNK_SIZE;
		int i = 0;
		for(int quadrant : quadrants) {
			int quadChunkSize = quadrant * Chunk.CHUNK_SIZE;
			x[i] = (int) Math.floor((camera.getPosition().x - rad) / quadChunkSize);
			z[i] = (int) Math.floor((camera.getPosition().z - rad) / quadChunkSize);
			i++;
		}
		
		size = newSize;
	}
	
	public void update(Camera camera) {
		if (Debug.structureMode) return;
		
		int quadIndex = 0;
		int rad = (size / 2) * Chunk.CHUNK_SIZE;
		for(int quadrant : quadrants) {
			int quadChunkSize = quadrant * Chunk.CHUNK_SIZE;
			int cx = (int) Math.floor((camera.getPosition().x - rad) / quadChunkSize);
			int cz = (int) Math.floor((camera.getPosition().z - rad) / quadChunkSize);
			
			if (x[quadIndex] != cx) {
				doArea(cx, cz, quadIndex);
				x[quadIndex] = cx;
				
			}

			if (z[quadIndex] != cz) {
				doArea(cx, cz, quadIndex);
				z[quadIndex] = cz;
			}
			
			quadIndex++;
		}
	}
	
	private void doArea(int x, int z, int quadrantIndex) {
		handler.clear(quadrantIndex);
		int quadrant = quadrants[quadrantIndex];
		int s = (int) Math.ceil(size / (float)quadrant);
		for(int i = 0; i < s; i ++) {
			for(int j = 0; j < s; j ++) {
				detStructData(x + i, z + j, quadrantIndex);
			}
		}
	}

	private void detStructData(int cx, int cz, int quadrantIndex) {
		int quadrant = quadrants[quadrantIndex];
		r.setSeed(pair3(seed, cx, cz));
		Structure structure;
		
		int rx = 0;//r.nextInt(Chunk.VERTEX_COUNT-1);
		int rz = 0;//r.nextInt(Chunk.VERTEX_COUNT-1);
		
		int chunkX = (cx * (Chunk.CHUNK_SIZE * quadrant));
		int chunkZ = (cz * (Chunk.CHUNK_SIZE * quadrant));
		
		int strX = chunkX + rx;
		int strZ = chunkZ + rz;
		
		BiomeData node = biomeVoronoi.getDataAt(strX, strZ);//biomeVoronoi.getPoint(cx*quadrant, cz*quadrant);
		
		structure = GenGlobalStructures.getTerrainStructures(strX, strZ, node.getSubseed(), r, quadrant);
		if (structure != null) {
			/*StructureData data = handler.structures.get(structure);
			if (rx + data.getWidth() > chunkX + Chunk.CHUNK_SIZE) {
				strX += data.getWidth();
			}
			
			if (rz + data.getLength() > chunkZ + Chunk.CHUNK_SIZE) {
				strZ += data.getLength();
			}*/
			handler.addStructure(strX, 0, strZ, structure, quadrantIndex);
			return;
		}
		
		structure = node.getMainBiome().getTerrainStructures(rx, rz, node.getSubseed(), r, quadrants[quadrantIndex]);
		if (structure != null) {
			handler.addStructure(strX, 0, strZ, structure, quadrantIndex);
		}
	}

	private long pair3(long seed, int cx, int cz) {
		return NoiseUtil.szudzik(NoiseUtil.szudzik(seed, cx), cz);
	}
}
