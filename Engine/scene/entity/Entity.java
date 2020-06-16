package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import anim.Animator;
import audio.AudioHandler;
import audio.Source;
import core.Resources;
import core.res.Model;
import core.res.Texture;
import map.Chunk;
import scene.Scene;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;

public class Entity {
	public Vector3f position = new Vector3f(), rotation = new Vector3f(), velocity = new Vector3f();
	protected Model model;
	protected Texture diffuse;
	protected Matrix4f matrix;
	protected Animator animator;
	
	protected Source source;
	
	protected int id = 0;
	
	protected Chunk chunk;
	protected float scale;
	
	protected boolean deactivated = false; // If deactivated, still exists in-game, but does not update or render
	protected int persistency = 0;	// 0 = despawn as soon as can, 1 = despawn at range or after period of time saved in chunk data(todo)
									// 2 = never despawn, 3 = never despawn, stays even if current chunk unloads
	
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
		this.model = model == null ? null : Resources.getModel(model);
		this.diffuse = diffuse == null ? null : Resources.getTexture(diffuse);
		source = new Source();
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
	
	public void tick(Scene scene) {
		matrix.identity();
		matrix.translate(position);
		matrix.rotate(rotation);
		matrix.scale(scale);
	}

	public void destroy() {
		EntityControl.removeEntity(this);
		AudioHandler.deleteSource(source);
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
	
	public void save(RunLengthOutputStream data) {}
	public void load(RunLengthInputStream data) {}

}
