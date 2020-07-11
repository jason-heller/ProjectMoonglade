package scene.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import dev.Console;
import scene.entity.friendly.FireflyEntity;
import scene.entity.object.BedEntity;
import scene.entity.object.CampfireEntity;
import scene.entity.object.DoorEntity;
import scene.entity.object.ForgeEntity;
import scene.entity.utility.ItemEntity;

public class EntityData {
	private static final Map<String, Integer> map = new HashMap<String, Integer>();
	
	public static void init() {
		map.put(ItemEntity.class.getSimpleName(), 1);
		map.put(FireflyEntity.class.getSimpleName(), 2);
		map.put(DoorEntity.class.getSimpleName(), 3);
		map.put(CampfireEntity.class.getSimpleName(), 4);
		map.put(BedEntity.class.getSimpleName(), 5);
		map.put(ForgeEntity.class.getSimpleName(), 6);
		Console.log(DoorEntity.class.getSimpleName());
		initModelsAndTextures();
	}
	
	public static Collection<String> keySet() {
		return map.keySet();
	}

	public static Entity instantiate(int id) {
		return instantiate(id, new Vector3f(), 0);
	}
	
	public static Entity instantiate(int id, Vector3f pos, float rot) {
		switch(id) {
		case 1: return new ItemEntity(pos, 0, 0);
		case 2: return new FireflyEntity(Application.scene);
		case 3: return new DoorEntity(pos, rot);
		case 4: return new CampfireEntity(0, 0, 0);
		case 5: return new BedEntity(pos, rot);
		case 6: return new ForgeEntity(pos, rot);
		}
	
		return null;
	}

	public static int getId(Class<? extends Entity> c) {
		return map.get(c.getSimpleName());
	}
	
	public static int getId(String s) {
		return map.containsKey(s) ? map.get(s) : 0;
	}
	
	private static void initModelsAndTextures() {
		Resources.addModel("firefly", "entity/firefly.mod");
		Resources.addModel("door", "entity/door.mod");
		Resources.addModel("campfire", "entity/campfire.mod");
		Resources.addModel("bed", "entity/bed.mod");
		Resources.addModel("forge", "entity/forge.mod");
		
		Resources.addSound("fire", "fire.ogg");
		
		Resources.addTexture("entity_sheet1", "entity/entity_sheet1.png");
		Resources.addTexture("campfire", "entity/campfire.png");
		Resources.addTexture("bed", "entity/bed.png");
	}
	
	static void cleanUp() {
		Resources.getModel("firefly").cleanUp();
		Resources.getModel("door").cleanUp();
		Resources.getModel("campfire").cleanUp();
		Resources.getModel("bed").cleanUp();
		Resources.getModel("forge").cleanUp();
		
		Resources.removeSound("fire");
		
		Resources.getTexture("entity_sheet1").delete();
		Resources.getTexture("campfire").delete();
		Resources.getTexture("bed").delete();
	}
}
