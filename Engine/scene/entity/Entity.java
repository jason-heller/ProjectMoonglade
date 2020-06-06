package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import anim.Animator;
import core.Resources;
import core.res.Model;
import core.res.Texture;
import scene.Scene;

public class Entity {
	public Vector3f position = new Vector3f(), rotation = new Vector3f(), velocity = new Vector3f();
	protected Model model;
	protected Texture diffuse;
	protected Matrix4f matrix;
	protected Animator animator;
	protected float scale;
	
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
		if (model != null) {
			EntityControl.addEntity(this);
		}
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
	}

	public void destroy() {
		EntityControl.removeEntity(this);
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
}
