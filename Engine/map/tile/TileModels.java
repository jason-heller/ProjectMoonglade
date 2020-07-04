package map.tile;

import gl.res.TileModel;
import io.TileFileLoader;

public enum TileModels {
	DEFAULT, FILLED;
	
	private TileModel model;

	public static void init() {
		DEFAULT.setModel(TileFileLoader.readTileFile("template.til"));
		FILLED.setModel(TileFileLoader.readTileFile("filled.til"));
	}
	
	private void setModel(TileModel model) {
		this.model = model;
	}

	public TileModel getModel() {
		return model;
	}
}
