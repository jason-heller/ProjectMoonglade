package scene.entity.utility;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import gl.Window;
import map.Chunk;
import map.Terrain;
import map.tile.EnvTile;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.EntityControl;
import scene.overworld.Overworld;
import scene.overworld.inventory.Item;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class FallingTreeEntity extends Entity {
	private float fallX = 0, fallZ = 0, fallSpeed = 0, animTime = 0;
	
	private Vector3f bounds;
	private boolean fallDirZ = false;

	private int fallDirPos;
	private Item drop;

	public FallingTreeEntity(EnvTile tile, float x, float y, float z, float scale) {
		super();
		this.position.set(x, y, z);
		this.scale = scale;

		this.setModel(tile.getModel().toNonTileModel());
		this.setDiffuse(Resources.getTexture("fauna"));
		this.bounds = tile.getBounds();
		
		drop = tile.getDrop();

		fallDirZ = (float)Math.random() > 0.5;
		fallDirPos = (Math.random() > 0.5) ? 1 : -1;
		
		source.play(Resources.getSound("tree_fall"));

		EntityControl.addEntity(this);
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}

	@Override
	public void tick(Scene scene) {
		Overworld ow = (Overworld)scene;
		//Terrain t = ow.getEnviroment().getTerrain();
		if (Math.abs(fallX) < 90 && Math.abs(fallZ) < 90) {
			
			animTime += (Window.deltaTime * .00009f);
			fallSpeed += animTime*9;
			if (fallDirZ) {
				fallZ = 90 * fallSpeed * fallDirPos;
			} else {
				fallX = 90 * fallSpeed * fallDirPos;
			}
			
			/*Matrix4f up = new Matrix4f(this.getMatrix());
			up.rotateX(-90);
			Vector3f dir = new Vector3f(-up.m02, -up.m12, -up.m22);
			TerrainIntersection ti = t.terrainRaycast(Vector3f.add(position, v2), dir, bounds.y);
			
			if (ti != null) {
				this.destroy();
			}*/
		} else {

			if (ow.getPlayer().position.distance(position) < (bounds.y*scale) * 5) {
				ow.getCamera().shake(.1f, 2f);
			}
			
			this.destroy();
			
			if (drop != Item.AIR) {
				Matrix4f up = new Matrix4f();
				if (fallDirZ) {
					up.rotateY(fallZ+180);
				} else {
					up.rotateY(fallX+90);
				}
				Vector3f dir = new Vector3f(-up.m02, -up.m12, -up.m22);
				for (float i = 0; i <= bounds.y*scale; i += 0.34f) {
					Vector3f pos = Vector3f.add(position, Vector3f.mul(dir, i*3f));
					EntityControl.addEntity(new ItemEntity(pos, drop, 1));
				}
			}
		}

		this.rotation.x = fallX;
		this.rotation.z = fallZ;
	}

	@Override
	public void save(RunLengthOutputStream data) {
	}

	@Override
	public void load(RunLengthInputStream data) {
	}
}
