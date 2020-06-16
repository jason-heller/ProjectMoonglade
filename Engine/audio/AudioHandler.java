package audio;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.joml.Vector3f;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCcontext;
import org.lwjgl.openal.ALCdevice;
import org.lwjgl.openal.EFX10;
import org.lwjgl.util.WaveData;
import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;

import core.Resources;
import gl.Camera;
import gl.Window;
import io.FileUtils;

public class AudioHandler {

	public static Queue<Source> sources = new LinkedList<Source>();	// All active sources
	private static Map<SoundEffects, SoundEffect> effects = new HashMap<SoundEffects, SoundEffect>();
	private static Map<SoundFilters, Integer> filters = new HashMap<SoundFilters, Integer>();
	
	private static Queue<Source> sourceDeletionQueue = new LinkedList<Source>();

	private static float pauseDelay = 0f;
	public static float volume = 0.5f;

	public static void addSource(Source source) {
		sources.add(source);
	}

	public static void changeMasterVolume() {
		for (final Source source : sources) {
			source.update();
		}
	}

	public static void cleanUp() {

		while (sources.size() > 0) {
			sources.remove().delete();
		}

		for (final SoundEffect sfx : getEffects().values()) {
			EFX10.alDeleteEffects(sfx.getId());
			EFX10.alDeleteAuxiliaryEffectSlots(sfx.getSlot());
		}

		for (final int filter : getFilters().values()) {
			EFX10.alDeleteFilters(filter);
		}
		
		Resources.removeAllSounds();

		AL.destroy();
	}

	public static Map<SoundEffects, SoundEffect> getEffects() {
		return effects;
	}

	public static Map<SoundFilters, Integer> getFilters() {
		return filters;
	}
	
	public static void init() {
		try {
			AL.create();
			AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
			setupEFX();
			setupEffects();
			setupFilters();
			Thread.sleep(50);
		} catch (final LWJGLException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static int loadOgg(String path) {
		try {
			final int buffer = AL10.alGenBuffers();
			final InputStream in = FileUtils.getInputStream(path);
			final OggDecoder decoder = new OggDecoder();
			final OggData ogg = decoder.getData(in);

			AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ogg.data, ogg.rate);

			return buffer;
		} catch (final Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static int loadWav(String path) {
		final int buffer = AL10.alGenBuffers();
		final WaveData waveFile = WaveData.create(path);
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		return buffer;
	}

	public static void pause() {
		for (final Source s : sources) {
			s.applyEffect(SoundEffects.ECHO);
			pauseDelay = 0.1f;
		}
	}

	public static void removeSource(Source source) {
		sources.remove(source);
	}

	public static void setListenerData(Vector3f pos) {
		AL10.alListener3f(AL10.AL_POSITION, pos.x, pos.y, pos.z);
		// AL10.alListener3f(AL10.AL_VELOCITY, vel.x, vel.y, vel.z);
	}

	private static void setupEffects() {
		int effect, slot;

		// Echo
		effect = EFX10.alGenEffects();
		slot = EFX10.alGenAuxiliaryEffectSlots();
		getEffects().put(SoundEffects.ECHO, new SoundEffect(effect, slot));

		EFX10.alEffecti(effect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_ECHO);
		// EFX10.alEffectf(effect, EFX10.AL_ECHO_DELAY, 5.0f);
		EFX10.alAuxiliaryEffectSloti(slot, EFX10.AL_EFFECTSLOT_EFFECT, effect);
	}

	private static void setupEFX() throws Exception {
		final ALCdevice device = AL.getDevice();
		// String defaultDeviceName = ALC10.alcGetString(device,
		// ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

		final ALCcontext newContext = ALC10.alcCreateContext(device, (IntBuffer) null);
		if (newContext == null) {
			throw new Exception("Failed to create context");
		}
		final int contextCurResult = ALC10.alcMakeContextCurrent(newContext);
		if (contextCurResult == ALC10.ALC_FALSE) {
			throw new Exception("Failed to make context");
		}
	}

	private static void setupFilters() throws Exception {
		int filter;

		// Low Pass Freq
		filter = EFX10.alGenFilters();
		getFilters().put(SoundFilters.LOW_PASS_FREQ, filter);

		EFX10.alFilteri(filter, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAIN, 0.5f);
		EFX10.alFilterf(filter, EFX10.AL_LOWPASS_GAINHF, 0.5f);
	}

	public static void underwater(boolean submerged) {
		if (submerged) {
			for (final Source s : sources) {
				s.applyFilter(SoundFilters.LOW_PASS_FREQ);
			}
		} else {
			for (final Source s : sources) {
				s.removeFilter();
			}
		}

	}

	public static void unpause() {
		for (final Source s : sources) {
			// s.removeEffect();
			s.unpause();
		}
	}

	public static void update(Camera camera) {
		if (pauseDelay != 0f) {
			pauseDelay = Math.max(pauseDelay - Window.deltaTime, 0f);

			if (pauseDelay == 0f) {
				for (final Source s : sources) {
					s.removeEffect();
					s.pause();
				}
			}
		}
		
		Iterator<Source> iter = sourceDeletionQueue.iterator();
		while(iter.hasNext()) {
			Source source = iter.next();
			if (!source.isPlaying()) {
				source.delete();
				iter.remove();
			}
		}

		final Vector3f p = camera.getPosition();
		AL10.alListener3f(AL10.AL_POSITION, p.x, p.y, p.z);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
	}

	public static void deleteSource(Source source) {
		sourceDeletionQueue.add(source);
	}

}
