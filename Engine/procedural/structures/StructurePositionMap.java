package procedural.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructurePositionMap {
	private Map<Integer, Map<Integer, List<StructPlacement>>>[] data;
	
	@SuppressWarnings("unchecked")
	public StructurePositionMap() {
		int numQuadrants = StructureSpawner.quadrants.length;
		data = new HashMap[numQuadrants];
		
		for(int i = 0; i < numQuadrants; i++) {
			data[i] = new HashMap<Integer, Map<Integer, List<StructPlacement>>>();
		}
	}

	public Set<Integer> keySet(int quadrant) {
		return data[quadrant].keySet();
	}

	public Map<Integer, List<StructPlacement>> get(int quadrant, int index) {
		return data[quadrant].get(index);
	}

	public void remove(int quadrant, int index) {
		data[quadrant].remove(index);
	}
	
	public void put(int quadrant, int position, Map<Integer, List<StructPlacement>> object) {
		data[quadrant].put(position, object);
	}

	public void clear(int quadrant) {
		data[quadrant].clear();
	}
}
