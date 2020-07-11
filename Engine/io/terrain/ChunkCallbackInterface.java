package io.terrain;

import java.util.List;

import map.Chunk;
import scene.entity.Entity;

public interface ChunkCallbackInterface {
	void saveCallback();
	void loadCallback(List<Entity> entities);
}
