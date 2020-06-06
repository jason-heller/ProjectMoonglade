package gl.fbo;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import core.Globals;
import gl.Window;

public class FboUtils {

	public static int createDepthBufferAttachment(int width, int height, boolean multisampled) {
		final int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		if (multisampled) {
			GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, Globals.fboSamplingAmt, GL11.GL_DEPTH_COMPONENT,
					width, height);
		} else {
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
		}
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER,
				depthBuffer);
		return depthBuffer;
	}

	public static int createDepthTextureAttachment(int width, int height) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, width, height, 0, GL11.GL_DEPTH_COMPONENT,
				GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		return texture;
	}

	public static FrameBuffer createDepthTextureFbo(int width, int height) {
		return new FrameBuffer(width, height, false, true, true, false, 1);
	}

	public static int createMultisampleColorAttachment(int width, int height, int attachment) {
		final int colorBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, colorBuffer);
		GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, Globals.fboSamplingAmt, GL11.GL_RGBA8, width,
				height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_RENDERBUFFER, colorBuffer);
		return colorBuffer;
	}

	public static int createTextureAttachment(int width, int height) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
				(ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
		return texture;
	}

	public static FrameBuffer createTextureFbo(int width, int height) {
		return new FrameBuffer(width, height, true, true, false, false, 1);
	}

	public static FrameBuffer createTextureFbo(int width, int height, boolean multisampled, int multiRenderTargets) {
		return new FrameBuffer(width, height, true, true, false, multisampled, multiRenderTargets);
	}

	/**
	 * Resolves a multisampled FBO to the screen
	 * 
	 * @param input The multisampled FBO to resolve
	 */
	public static void resolve(FrameBuffer input) {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		GL11.glDrawBuffer(GL11.GL_BACK);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, input.getId());

		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		GL30.glBlitFramebuffer(0, 0, input.getWidth(), input.getHeight(), 0, 0, Window.getWidth(), Window.getHeight(),
				GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	/**
	 * Resolves a multisampled FBO into a standard FBO
	 * 
	 * @param readBuffer the buffer to read from (ie GL30.GL_COLOR_ATTACHMENT0)
	 * @param input      The multisampled FBO to resolve
	 * @param output     The non-multisampled FBO to resolve to
	 */
	public static void resolve(int readBuffer, FrameBuffer input, FrameBuffer output) {

		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, output.getId());
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, input.getId());

		GL11.glReadBuffer(readBuffer);
		GL30.glBlitFramebuffer(0, 0, input.getWidth(), input.getHeight(), 0, 0, output.getWidth(), output.getHeight(),
				GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

}
