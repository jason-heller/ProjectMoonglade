package map;

public class EntitySpawnHandler {
	private Terrain terrain;
	
	public EntitySpawnHandler(Terrain terrain) {
		this.terrain = terrain;
	}

	public void tick() {
		//TODO: Every Xth tick do a die roll to see if an entity should be spawned,
		// check a random spot within range of player, if its free, pick a random entity based 
		// on time, biome, distance from spawn (for enemies). also do a random chance check
		// for each entity type after picking one
		
		// then spawn 1-X of that entity 
	}
}
