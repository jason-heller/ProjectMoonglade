package io.terrain;

import map.Chunk;

public interface ChunkCallbackInterface {
	void saveCallback(Chunk chunk);
	void loadCallback(Chunk chunk);
}
