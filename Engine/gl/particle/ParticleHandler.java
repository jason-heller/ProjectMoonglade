package gl.particle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import core.Resources;
import gl.Camera;
import gl.res.Texture;
import map.Material;

public class ParticleHandler {
	private static Map<Texture, List<Particle>> particles = Collections
			.synchronizedMap(new LinkedHashMap<Texture, List<Particle>>());
	private static ParticleRenderer renderer;
	private static int particleCount = 0;
	public static int maxParticles = 99;

	public static void add(Particle p) {
		if (particleCount > maxParticles + 1) {
			// particles.remove(entry.getKey(),entry.getValue());
			// particleCount--;
			return;
		}
		List<Particle> list = particles.get(p.getTexture());

		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<Particle>());
			particles.put(p.getTexture(), list);
			// list.add(p);
		}
		/*
		 * else if (numAlive < list.size()) { for(Particle particle : list) { if
		 * (!particle.isAlive()) { particle.setActive(p.getPosition(), p.getVelocity(),
		 * p.getGravity(), p.getLife(), p.getRotation(), p.getScale()); } } } else {
		 */
		list.add(p);
		particleCount++;
		// }
	}

	public static void cleanUp() {
		renderer.cleanup();
		Resources.getTexture("particles").delete();
		Resources.getTexture("small_particles").delete();
	}

	public static void init() {
		renderer = new ParticleRenderer();
		Resources.addTexture("particles", "particles/particles.png", GL11.GL_TEXTURE_2D, true, 8);
		Resources.addTexture("small_particles", "particles/small_particles.png", GL11.GL_TEXTURE_2D, true, 32);
	}

	public static void render(Camera camera) {
		renderer.render(particles, camera);
	}

	private static void sortParticles(List<Particle> list) {
		for (int i = 1; i < list.size(); i++) {
			final Particle item = list.get(i);
			if (item.getDistance() > list.get(i - 1).getDistance()) {
				sortUpHighToLow(list, i);
			}
		}
	}

	private static void sortUpHighToLow(List<Particle> list, int i) {
		final Particle item = list.get(i);
		int attemptPos = i - 1;
		while (attemptPos != 0 && list.get(attemptPos - 1).getDistance() < item.getDistance()) {
			attemptPos--;
		}
		list.remove(i);
		list.add(attemptPos, item);
	}

	public static void update(Camera camera) {
		final Iterator<Entry<Texture, List<Particle>>> mapIterator = particles.entrySet().iterator();
		while (mapIterator.hasNext()) {
			final Entry<Texture, List<Particle>> entry = mapIterator.next();
			final List<Particle> list = entry.getValue();

			synchronized (list) {
				final Iterator<Particle> iter = list.iterator();
				while (iter.hasNext()) {
					final Particle p = iter.next();
					if (p.isAlive()) {
						final boolean alive = p.update(camera);
						if (!alive) {
							iter.remove();
							particleCount--;
						}
					}
				}
			}

			// if (!entry.getKey().isAdditive()) {
			sortParticles(list);
			// }
		}
	}

	public static void addBurst(String tex, int start, int end, Vector3f position) {
		Texture texture = Resources.getTexture(tex);
		for(float i = 0; i < .36f; i += .12f) {
			for(float j = 0; j < .36f; j += .12f) {
				for(float k = 0; k < .36f; k += .12f) {
					float dx = -(.17f - i) / 18f;
					float dy = .01f;
					float dz = -(.17f - k) / 18f;
					Particle p = new Particle(texture, new Vector3f(position.x+i-.12f, position.y+j+.25f, position.z+k-.12f), new Vector3f(dx,dy, dz), .0028f, 50, 1, 1, .15f);
					p.setTextureAtlasRange(start, end);

				}
			}
		}
	}
	
	public static void addSplash(Material matieral, Vector3f position, Vector3f direction) {
		Texture tex = Resources.getTexture("materials");
		Vector3f texData = new Vector3f();
		Material.getTexCoordData(texData, matieral, (byte)0);
		Vector3f dir;
		for (float i = 0; i < 6; i++) {
			dir = generateRandomUnitVectorWithinCone(direction, 5f);
			dir.mul(.008f + (float)(Math.random()*.03f));
			
			Particle p = new Particle(tex, new Vector3f(position), new Vector3f(dir), .0035f, 50, 1, 1, .15f);
			//p.setTextureUvs(texData.x, texData.y, texData.x + texData.z, texData.y + texData.z);
			int matPos = (int)((texData.x*32) + (texData.y*32*32));
			p.setTextureAtlasRange(matPos, matPos);
		}
	}
	
	private static Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
		final float cosAngle = (float) Math.cos(angle);
		final Random random = new Random();
		final float theta = (float) (random.nextFloat() * 2f * Math.PI);
		final float y = cosAngle + random.nextFloat() * (1 - cosAngle);
		final float rootOneMinusZSquared = (float) Math.sqrt(1 - y * y);
		final float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		final float z = -(float) (rootOneMinusZSquared * Math.sin(theta));

		Vector4f direction = new Vector4f(x, y, z, 1);
		if (coneDirection.x != 0 || coneDirection.z != 0 || coneDirection.y != 1 && coneDirection.y != -1) {
			final Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 1, 0));
			rotateAxis.normalize();
			final float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, new Vector3f(0, 1, 0)));
			final Matrix4f rotationMatrix = new Matrix4f();
			rotationMatrix.rotate((float) -Math.toDegrees(rotateAngle), rotateAxis);
			direction = Matrix4f.transform(rotationMatrix, direction);
		} else if (coneDirection.y == -1) {
			direction.y *= -1;
		}
		return new Vector3f(direction);
	}
	
	public static int size() {
		return particleCount;
	}
}
