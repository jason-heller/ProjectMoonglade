package procedural.biome.types;

import org.joml.Vector3f;

import util.MathUtil;

public class BiomeColors {
	// DIRT
	public static final Vector3f DIRT_COLOR = rgb(169, 100, 55);
	public static final Vector3f MUD_COLOR = rgb(94, 63, 29);
	public static final Vector3f SWAMP_MUD = rgb(94, 120, 53);
	public static final Vector3f ROCK_COLOR = new Vector3f(0.3f, 0.3f, 0.4f);
	
	// SKY
	public static final Vector3f DEFAULT_SKY = new Vector3f(1,1,1);
	public static final Vector3f DESERT_SKY = new Vector3f(1, 1, .5f);
	public static final Vector3f TROPIC_SKY = new Vector3f(1, 1, 1.2f);
	public static final Vector3f SNOW_SKY = new Vector3f(.8f, 1f, .8f);
	public static final Vector3f SWAMP_SKY = rgb(244, 255, 219);
	
	// GROUND
	public static final Vector3f DEFAULT_GRASS = rgb(77, 178, 21);
	public static final Vector3f SAND_COLOR = rgb(239, 156, 112);//rgb(255, 224, 67);
	public static final Vector3f SNOW_COLOR = rgb(237, 254, 255);
	public static final Vector3f DECIDUOUSFOREST_GRASS = rgb(94, 137, 21);
	public static final Vector3f FEN_GRASS = rgb(118, 145, 42);
	public static final Vector3f CONIFERFOREST_GRASS = rgb(56, 168, 118);
	public static final Vector3f TROPIC_GRASS_COLOR = rgb(56, 173, 9);
	public static final Vector3f TAIGA_GRASS = rgb(151, 170, 97);
	
	// WATER
	public static final Vector3f DEFAULT_WATER = rgb(12, 92, 247);
	public static final Vector3f SWAMP_WATER = rgb(90, 158, 112);
	public static final Vector3f CHAPARREL_GRASS = rgb(150, 115, 100);
	
	private static Vector3f rgb(float r, float g, float b) {
		return MathUtil.rgb(r,g,b);
	}
}
