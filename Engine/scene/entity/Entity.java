package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import geom.AABB;
import gl.Window;
import gl.anim.Animator;
import gl.res.Model;
import gl.res.Texture;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import scene.Scene;
import scene.overworld.inventory.Inventory;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public abstract class Entity {
	public Vector3f position = new Vector3f(), rotation = new Vector3f(), velocity = new Vector3f();
	protected Model model;
	protected Texture diffuse;
	protected Matrix4f matrix;
	protected Animator animator;
	
	protected AABB aabb;
	
	protected int id = 0;
	
	protected Chunk chunk;
	protected float scale;
	
	protected int hp = 0;
	protected float invulnerabilityTimer = 0f;
	
	protected boolean clickable = false;
	
	public boolean deactivated = false; // If deactivated, still exists in-game, but does not update or render
	protected int persistency = 0;	// 0 = despawn as soon as can, 1 = despawn at range or after period of time saved in chunk data(todo)
									// 2 = never despawn, 3 = never despawn, stays even if current chunk unloads
	
	protected int spawnGroupMin = 1;
	protected int spawnGroupVariation = 1;
	protected int spawnRarity = 0;
	
	public Entity() {
		this(null, null);
	}

	public Entity(String model, String diffuse) {
		position = new Vector3f();
		rotation = new Vector3f();
		velocity = new Vector3f();
		scale = 1f;
		visible = true;
		matrix = new Matrix4f();
		if (model == null) {
			this.model = null;
		} else {
			this.model = Resources.getModel(model);
			if (this.model.getSkeleton() != null) {
				animator = new Animator(this.model, this);
			}
			
		}
		
		this.diffuse = diffuse == null ? null : Resources.getTexture(diffuse);
		
	}
	
	public boolean visible = true;
	
	public Model getModel() {
		return model;
	}

	public Matrix4f getMatrix() {
		return matrix;
	}

	public Animator getAnimator() {
		return animator;
	}

	public Texture getDiffuse() {
		return diffuse;
	}
	
	public void update(Scene scene) {
		matrix.identity();
		matrix.translate(position);
		matrix.rotate(rotation);
		matrix.scale(scale);
		
		invulnerabilityTimer = Math.max(invulnerabilityTimer - Window.deltaTime, 0f);
	}
	
	public void tick(Scene scene) {
	}

	public void destroy() {
		if (animator != null) {
			animator.destroy();
		}
		EntityHandler.removeEntity(this);
	}

	public void setDiffuse(Texture diffuse) {
		this.diffuse = diffuse;
	}

	public void setMatrix(Matrix4f matrix) {
		this.matrix = matrix;
	}

	public void setMatrix(Vector3f position) {
		//
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Chunk getChunk() {
		return chunk;
	}
	
	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}

	public int getPersistency() {
		return persistency;
	}
	
	public abstract void save(RunLengthOutputStream data);
	public abstract void load(RunLengthInputStream data);
	public boolean spawnConditionsMet(Enviroment enviroment, Terrain terrain, Chunk chunk, float x, float z, int dx, float dy, int dz) {
		return false;
	}

	public int getSpawnGroupMin() {
		return this.spawnGroupMin;
	}
	
	public int getSpawnGroupVariation() {
		return this.spawnGroupVariation;
	}
	
	public void setHp(int hp) {
		this.hp = hp;
	}
	
	public boolean isClickable() {
		return clickable;
	}

	protected void onClick(boolean lmb, Inventory inv) {}

	public AABB getAabb() {
		return aabb;
	}
	
	public boolean isAnimated() {
		return (animator != null);
	}
	
	public int getHp() {
		return hp;
	}
	
	public void heal(int heal) {
		hp += heal;
	}
	
	public void hurt(int damage, Entity attacker) {
		hurt( damage, attacker, 1f);
	}
	
	public void hurt(int  damage, Entity attacker, float invulnerabiltiyTime) {
		if (invulnerabilityTimer == 0) {
			hp -=  damage;
			invulnerabilityTimer = invulnerabiltiyTime;
		}
		
		if (hp <= 0) {
			die();
		}
		
	}

	protected void die() {
		this.destroy();
	}

	public int getSpawnRarity() {
		return spawnRarity;
	}
}
