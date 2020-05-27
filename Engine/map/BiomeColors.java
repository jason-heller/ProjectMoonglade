package map;

import org.joml.Vector3f;

public class BiomeColors {
	// DIRT
	public static final Vector3f DIRT_COLOR = rgb(169, 100, 55);
	
	// SKY
	static final Vector3f DEFAULT_SKY = new Vector3f(1,1,1);
	static final Vector3f DESERT_SKY = new Vector3f(1, 1, .5f);
	
	// GROUND
	static final Vector3f DEFAULT_GRASS = rgb(59, 189, 85);
	static final Vector3f SAND_COLOR = rgb(255, 224, 67);
	static final Vector3f SNOW_COLOR = rgb(237, 254, 255);
	
	private static Vector3f rgb(float r, float g, float b) {
		return new Vector3f(r/255f, g/255f, b/255f);
	}
}
