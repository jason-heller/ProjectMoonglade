package map.tile;

public class TileProperties {
	public float dx, dz, scale;
	public byte damage;
	
	public TileProperties(float dx, float dz, float scale) {
		this.dx = dx;
		this.dz = dz;
		this.scale = scale;
	}
}