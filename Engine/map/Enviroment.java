package map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import core.Application;
import core.Globals;
import gl.Camera;
import map.render.TerrainRender;
import procedural.BiomeVoronoi;
import scene.Scene;
import util.MathUtil;

public class Enviroment {
	public static int chunkArrSize = Globals.chunkRenderDist; // Keep odd

	public static final int DAY_LENGTH = 60000;
	private static final double DAY_START = 0;
	public static int timeSpeed = 1;
	public static int time = 0;
	private static boolean toggleTime = true;
	
	private BiomeVoronoi biomeVoronoi;
	
	private final Vector3f lightDirection;
	
	//private final SkyboxRenderer skyboxRenderer;
	private final TerrainRender terrainRender;
	
	int x, z;
	
	private Weather weather;
	public static Terrain terrain;

	public static Map<Temperature, Map<Moisture, List<Biome>>> biomeMap = new HashMap<Temperature, Map<Moisture, List<Biome>>>();
	
	
	public Enviroment(Scene scene) {
		long seed = System.currentTimeMillis();
		
		//skyboxRenderer = new SkyboxRenderer();
		terrainRender = new TerrainRender();

		lightDirection = new Vector3f();

		x = chunkArrSize / 2;
		z = chunkArrSize / 2;
		
		Vector3f c = scene.getCamera().getPosition();
		biomeVoronoi = new BiomeVoronoi(3, Chunk.CHUNK_SIZE*(15), c.x, c.z, 223443);
		
		weather = new Weather(seed, 3);
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate(0, 0);
	}
	
	public void cleanUp() {

		//skyboxRenderer.cleanUp();
		terrainRender.cleanUp();
		terrain.cleanUp();
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
		
		//biomeVoronoi.update(camX, camZ);
		terrain.populate(x - chunkArrSize / 2, z - chunkArrSize / 2);
	}

	public void resize(int chunkArrSize) {
		final int dSize = chunkArrSize / 2;// - (World.chunkArrSize/2);
		Terrain.size = chunkArrSize;
		// x -= dSize;
		// z -= dSize;
		terrain.cleanUp();
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate(x - dSize, z - dSize);
	}

	public void update(Scene scene) {
		final Camera camera = scene.getCamera();

		final int camX = (int) Math.floor(camera.getPosition().x / Chunk.CHUNK_SIZE);
		final int camZ = (int) Math.floor(camera.getPosition().z / Chunk.CHUNK_SIZE);

		weather.update();
		biomeVoronoi.update(camera.getPosition().x, camera.getPosition().z);//camera.getPosition().x, camera.getPosition().z
		terrain.update();
		
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

		// DAY/NIGHT time
		time = (time + timeSpeed) % DAY_LENGTH;
		lightDirection.z = (float) Math.cos(DAY_START + time * MathUtil.TAU / DAY_LENGTH);
		lightDirection.y = (float) Math.sin(DAY_START + time * MathUtil.TAU / DAY_LENGTH);
		lightDirection.x = 0;
		
		if (weather.getWeatherCells()[1][1] < -.6f) {
			lightDirection.mul(.5f);
		}
	}
	
	public BiomeVoronoi getBiomeVoronoi() {
		return biomeVoronoi;
	}

	public Biome getBiome() {
		return Biome.values()[(int)biomeVoronoi.getClosest()[2]];
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
