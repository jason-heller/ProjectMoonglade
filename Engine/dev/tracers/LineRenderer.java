package dev.tracers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import dev.Debug;
import gl.Camera;
import gl.res.Model;

public class LineRenderer {
	private static LineShader shader;
	private static List<Vector3f> points;
	private static List<Vector3f> colors;
	private static Model line;
	private static boolean active = false;

	public static void addPoints(Vector3f point1, Vector3f point2) {
		points.add(point1);
		points.add(point2);
		colors.add(new Vector3f(0, 0, 0));
	}

	public static void addPoints(Vector3f point1, Vector3f point2, Vector3f color) {
		points.add(point1);
		points.add(point2);
		colors.add(color);
	}

	public static void disable() {
		if (active) {
			points.clear();
			line.cleanUp();
			shader.cleanUp();
			active = false;
		}
	}

	public static void init() {
		shader = new LineShader();
		points = new ArrayList<Vector3f>();
		colors = new ArrayList<Vector3f>();
		makeLineVao();
		active = true;
	}

	private static void makeLineVao() {
		line = Model.create();
		line.bind();
		line.createAttribute(0, new float[] { 0, 0, 0, 0, 0, 0 }, 3); // -0.5f,-0.5f, 0.5f,-0.5f, -0.5f,0.5f, 0.5f,0.5f
		line.unbind();
	}

	public static void render(Camera cam) {
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind();
		GL20.glEnableVertexAttribArray(0);
		shader.projectionViewMatrix.loadMatrix(cam.getProjectionViewMatrix());
		int j = 0;
		for (int i = 0; i < points.size(); i += 2) {
			shader.color.loadVec3(colors.get(j++));
			shader.point1.loadVec3(points.get(i));
			shader.point2.loadVec3(points.get(i + 1));
			GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		}
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL20.glDisableVertexAttribArray(0);
		line.unbind();
		shader.stop();
	}

	public static void render(Camera cam, Vector3f p1, Vector3f p2) {
		if (!Debug.debugMode) {
			return;
		}
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind();
		GL20.glEnableVertexAttribArray(0);
		shader.projectionViewMatrix.loadMatrix(cam.getProjectionViewMatrix());
		shader.color.loadVec3(1, 0, 1);
		shader.point1.loadVec3(p1);
		shader.point2.loadVec3(p2);
		GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL20.glDisableVertexAttribArray(0);
		line.unbind();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		shader.stop();
	}

	public static void render(Camera cam, Vector3f p1, Vector3f p2, Vector3f color) {
		if (!Debug.debugMode) {
			return;
		}
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind();
		GL20.glEnableVertexAttribArray(0);
		shader.projectionViewMatrix.loadMatrix(cam.getProjectionViewMatrix());
		shader.color.loadVec3(color);
		shader.point1.loadVec3(p1);
		shader.point2.loadVec3(p2);
		GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL20.glDisableVertexAttribArray(0);
		line.unbind();
		shader.stop();
	}

	public static void render(Camera camera, Vector4f p1, Vector4f p2) {
		if (!Debug.debugMode) {
			return;
		}
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		line.bind();
		GL20.glEnableVertexAttribArray(0);
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.color.loadVec3(1, 0, 1);
		shader.point1.loadVec3(p1.x, p1.y, p1.z);
		shader.point2.loadVec3(p2.x, p2.y, p2.z);
		GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		// GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL20.glDisableVertexAttribArray(0);
		line.unbind();
		shader.stop();
	}
}
