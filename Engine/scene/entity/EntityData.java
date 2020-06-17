package scene.entity;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import scene.entity.friendly.FireflyEntity;
import scene.entity.object.DoorEntity;
import scene.entity.utility.ItemEntity;
import scene.overworld.inventory.Item;

public class EntityData {
	private static final Map<String, Integer> map = new HashMap<String, Integer>();
	
	public static void init() {
		map.put(ItemEntity.class.getSimpleName(), 1);
		map.put(FireflyEntity.class.getSimpleName(), 2);
		map.put(DoorEntity.class.getSimpleName(), 3);
		
		initModelsAndTextures();
	}

	public static Entity instantiate(int id) {
		switch(id) {
		case 1: return new ItemEntity(new Vector3f(), Item.AIR, 0);
		case 2: return new FireflyEntity(Application.scene);
		case 3: return new DoorEntity(new Vector3f(), 0);
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
		
		Resources.addTexture("entity_sheet1", "entity/entity_sheet1.png");
	}
	
	static void cleanUp() {
		Resources.getModel("firefly").cleanUp();
		Resources.getModel("door").cleanUp();
		Resources.getTexture("entity_sheet1").delete();
	}
}
