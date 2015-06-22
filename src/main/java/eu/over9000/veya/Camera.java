package eu.over9000.veya;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class Camera {
	public static final double PITCH_LIMIT = 90 * Math.PI / 180;
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();

	private final Vector3f position;
	private float yaw = 0;
	private float pitch = 0;

	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private final int viewMatrixLocation;
	private final int projectionMatrixLocation;
	private final int cameraPositionLocation;
	public static final float YAW_LIMIT = 2f * (float) Math.PI;

	public Camera(final Program shader, final float posX, final float posY, final float posZ) {
		this.viewMatrixLocation = shader.getUniformLocation("viewMatrix");
		this.projectionMatrixLocation = shader.getUniformLocation("projectionMatrix");
		this.cameraPositionLocation = shader.getUniformLocation("cameraPosition");
		this.position = new Vector3f(posX, posY, posZ);
	}

	public void updateCameraPosition() {
		GL20.glUniform3f(this.cameraPositionLocation, this.position.x, this.position.y, this.position.z);
	}

	public void updateViewMatrix() {
		this.viewMatrix = new Matrix4f();

		this.viewMatrix.rotate(this.pitch, new Vector3f(1, 0, 0), this.viewMatrix);
		this.viewMatrix.rotate(this.yaw, new Vector3f(0, 1, 0), this.viewMatrix);
		this.viewMatrix.translate(this.position.negate(null), this.viewMatrix);

		this.viewMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.viewMatrixLocation, false, this.matrixBuffer);

	}

	public void updateProjectionMatrix(final float fieldOfView, final int width, final int height, final float nearPlane, final float farPlane) {
		this.projectionMatrix = new Matrix4f();
		final float aspectRatio = (float) width / (float) height;

		final float y_scale = (float) (1.0f / Math.tan(Math.toRadians(fieldOfView / 2.0f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = farPlane - nearPlane;
		this.projectionMatrix.m00 = x_scale;
		this.projectionMatrix.m11 = y_scale;
		this.projectionMatrix.m22 = -((farPlane + nearPlane) / frustum_length);
		this.projectionMatrix.m23 = -1;
		this.projectionMatrix.m32 = -(2 * nearPlane * farPlane / frustum_length);
		this.projectionMatrix.m33 = 0;

		this.projectionMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.projectionMatrixLocation, false, this.matrixBuffer);

		// System.out.println("updated projection matrix:");
		// System.out.println(this.projectionMatrix);

	}

	// moves the camera forward relative to its current rotation (yaw)
	public void walkForward(final float distance) {
		this.position.x += distance * (float) Math.sin(this.yaw);
		this.position.z -= distance * (float) Math.cos(this.yaw);
	}

	// moves the camera backward relative to its current rotation (yaw)
	public void walkBackwards(final float distance) {
		this.position.x -= distance * (float) Math.sin(this.yaw);
		this.position.z += distance * (float) Math.cos(this.yaw);
	}

	// strafes the camera left relitive to its current rotation (yaw)
	public void strafeLeft(final float distance) {
		this.position.x += distance * (float) Math.sin(this.yaw - Math.toRadians(90));
		this.position.z -= distance * (float) Math.cos(this.yaw - Math.toRadians(90));
	}

	// strafes the camera right relitive to its current rotation (yaw)
	public void strafeRight(final float distance) {
		this.position.x += distance * (float) Math.sin(this.yaw + Math.toRadians(90));
		this.position.z -= distance * (float) Math.cos(this.yaw + Math.toRadians(90));
	}

	// increment the camera's current yaw rotation
	public void yaw(final float amount) {
		this.yaw = (yaw + amount) % YAW_LIMIT;

	}

	// increment the camera's current yaw rotation
	public void pitch(final float amount) {
		// increment the pitch by the amount param
		this.pitch -= amount;
		if (this.pitch > PITCH_LIMIT) {
			this.pitch = (float) (90 * Math.PI / 180);
		}
		if (this.pitch < -PITCH_LIMIT) {
			this.pitch = (float) (-90 * Math.PI / 180);
		}

	}

	// private void setViewMatrix(final float eyeX, final float eyeY, final float eyeZ, final float dx, final float dy) {
	// final Vector3f up = new Vector3f(0, 1, 0).normalise(null);
	//
	// final Vector3f eye = new Vector3f(eyeX, eyeY, eyeZ);
	//
	// this.lookDir = Camera.rotY(this.lookDir, (float) (dy * Math.PI / 180));
	//
	// // final Vector3f front = Vector3f.sub(this.lookAt, eye, null).normalise(null); // front
	// final Vector3f front = this.lookDir.normalise(null); // front
	// final Vector3f side = Vector3f.cross(front, up, null).normalise(null); // side
	// final Vector3f upCam = Vector3f.cross(side, front, null).normalise(null); // up in cam
	//
	// this.viewMatrix = new Matrix4f();
	// this.viewMatrix.m00 = side.x;
	// this.viewMatrix.m01 = upCam.x;
	// this.viewMatrix.m02 = -front.x;
	// this.viewMatrix.m03 = 0;
	// this.viewMatrix.m10 = side.y;
	// this.viewMatrix.m11 = upCam.y;
	// this.viewMatrix.m12 = -front.y;
	// this.viewMatrix.m13 = 0;
	// this.viewMatrix.m20 = side.z;
	// this.viewMatrix.m21 = upCam.z;
	// this.viewMatrix.m22 = -front.z;
	// this.viewMatrix.m23 = 0;
	// this.viewMatrix.m30 = Vector3f.dot(side, eye);
	// this.viewMatrix.m31 = Vector3f.dot(upCam, eye);
	// this.viewMatrix.m32 = Vector3f.dot(front, eye);
	// this.viewMatrix.m33 = 1;
	//
	// // System.out.println("updated view matrix:");
	// // System.out.println(this.viewMatrix);
	// }

	public void moveUp(final float distance) {
		this.position.y += distance;
	}

	public void moveDown(final float distance) {
		this.position.y -= distance;
	}

	public Vector3f getPosition() {
		return new Vector3f(this.position.x, this.position.y, this.position.z);
	}

	public Vector3f getViewDirection() {
		float x = (float) (Math.cos(pitch) * Math.sin(yaw));
		float y = (float) -(Math.sin(pitch));
		float z = (float) -(Math.cos(pitch) * Math.cos(yaw));
		return new Vector3f(x, y, z);
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void printViewMatrix() {
		System.out.println(viewMatrix.m00 + " " + viewMatrix.m01 + " " + viewMatrix.m02 + viewMatrix.m03);
		System.out.println(viewMatrix.m10 + " " + viewMatrix.m11 + " " + viewMatrix.m12 + viewMatrix.m13);
		System.out.println(viewMatrix.m20 + " " + viewMatrix.m21 + " " + viewMatrix.m22 + viewMatrix.m23);
		System.out.println(viewMatrix.m30 + " " + viewMatrix.m31 + " " + viewMatrix.m32 + viewMatrix.m33);
	}
}
