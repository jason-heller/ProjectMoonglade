package procedural.terrain;

import java.util.Random;

import procedural.structures.Structure;

public class GenGlobalStructures {

	public static Structure getTerrainStructures(int x, int z, float currentHeight, float waterHeight, int subseed, Random r) {
		if (currentHeight >= waterHeight) {
			if (r.nextInt(10000) == 0) {
				return Structure.PYLON;
			}
		}
		
		return null;
	}

}
