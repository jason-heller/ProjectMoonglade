package scene.entity.friendly;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import gl.Window;
import gl.particle.Particle;
import map.Chunk;
import map.Enviroment;
import map.Temperature;
import map.Terrain;
import procedural.biome.Biome;
import scene.Scene;
import scene.entity.Entity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class FireflyEntity extends Entity {
	
	private final float flyYawDiff = .04f;
	private final float flyAltDiff = .01f;
	
	private float thinkTimer = 0f, hoverTimer = 0f, blinkTimer = 0f;
	private final float decisionTime = 6.28f;//12.56f;
	private float y = 0;

	public FireflyEntity(Scene scene) {
		super("firefly", "entity_sheet1");
		scale = .25f;
		blinkTimer = 3 + (float)Math.random() * 7;
		this.spawnGroupMin = 2;
		this.spawnGroupVariation = 3;
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}

	@Override
	public void tick(Scene scene) {
		if (this.getChunk() == null) return;
		
		thinkTimer += Window.deltaTime*2;
		blinkTimer -= Window.deltaTime*2;
		
		if (blinkTimer < .3f && scale > 0) {
			this.scale = -scale;
			float life = 10f + ((float)Math.random()*20f);
			new Particle(Resources.getTexture("particles"), position, new Vector3f(), 0f, life, 0, 0, .5f, 2, 2);
		}
		
		if (blinkTimer <= 0) {
			this.scale = -scale;
			blinkTimer = 3 + (float)Math.random() * 7;
		}
		
		float floor = chunk.heightLookup((int)position.x-chunk.realX, (int)position.z-chunk.realZ);
		float hoverY = (float)Math.sin(hoverTimer);
		
		
		if (y > floor + 6) {
			y = floor + 6;
		}
		
		if (y+hoverY <= floor) {
			y = floor;
			velocity.zero();
			
			if (thinkTimer > decisionTime) {
				thinkTimer = 0;
				velocity.x = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
				velocity.z = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
				velocity.y = ((float)Math.random()*flyAltDiff);
			}
		} else if (thinkTimer > decisionTime) {
			velocity.x = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
			velocity.z = (-flyYawDiff/2f) + ((float)Math.random()*flyYawDiff);
			velocity.y = (-flyAltDiff/2f) + ((float)Math.random()*flyAltDiff);
			
			thinkTimer = 0f;
		} else {
			hoverTimer += Window.deltaTime*2;
			position.y = y + hoverY;
		}

		position.x += velocity.x;
		position.z += velocity.z;
		y += velocity.y;
		
		if (Enviroment.exactTime > Enviroment.NIGHT) {
			destroy();
		}
	}
	
	@Override
	public void save(RunLengthOutputStream data) {
	}

	@Override
	public void load(RunLengthInputStream data) {
	}

	@Override
	public boolean spawnConditionsMet(Enviroment enviroment, Terrain terrain, Chunk chunk, float x, float z, int dx, int dy, int dz) {
		//final int tileId = chunk.getTileItems().getTileId(dx, dz);
		//EnvTile envTile = terrain.getTileById(tileId);
		//BuildingTile buildingTile = chunk.getBuilding().get(dx, dy+2, dz);
		Biome biome = enviroment.getBiomeVoronoi().getDataAt(x, z).getMainBiome();
		
		if (biome.getTemperature() != Temperature.TEMPERATE)
			return false;
		
		return (Enviroment.exactTime > Enviroment.DUSK && Enviroment.exactTime < Enviroment.NIGHT);
	}
}
