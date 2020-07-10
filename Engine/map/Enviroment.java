package map;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import dev.Console;
import gl.Camera;
import gl.skybox.Skybox;
import gl.terrain.TerrainRender;
import map.tile.TileModels;
import map.weather.Weather;
import procedural.biome.Biome;
import procedural.biome.BiomeMap;
import procedural.biome.BiomeVoronoi;
import procedural.terrain.GenTerrain;
import scene.Scene;
import scene.entity.EntityHandler;
import scene.entity.skybox.RevolvingPlanetEntity;
import scene.overworld.Overworld;
import util.MathUtil;

public class Enviroment {
	public static final int CYCLE_LENGTH = (Application.TICKS_PER_SECOND*60)*20;
	private static final int DAY_START = 0;
	private static final int DAY_SECTION_LENGTH  = CYCLE_LENGTH / 4;

	public static final int DAWN = 0; 
	public static final int DAY = DAY_SECTION_LENGTH; 
	public static final int DUSK = DAY_SECTION_LENGTH*2; 
	public static final int NIGHT = DAY_SECTION_LENGTH*3 ; 

	public static int biomeScale = 16*Chunk.CHUNK_SIZE;//8 for smaller
	public static int timeSpeed = 1;
	private static int time = 0;
	public static float exactTime = 0f;
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
	
	private Skybox skybox;

	static long seed;

	public Enviroment(Scene scene) {
		
		seed = Overworld.worldSeed.hashCode();
		GenTerrain.init((int)(seed & 0xff), (int)(seed & 0xff00), (int)(seed & 0xff0000), (int)(seed & 0xff000000));
		
		skybox = new Skybox();
		skybox.addEntity(new RevolvingPlanetEntity(5, 45, 45, "sun"));
		skybox.addEntity(new RevolvingPlanetEntity(-5, 45, 45, "moon"));
		
		terrainRender = new TerrainRender();
		
		TileModels.init();

		lightDirection = new Vector3f();

		Vector3f c = scene.getCamera().getPosition();
		
		biomeMap = new BiomeMap();
		
		for(Biome biome : biomeMap.getBiomes()) {
			if (Overworld.worldSeed.toLowerCase().equals(biome.getName().toLowerCase())) {
				BiomeVoronoi.singularBiome = biome;
				break;
			}
		}
		biomeVoronoi = new BiomeVoronoi(this, Terrain.size, biomeScale, c.x, c.z, (int)seed + 94823);
		
		weather = new Weather(seed, 3);
		terrain = new Terrain(this);
		GenTerrain.initStructureHandler(terrain);
		
		final int chunkX = (int) Math.floor(c.x / Chunk.CHUNK_SIZE);
		final int chunkZ = (int) Math.floor(c.z / Chunk.CHUNK_SIZE);

		reposition(chunkX, chunkZ);
		
		Resources.addSound("walk_grass", "walk_grass.ogg", 3, false);
		Resources.addSound("tree_fall", "tree_fall.ogg", true);
		Resources.addSound("chop_bark", "chop.ogg", 2, false);
		Resources.addSound("swing", "swing.ogg", 2, false);
		Resources.addSound("collect", "collect03.wav", true);
		Resources.addSound("water", "ambient/water_ambient.ogg", true);
		
		spawner = new EntitySpawnHandler(this, terrain);
	}
	
	public void cleanUp() {
		skybox.cleanUp();
		terrainRender.cleanUp();
		terrain.cleanUp();
		weather.cleanUp();
		//Resources.removeTextureReference("terrain_tiles");
		
		Resources.removeSound("walk_grass");
		Resources.removeSound("tree_fall");
		Resources.removeSound("chop_bark");
		Resources.removeSound("swing");
		Resources.removeSound("collect");
		Resources.removeSound("water");
	}

	public Terrain getTerrain() {
		return terrain;
	}
	
	public void render(Camera camera, Vector3f selectionPt, byte facing) {
		Vector3f weatherColor = weather.determineSkyColor();
		
		skybox.render(camera, Enviroment.time, lightDirection, getClosestBiome().getSkyColor(), weatherColor);
		terrainRender.render(camera, lightDirection, selectionPt, facing, terrain);

		//EntityControl.render(camera, lightDirection);
	}
	
	public void reposition(int camX, int camZ) {
		x = camX;
		z = camZ;
		
		terrain.populate(x, z);
		EntityHandler.setActivation(terrain);
	}

	public void tick(Scene scene) {
		final Camera camera = scene.getCamera();

		final int camX = (int) Math.floor(camera.getPosition().x / Chunk.CHUNK_SIZE) - (Terrain.size / 2);
		final int camZ = (int) Math.floor(camera.getPosition().z / Chunk.CHUNK_SIZE) - (Terrain.size / 2);

		spawner.tick();
		
		weather.tick(camera);
		biomeVoronoi.tick(camera.getPosition().x, camera.getPosition().z);//camera.getPosition().x, camera.getPosition().z
		if (x != camX) {
			final int dx = camX - x;
			if (Math.abs(dx) > 1) {
				reposition(camX, camZ);
			} else {
				terrain.shiftX(dx);
				EntityHandler.setActivation(terrain);
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
				EntityHandler.setActivation(terrain);
				//EntityControl.shiftY(terrain, dz);
			}
			z = camZ;
		}
		
		terrain.update(camera);

		// DAY/NIGHT time
		if (Math.abs(time - DAY) < 30) {
			exactTime = ((exactTime + (timeSpeed/5f)) % CYCLE_LENGTH);
		} else {
			exactTime = ((exactTime + timeSpeed) % CYCLE_LENGTH);
		}
		time = (int)exactTime;
		
		lightDirection.z = (float) Math.cos((DAY_START + time) * MathUtil.TAU / CYCLE_LENGTH);
		lightDirection.y = (float) Math.sin((DAY_START + time) * MathUtil.TAU / CYCLE_LENGTH);
		lightDirection.x = .3f;
		
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
		exactTime = t;
	}
	
	public int getTime() {
		return time;
	}
	
	public Vector3f getLightDirection() {
		return lightDirection;
	}

	public Skybox getSkybox() {
		return skybox;
	}
}
