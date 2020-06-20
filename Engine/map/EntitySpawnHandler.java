package map;

import java.util.Random;

import dev.Console;
import scene.entity.Entity;
import scene.entity.EntityHandler;
import scene.entity.EntityData;

public class EntitySpawnHandler {
	private Terrain terrain;
	private Enviroment enviroment;
	private Random spawnRng;
	
	private int tick = 0;
	private final int SPAWN_TICK = 24;
	
	private final int MIN_SPAWN_RAD = 24, SPAWN_RANGE = 64;
	
	private final int[] spawnable;
	
	public EntitySpawnHandler(Enviroment enviroment, Terrain terrain) {
		this.terrain = terrain;
		this.enviroment = enviroment;
		spawnRng = new Random(System.currentTimeMillis());
		
		spawnable = new int[] {2};
	}

	public void tick() {
		tick++;
		if (SPAWN_TICK == tick) {
			tick = 0;
			if (chance(1, 24)) {
				attemptSpawn();
			}
			
		}
		//TODO: Every Xth tick do a die roll to see if an entity should be spawned,
		// check a random spot within range of player, if its free, pick a random entity based 
		// on time, biome, distance from spawn (for enemies). also do a random chance check
		// for each entity type after picking one
		
		// then spawn 1-X of that entity 
		
	}

	private void attemptSpawn() {
		final int center = terrain.getArraySize() / 2;
		Chunk chunk = terrain.get(center, center);
		
		final int focusX = chunk.realX + (Chunk.CHUNK_SIZE/2);
		final int focusZ = chunk.realZ + (Chunk.CHUNK_SIZE/2);
		
		final float direction = (spawnRng.nextInt() & 0xB40) / 2f;
		float distance = MIN_SPAWN_RAD + (spawnRng.nextInt() & SPAWN_RANGE);

		distance = Math.min(distance, ((Chunk.CHUNK_SIZE * Terrain.size) / 2f) - 1f);

		float x = focusX + (float) (Math.sin(direction)) * distance;
		float z = focusZ + (float) (Math.cos(direction)) * distance;
		
		chunk = terrain.getChunkAt(x, z);
		
		int dx = (int)x - chunk.realX;
		int dz = (int)z - chunk.realZ;
		
		//if (chunk == null) return;
		// TODO: Ensure dx, dz are not on edge of chunk
		float terrainHeight = chunk.heightLookup(dx, dz);
		
		// Pick an entity to spawn
		int id = pickEntityId();
		Entity entity = EntityData.instantiate(id);
		
		// Check if location is viable
		//final int tileId = chunk.getTileItems().getTileId(dx, dz);
		//EnvTile envTile = terrain.getTileById(tileId);
		//BuildingTile buildingTile = chunk.getBuilding().get(dx, (int)terrainHeight, dz);
		
		if (entity.spawnConditionsMet(enviroment, terrain, chunk, x, z, dx, (int)terrainHeight, dz)) {
			spawn(x, terrainHeight, z, id, entity);
		}
	}

	private void spawn(float x, float y, float z, int id, Entity entity) {
		entity.position.set(x, y, z);
		EntityHandler.addEntity(entity);
		int numSpawned = 1;
		int numToSpawn = entity.getSpawnGroupMin() + (spawnRng.nextInt() & entity.getSpawnGroupVariation());

		for (; numSpawned < numToSpawn; numSpawned++) {
			int varX = -4 + (spawnRng.nextInt() & 8);
			int varZ = -4 + (spawnRng.nextInt() & 8);

			Chunk chunk = terrain.getChunkAt(x + varX, z + varZ);
			int nx = (int)(x + varX);
			int nz = (int)(z + varZ);
			int dx = nx - chunk.realX;
			int dz = nz - chunk.realZ;
			
			float ny = chunk.heightLookup(dx, dz);
			
			if (entity.spawnConditionsMet(enviroment, terrain, chunk, nx, nz, dx, (int)ny, dz)) {
				entity = EntityData.instantiate(id);
				entity.position.set(nx, ny, nz);
				EntityHandler.addEntity(entity);
			}
		}
	}

	private int pickEntityId() {
		int id = spawnRng.nextInt() & (spawnable.length - 1);
		return spawnable[id];
	}

	private boolean chance(int success, int trials) {
		return (spawnRng.nextInt() & trials) < success;
	}
}
