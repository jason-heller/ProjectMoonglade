package util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import gl.Camera;
import map.Terrain;

public class HeightmapRaycaster {

	private static final int RECURSION_COUNT = 200;
	private static final float RAY_RANGE = 600;

	public static Vector3f raycast(Camera camera, Terrain terrain) {
		Vector3f currentRay = calculateMouseRay(camera.getViewMatrix(), camera.getProjectionMatrix());
		Vector3f currentTerrainPoint;
		if (intersectionInRange(camera, terrain, 0, RAY_RANGE, currentRay)) {
			currentTerrainPoint = binarySearch(camera, terrain, 0, 0, RAY_RANGE, currentRay);
		} else {
			currentTerrainPoint = null;
		}
		
		return currentTerrainPoint;
	}

	private static Vector3f calculateMouseRay(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		float mouseX = Mouse.getX();
		float mouseY = Mouse.getY();
		Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
		Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
		Vector4f eyeCoords = toEyeCoords(clipCoords, projectionMatrix);
		Vector3f worldRay = toWorldCoords(eyeCoords, viewMatrix);
		return worldRay;
	}

	private static Vector3f toWorldCoords(Vector4f eyeCoords, Matrix4f viewMatrix) {
		Matrix4f invertedView = new Matrix4f();
		Matrix4f.invert(viewMatrix, invertedView);
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords);
		Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		mouseRay.normalize();
		return mouseRay;
	}

	private static Vector4f toEyeCoords(Vector4f clipCoords, Matrix4f projectionMatrix) {
		Matrix4f invertedProjection = new Matrix4f();
		Matrix4f.invert(projectionMatrix, invertedProjection);
		Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords);
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}

	private static Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY) {
		float x = (2.0f * mouseX) / Display.getWidth() - 1f;
		float y = (2.0f * mouseY) / Display.getHeight() - 1f;
		return new Vector2f(x, y);
	}
	
	//**********************************************************
	
	private static Vector3f getPointOnRay(Camera camera, Vector3f ray, float distance) {
		Vector3f camPos = camera.getPosition();
		Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
		Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
		return Vector3f.add(start, scaledRay);
	}
	
	private static Vector3f binarySearch(Camera camera, Terrain terrain, int count, float start, float finish, Vector3f ray) {
		float half = start + ((finish - start) / 2f);
		if (count >= RECURSION_COUNT) {
			Vector3f endPoint = getPointOnRay(camera, ray, half);
			if (terrain != null) {
				return endPoint;
			} else {
				return null;
			}
		}
		if (intersectionInRange(camera, terrain, start, half, ray)) {
			return binarySearch(camera, terrain, count + 1, start, half, ray);
		} else {
			return binarySearch(camera, terrain, count + 1, half, finish, ray);
		}
	}

	private static boolean intersectionInRange(Camera camera, Terrain terrain, float start, float finish, Vector3f ray) {
		Vector3f startPoint = getPointOnRay(camera, ray, start);
		Vector3f endPoint = getPointOnRay(camera, ray, finish);
		if (!isUnderGround(terrain, startPoint) && isUnderGround(terrain, endPoint)) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isUnderGround(Terrain terrain, Vector3f testPoint) {
		float height = 0;
		if (terrain != null) {
			height = terrain.getHeightAt(testPoint.x, testPoint.z);
			
		}
		if (testPoint.y < height) {
			return true;
		} else {
			return false;
		}
	}

}