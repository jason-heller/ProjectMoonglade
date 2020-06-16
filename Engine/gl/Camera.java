package gl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.Console;
import geom.Frustum;
import io.Input;
import scene.entity.Entity;
import util.MathUtil;

public class Camera {
	private static final float MAX_PITCH = 90;

	private static float zoom, targetZoom, zoomSpeed;

	public static float cameraSpeed = .5f;

	public static int fov = 90;

	public static float mouseSensitivity = 1f;
	public static final float FAR_PLANE = 4500f;

	public static final float NEAR_PLANE = .1f;
	public static final byte NO_CONTROL = 0, SPECTATOR = 1, FIRST_PERSON = 2;

	private static Matrix4f createProjectionMatrix() {
		final Matrix4f projectionMatrix = new Matrix4f();
		final float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		final float y_scale = (float) (1f / Math.tan(Math.toRadians((fov - zoom) / 2f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -(2 * NEAR_PLANE * FAR_PLANE / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}

	private Matrix4f projectionMatrix;
	private final Matrix4f projectionViewMatrix = new Matrix4f();
	private final Matrix4f viewMatrix = new Matrix4f();
	private final Vector3f position = new Vector3f();
	private final Frustum frustum = new Frustum();
	private float yaw;
	private float pitch;
	private float roll;

	private float angleAroundPlayer;

	private float shakeTime = 0f, shakeIntensity = 0f;

	private final Vector2f screenShake = new Vector2f();

	private Vector3f lookAt = null;
	private Vector3f viewDirection = new Vector3f();

	private Entity focus = null;

	private final boolean mouseIsGrabbed = false;

	private byte controlStyle = FIRST_PERSON;

	public Camera() {
		updateProjection();
	}

	public void addPitch(float f) {
		pitch += f;
	}

	public void addYaw(float f) {
		angleAroundPlayer += f;
	}

	private void clampPitch() {
		if (pitch < -MAX_PITCH) {
			pitch = -MAX_PITCH;
		} else if (pitch > MAX_PITCH) {
			pitch = MAX_PITCH;
		}
	}

	public void focusOn(Entity focus) {
		this.focus = focus;
		if (focus == null) {
			lookAt = null;
		}
	}

	public byte getControlStyle() {
		return controlStyle;
	}

	public Vector3f getDirectionVector() {
		return viewDirection;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public float getPitch() {
		return pitch;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4f getProjectionViewMatrix() {
		return projectionViewMatrix;
	}

	public Vector2f getScreenShake() {
		return screenShake;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public float getYaw() {
		return yaw;
	}

	public void grabMouse() {
		Input.requestMouseGrab();
	}

	private void handleControl() {
		// Yaw/pitch look
		if (controlStyle == SPECTATOR || controlStyle == FIRST_PERSON) {
			if (Mouse.isGrabbed()) {
				final float offset = 10f;
				final float pitchChange = Input.getMouseDY() * (mouseSensitivity / offset);
				final float angleChange = Input.getMouseDX() * (mouseSensitivity / offset);
				pitch -= pitchChange;
				angleAroundPlayer -= angleChange;
				clampPitch();
			}
		}

		// WASD movement
		if (controlStyle == SPECTATOR && !Console.isVisible()) {
			final Vector3f foward = MathUtil.getDirection(viewMatrix);
			final float yawRad = (float) Math.toRadians(yaw);
			final Vector3f strafe = new Vector3f(-(float) Math.sin(yawRad), 0, (float) Math.cos(yawRad))
					.perpindicular();

			final float speed = Input.isDown(Keyboard.KEY_LCONTROL) ? cameraSpeed / 4f : cameraSpeed;

			if (Input.isDown("walk_forward")) {
				foward.mul(-speed);
			} else if (Input.isDown("walk_backward")) {
				foward.mul(speed);
			} else {
				foward.zero();
			}

			if (Input.isDown("walk_right")) {
				strafe.mul(-speed);
			} else if (Input.isDown("walk_left")) {
				strafe.mul(speed);
			} else {
				strafe.zero();
			}

			position.add(foward).add(strafe);
		}
	}

	public boolean isMouseGrabbed() {
		return mouseIsGrabbed;
	}

	public boolean isShaking() {
		return shakeTime == 0f;
	}

	public void move() {
		if (Math.abs(targetZoom - zoom) > .2f) {
			zoom += zoomSpeed;
			updateProjection();
		}

		if (shakeTime > 0) {
			shakeTime = Math.max(shakeTime - Window.deltaTime, 0f);
			if (shakeTime == 0) {
				screenShake.zero();
			} else {
				screenShake.set(-(shakeIntensity / 2f) + (float) (Math.random() * shakeIntensity),
						-(shakeIntensity / 2f) + (float) (Math.random() * shakeIntensity));
			}
		}

		if (controlStyle == NO_CONTROL && focus != null) {
			if (lookAt == null) {
				final Vector3f lookPos = new Vector3f(focus.position);
				lookAt = Vector3f.sub(position, lookPos).normalize();
			}

			pitch = MathUtil.lerp(pitch, (float) Math.toDegrees(Math.asin(lookAt.y)), .05f);
			yaw = MathUtil.angleLerp(yaw, -(float) Math.toDegrees(Math.atan2(lookAt.x, lookAt.z)), .05f);
			angleAroundPlayer = -(yaw - 360);
		} else {
			handleControl();

			this.yaw = 360 - angleAroundPlayer;
			yaw += 360;
			yaw %= 360;
		}

		updateViewMatrix();

	}

	public void setControlStyle(byte style) {
		this.controlStyle = style;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public void setYaw(float f) {
		angleAroundPlayer = -f;
	}

	public void setZoom(float i) {
		targetZoom = i;
		zoomSpeed = (targetZoom - zoom) / 45;
		updateProjection();
	}

	public void shake(float time, float intensity) {
		this.shakeIntensity = intensity;
		this.shakeTime = time;
	}

	public void ungrabMouse() {
		Input.requestMouseRelease();
	}

	public void updateProjection() {
		this.projectionMatrix = createProjectionMatrix();
	}

	public void updateViewMatrix() {
		viewMatrix.identity();

		final Vector2f shake = getScreenShake();

		viewMatrix.rotateX(pitch + shake.y);
		viewMatrix.rotateY(yaw + shake.x);
		viewMatrix.rotateZ(roll);
		final Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
		viewMatrix.translate(negativeCameraPos);

		viewDirection.x = -viewMatrix.m02;
		viewDirection.y = -viewMatrix.m12;
		viewDirection.z = -viewMatrix.m22;

		Matrix4f.mul(projectionMatrix, viewMatrix, projectionViewMatrix);

		frustum.update(projectionViewMatrix);
	}

}
