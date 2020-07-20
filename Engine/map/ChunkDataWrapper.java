package map;

import java.util.Collection;

import map.prop.Props;
import map.tile.BuildSector;

public class ChunkDataWrapper {
	public int x, z, arrX, arrZ;
	public float[][] heightmap;
	public Props[][] tilemap;
	//public float[][] waterTable;
	public BuildSector[] sectors;
	public byte editFlags = 1;
	
	public byte state = 0;
	
	public ChunkDataWrapper(Chunk chunk) {
		this.x = chunk.dataX;
		this.z = chunk.dataZ;
		this.arrX = chunk.arrX;
		this.arrZ = chunk.arrZ;
		
		this.heightmap = chunk.heightmap.clone();
		this.tilemap = chunk.getProps().getPropMap().clone();
		Collection<BuildSector> sectorCollection = chunk.getBuilding().getSectors();
		this.sectors = new BuildSector[sectorCollection.size()];
		int i = 0;
		for(BuildSector sector : sectorCollection) {
			sectors[i++] = sector;
		}
	}

	public void setState(byte state) {
		this.state = state;
	}
}
