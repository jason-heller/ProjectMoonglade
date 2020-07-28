package procedural.structures;

import java.util.Random;

public class GenGlobalStructures {

	public static Structure getTerrainStructures(int x, int z, float currentHeight, Random r, int quadrantSize) {
		switch(quadrantSize) {
		case 1:
			if (r.nextInt(15000) == 0) {
				return Structure.PYLON;
			}
			break;
		}
		
		return null;
	}

}
