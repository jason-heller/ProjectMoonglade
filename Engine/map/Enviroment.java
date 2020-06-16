package map;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import gl.Camera;
import gl.Window;
import gl.terrain.TerrainRender;
import map.weather.Weather;
import procedural.biome.Biome;
import procedural.biome.BiomeMap;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;
import scene.Scene;
import scene.entity.EntityControl;
import scene.overworld.Overworld;
import util.MathUtil;

public class Enviroment {
	public static int chunkArrSize = Terrain.size; // Keep odd

	public static final int DAY_LENGTH = 100000;
	private static final int DAY_START = 0;
	private static final int DAY_SECTION_LENGTH  = DAY_LENGTH / 4; 

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
	private EntitySpawnHandler spawner;

	public static long seed;

	public Enviroment(Scene scene) {
		
		seed = Overworld.worldSeed.hashCode();
		GenTerrain.init((int)(seed & 0xff), (int)(seed & 0xff00), 11 & 0xff0000, 13 & 0xff000000);
		
		//skyboxRenderer = new SkyboxRenderer();
		terrainRender = new TerrainRender();

		lightDirection = new Vector3f();

		Vector3f c = scene.getCamera().getPosition();
		
		biomeMap = new BiomeMap();
		biomeVoronoi = new BiomeVoronoi(this, chunkArrSize, biomeScale, c.x, c.z, (int)seed);
		
		weather = new Weather(seed, 3);
		terrain = new Terrain(this, chunkArrSize);
		
		final int chunkX = (int) Math.floor(c.x / Chunk.CHUNK_SIZE);
		final int chunkZ = (int) Math.floor(c.z / Chunk.CHUNK_SIZE);

		reposition(chunkX, chunkZ);
		
		Resources.addSound("walk_grass", "walk_grass.ogg", 3, false);
		Resources.addSound("tree_fall", "tree_fall.ogg", true);
		Resources.addSound("chop_bark", "chop.ogg", 2, false);
		Resources.addSound("swing", "swing.ogg", 2, false);
		Resources.addSound("collect", "collect03.wav", true);
		
		spawner = new EntitySpawnHandler(terrain);
	}
	
	public void cleanUp() {
		//skyboxRenderer.cleanUp();
		terrainRender.cleanUp();
		terrain.cleanUp();
		weather.cleanUp();
		//Resources.removeTextureReference("terrain_tiles");
		
		Resources.removeSound("walk_grass");
		Resources.removeSound("tree_fall");
		Resources.removeSound("chop_bark");
		Resources.removeSound("swing");
		Resources.removeSound("collect");
		
	}

	public Terrain getTerrain() {
		return terrain;
	}
	
	public void render(Camera camera, Vector3f selectionPt, byte facing, float px, float py, float pz, float pw) {
		//skyboxRenderer.render(camera, time);
		
		terrainRender.render(camera, lightDirection, selectionPt, facing, terrain, px, py, pz, pw);

		//EntityControl.render(camera, lightDirection);
	}
	
	public void reposition(int camX, int camZ) {
		x = camX;
		z = camZ;
		
		terrain.populate(x, z);
		EntityControl.setActivation(terrain);
	}

	public void resize(int chunkArrSize) {
		Terrain.size = chunkArrSize;
		terrain.cleanUp();
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate(x, z);
	}

	public void tick(Scene scene) {
		final Camera camera = scene.getCamera();

		final int camX = (int) Math.floor(camera.getPosition().x / Chunk.CHUNK_SIZE) - (chunkArrSize / 2);
		final int camZ = (int) Math.floor(camera.getPosition().z / Chunk.CHUNK_SIZE) - (chunkArrSize / 2);

		spawner.tick();
		
		weather.tick(camera);
		biomeVoronoi.tick(camera.getPosition().x, camera.getPosition().z);//camera.getPosition().x, camera.getPosition().z
		if (x != camX) {
			final int dx = camX - x;
			if (Math.abs(dx) > 1) {
				reposition(camX, camZ);
			} else {
				terrain.shiftX(dx);
				EntityControl.setActivation(terrain);
				//EntityControl.shiftX(terrain, dx);
			}
			x = camX;
		}

		if (z != camZ) {
			final int dz = camZ - z;
			if (Math.abs(dz) > 1) {
				reposition(camX, camZ);
			} else {
				terrain.shiftY(dz);
				EntityControl.setActivation(terrain);
				//EntityControl.shiftY(terrain, dz);
			}
			z = camZ;
		}
		
		terrain.update(camera);

		// DAY/NIGHT time
		time = (int) ((time + (timeSpeed*(Window.deltaTime*125f))) % DAY_LENGTH);
		lightDirection.z = (float) Math.cos((DAY_START + time) * MathUtil.TAU / DAY_LENGTH);
		lightDirection.y = (float) Math.sin((DAY_START + time) * MathUtil.TAU / DAY_LENGTH);
		
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
		chunkArrSize = Terrain.size; // Keep odd
		int halfSize = chunkArrSize / 2;
		terrain.cleanUp();
		terrain = new Terrain(this, chunkArrSize);
		terrain.populate((int)(pos.x/Chunk.CHUNK_SIZE) - halfSize, (int)(pos.z/Chunk.CHUNK_SIZE) - halfSize);
	}
	
	public Vector3f getLightDirection() {
		return lightDirection;
	}
}
