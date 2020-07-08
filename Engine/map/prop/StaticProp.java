package map.prop;

import org.joml.Vector3f;

import gl.res.PropModel;
import map.Material;
import scene.overworld.inventory.Item;

public class StaticProp {
	private PropModel model;
	private int drop;
	private int dropAmt;
	private Material material;
	private byte strength;
	private boolean alwaysDrop;
	private int tool = 0;
	
	public StaticProp(PropModel model, Material material, int drop, int dropAmt, byte strength, boolean alwaysDrop) {
		this(model, material, drop, dropAmt, strength, alwaysDrop, 0);
	}
	
	public StaticProp(PropModel model, Material material, int drop, int dropAmt, byte strength, boolean alwaysDrop, int tool) {
		this.model = model;
		this.drop = drop;
		this.material = material;
		this.dropAmt = dropAmt;
		this.strength = strength;
		this.alwaysDrop = alwaysDrop;
		this.tool = tool;
	}

	public int getNumDrops() {
		return (int) ((alwaysDrop) ? dropAmt : Math.round(Math.random() * dropAmt));
	}
	
	public int getDrop() {
		return drop;
	}
	
	public PropModel getModel() {
		return model;
	}

	public Vector3f getBounds() {
		return model.getBounds();
	}
	
	public int getTool() {
		return tool;
	}
	
	public Material getMaterial() {
		return material;
	}

	public byte getStrength() {
		return strength;
	}

	public boolean isDestroyableBy(int item) {
		if (tool == item)
			return true;
		
		switch(tool) {
		case Item.AXE:
			return false;
		case Item.SPADE:
			if (item == Item.TROWEL)
				return true;
			return false;
		case Item.TROWEL:
			if (item == Item.SPADE)
				return true;
			return false;
		default:
			return (item != Item.AXE);
		}
	}
}
