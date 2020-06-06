package map;

import org.joml.Vector3f;

import core.Application;
import core.Globals;
import dev.Console;
import gl.Camera;
import gl.terrain.TerrainRender;
import map.weather.Weather;
import procedural.biome.Biome;
import procedural.biome.BiomeMap;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;
import scene.Scene;
import util.MathUtil;

public class Enviroment {
	public static int chunkArrSize = Globals.chunkRenderDist; // Keep odd

	public static final int DAY_LENGTH = 60000;
	private static final double DAY_START = 0;

	public static int biomeScale = 8*Chunk.CHUNK_SIZE;
	public static int timeSpeed = 1;
	public static int time = 0;
	private static boolean toggleTime = true;
	
	private BiomeMap biomeMap;
	private BiomeVoronoi biomeVoronoi;
	
	private final Vector3f lightDirection;
	
	//private final SkyboxRenderer skyboxRenderer;
	private final TerrainRender terrainRender;
	
	int x, z;
	
	private Weather weather;
	public static Terrain terrain;

	public Enviroment(Scene scene) {
		GenTerrain.init(5, 7, 11, 13);
		long seed = System.currentTimeMillis();
		
		//skyboxRenderer = new SkyboxRenderer();
		terrainRender = new TerrainRender();

		lightDirection = new Vector3f();

		Vector3f c = scene.getCamera().getPosition();
		biomeMap = new BiomeMap();
		biomeVoronoi = new BiomeVoronoi(this, chunkArrSize, biomeScale, c.x, c.z, (int)seed);
		weather = new Weather(seed, 3);
		terrain = new Terrain(this, chunkArrSize);

		reposition(-chunkArrSize/2, -chunkArrSize/2);
		
		
	}
	
	public void cleanUp() {
		//skyboxRenderer.cleanUp();
		terrainRender.cleanUp();
		terrain.cleanUp();
		weather.cleanUp();
		//Resources.removeTextureReference("terrain_tiles");
	}

	public Terrain getTerrain() {
		return terrain;
	}
	
	public void render(Camera camera, float px, float py, float pz, float pw) {
		//skyboxRenderer.render(camera, time);
		
		for (final Chunk[] ca : terrain.get()) {
			for (final Chunk c : ca) {
				if (c == null) continue;
				c.checkIfCulled(camera.getFrustum());
			}
		}

		terrainRender.render(camera, lightDirection, terrain, px, py, pz, pw);

		//EntityControl.render(camera, lightDirection);
	}
	
	public void reposition(int camX, int camZ) {
		x = camX;
		z = camZ;
		
		terrain.populate(x, z);
	}

	public void resize(int chunkArrSize) {
		Terrain.size = chunkArrSize;
		terrain.cleanUp();
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate(x, z);
	}

	public void update(Scene scene) {
		final Camera camera = scene.getCamera();

		final int camX = (int) Math.floor(camera.getPosition().x / Chunk.CHUNK_SIZE) - (chunkArrSize / 2);
		final int camZ = (int) Math.floor(camera.getPosition().z / Chunk.CHUNK_SIZE) - (chunkArrSize / 2);

		weather.update(camera);
		biomeVoronoi.update(camera.getPosition().x, camera.getPosition().z);//camera.getPosition().x, camera.getPosition().z
		if (x != camX) {
			final int dx = camX - x;
			if (Math.abs(dx) > 1) {
				reposition(camX, camZ);
			} else {
				terrain.shiftX(dx);
			}
			x = camX;
		}

		if (z != camZ) {
			final int dz = camZ - z;
			if (Math.abs(dz) > 1) {
				reposition(camX, camZ);
			} else {
				terrain.shiftY(dz);
			}
			z = camZ;
		}
		
		terrain.update(camera);

		// DAY/NIGHT time
		time = (time + timeSpeed) % DAY_LENGTH;
		lightDirection.z = (float) Math.cos(DAY_START + time * MathUtil.TAU / DAY_LENGTH);
		lightDirection.y = (float) Math.sin(DAY_START + time * MathUtil.TAU / DAY_LENGTH);
		lightDirection.x = 0;
		
		lightDirection.mul(weather.getLightingDim());

	}
	
	public BiomeVoronoi getBiomeVoronoi() {
		return biomeVoronoi;
	}

	public Biome getClosestBiome() {
		return biomeVoronoi.getClosest().biome;
	}
	
	public Biome calcBiome(int nx, int ny, int seed, Temperature temperature, Moisture moisture, float randBiomeChange) {
		return biomeMap.getBiome(nx, ny, seed, temperature, moisture, randBiomeChange);
	}
	
	public Weather getWeather() {
		return weather;
	}

	public void toggleTime() {
		toggleTime = !toggleTime;
	}
	
	public void setTime(int t) {
		time = t;
	}
	
	public int getTime() {
		return time;
	}

	public void reloadTerrain() {
		Vector3f pos = Application.scene.getCamera().getPosition();
		chunkArrSize = Globals.chunkRenderDist; // Keep odd
		int halfSize = chunkArrSize / 2;
		terrain.cleanUp();
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate((int)(pos.x/Chunk.CHUNK_SIZE) - halfSize, (int)(pos.z/Chunk.CHUNK_SIZE) - halfSize);
	}
}
