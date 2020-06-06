package procedural.biome.types;

import org.joml.Vector3f;

public class BiomeColors {
	// DIRT
	public static final Vector3f DIRT_COLOR = rgb(169, 100, 55);
	public static final Vector3f ROCK_COLOR = new Vector3f(0.3f, 0.3f, 0.4f);
	
	// SKY
	public static final Vector3f DEFAULT_SKY = new Vector3f(1,1,1);
	static final Vector3f DESERT_SKY = new Vector3f(1, 1, .5f);
	
	// GROUND
	public static final Vector3f DEFAULT_GRASS = rgb(77, 178, 21);
	static final Vector3f SAND_COLOR = rgb(239, 156, 112);//rgb(255, 224, 67);
	static final Vector3f SNOW_COLOR = rgb(237, 254, 255);
	static final Vector3f DECIDUOUSFOREST_GRASS = rgb(94, 137, 21);
	static final Vector3f FEN_GRASS = rgb(118, 145, 42);
	static final Vector3f CONIFERFOREST_GRASS = rgb(56, 168, 118);
	
	private static Vector3f rgb(float r, float g, float b) {
		return new Vector3f(r/255f, g/255f, b/255f);
	}
}
