package map.tile;

import core.res.TileableModel;
import scene.overworld.inventory.Item;

public class EnvTile {
	private TileableModel model;
	private Item drop;
	private int dropAmt;
	private boolean alwaysDrop;
	
	public EnvTile(TileableModel model, Item drop, int dropAmt, boolean alwaysDrop) {
		this.model = model;
		this.drop = drop;
		this.dropAmt = dropAmt;
		this.alwaysDrop = alwaysDrop;
	}

	public int getNumDrops() {
		return (int) ((alwaysDrop) ? dropAmt : Math.round(Math.random() * dropAmt));
	}
	
	public Item getDrop() {
		return drop;
	}
	
	public TileableModel getModel() {
		return model;
	}
}
