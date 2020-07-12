package scene.overworld.inventory;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Resources;
import gl.Camera;
import gl.Window;
import gl.res.Model;
import gl.res.Texture;
import io.Input;
import scene.entity.EntityHandler;
import util.MathUtil;

public class Viewmodel {
	private Texture texture;
	private Model model;
	private Matrix4f matrix;
	private int item;

	private float xWag, zWag;
	
	private boolean swingAnim = false;
	private float xRot = 0, xRotTarget = 0;

	public Viewmodel() {
		model = null;
		matrix = new Matrix4f();
	}

	public void set(int item) {
		this.item = item;
		switch (item) {
		case Item.AXE:
			texture = Resources.getTexture("tools");
			model = Resources.getModel("axe");
			break;
		case Item.SPADE:
			texture = Resources.getTexture("tools");
			model = Resources.getModel("spade");
			break;
		case Item.TROWEL:
			texture = Resources.getTexture("tools");
			model = Resources.getModel("spade");
			break;

		default:
			model = null;
		}
	}

	public void update() {
		if (Input.isDown("walk_left") || Input.isDown("walk_right") || Input.isDown("walk_forward")
				|| Input.isDown("walk_backward")) {
			xWag += Window.deltaTime*5;
			zWag += Window.deltaTime*5;
		}
		
		if (Input.isPressed(Input.KEY_LMB)) {
			swingAnim = true;
			xRot = 0;
			xRotTarget = -90;
		}
		
		if (swingAnim) {
			if (xRotTarget < xRot) {
				xRot -= 1000*Window.deltaTime;
				
				if (xRot < xRotTarget) {
					xRotTarget = 0;
				}
			} else {
				xRot += 2500 * Window.deltaTime;
				
				if (xRot >= xRotTarget) {
					xRot = 0;
					swingAnim = false;
				}
			}
		}

		matrix.identity();
		matrix.translate(.45f + (float)Math.sin(xWag)*.005f, -.36f, -.5f + (float)Math.cos(zWag)*.015f);
		
		// TODO: ACTUAL FUCKIN ANIMATIONS
		if (item == Item.SPADE || item == Item.TROWEL) {
			matrix.rotateX(-xRot+20);
		} else {
			matrix.rotateX(xRot);
		}
		
		if (item == Item.TROWEL) {
			matrix.scale(.4f);
		}
		
		matrix.scale(.2f);
	}

	public void render(Camera camera, Vector3f lightDir) {
		if (model != null) {
			EntityHandler.renderViewmodel(camera, lightDir, model, texture, matrix);
		}
	}
}