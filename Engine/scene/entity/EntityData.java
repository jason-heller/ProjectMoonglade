package scene.entity;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import core.Application;
import core.Resources;
import scene.overworld.FireflyEntity;
import scene.overworld.inventory.Item;

public enum EntityData {
	data;
	
	private final Map<String, Integer> map = new HashMap<String, Integer>();
	
	EntityData() {
		map.put(ItemEntity.class.getSimpleName(), 1);
		map.put(FireflyEntity.class.getSimpleName(), 2);
	}

	public Entity instantiate(int id) {
		switch(id) {
		case 1: return new ItemEntity(new Vector3f(), Item.AIR, 0);
		case 2: return new FireflyEntity(Application.scene);
		}
	
		return null;
	}

	public int getId(Class<? extends Entity> c) {
		return map.get(c.getSimpleName());
	}
	
	public int getId(String s) {
		return map.containsKey(s) ? map.get(s) : 0;
	}
	
	void initModelsAndTextures() {
		Resources.addModel("firefly", "entity/firefly.mod");
		
		Resources.addTexture("entity_sheet1", "entity/entity_sheet1.png");
	}
	
	void cleanUp() {
		Resources.getModel("firefly").cleanUp();
		Resources.getTexture("entity_sheet1").delete();
	}
}
