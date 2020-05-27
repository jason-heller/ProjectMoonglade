package audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EFX10;

import core.Globals;
import core.Resources;

public class Source {
	private final int sourceId;
	private float vol = 1.0f;
	private int bufferOffset = 0;

	public Source() {
		sourceId = AL10.alGenSources();
		// defaultAttenuation();
		AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, 0f);

		// Attenuation
		AudioHandler.addSource(this);
		update();
	}

	public void applyEffect(SoundEffects effect) {
		AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, AudioHandler.getEffects().get(effect).getSlot(), 0,
				EFX10.AL_FILTER_NULL);
		AL10.alSourcei(sourceId, AL11.AL_SAMPLE_OFFSET, bufferOffset);
	}

	public void applyFilter(SoundFilters filter) {
		AL10.alSourcei(sourceId, EFX10.AL_DIRECT_FILTER, AudioHandler.getFilters().get(filter));
	}

	public void defaultAttenuation() {
		setAttenuation(15f, 64f);
	}

	public void delete() {
		stop();
		AL10.alSourceStop(sourceId);
		AudioHandler.removeSource(this);
		AL10.alDeleteSources(sourceId);
	}

	public String getSound() {
		return Resources.getSound(AL10.alGetSourcei(sourceId, AL10.AL_BUFFER));
	}

	public boolean isPlaying() {
		return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}

	public void pause() {
		bufferOffset = AL10.alGetSourcei(sourceId, AL11.AL_SAMPLE_OFFSET);
		// soundId = AL10.alGetSourcei(sourceId, AL10.AL_BUFFER);
		AL10.alSourcePause(sourceId);
	}

	public void play(int buffer) {
		stop();
		AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
		AL10.alSourcePlay(sourceId);
	}

	public void play(String sound) {
		stop();
		AL10.alSourcei(sourceId, AL10.AL_BUFFER, Resources.getSound(sound));
		AL10.alSourcePlay(sourceId);
	}

	public void removeEffect() {
		AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, EFX10.AL_EFFECTSLOT_NULL, 0, EFX10.AL_FILTER_NULL);
	}

	public void removeFilter() {
		AL10.alSourcei(sourceId, EFX10.AL_DIRECT_FILTER, EFX10.AL_FILTER_NULL);
	}

	public void setAttenuation(float rolloffFactor, float referenceDistance) {
		AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, rolloffFactor);
		AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, referenceDistance);
		// AL10.alSourcef(sourceId, AL10.AL_MAX_DISTANCE, maxDistance);
	}

	public void setGain(float vol) {
		this.vol = vol;
		AL10.alSourcef(sourceId, AL10.AL_GAIN, Globals.volume * vol);
	}

	public void setLooping(boolean loop) {
		AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
	}

	public void setPitch(float pitch) {
		AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
	}

	public void setPosition(Vector3f pos) {
		AL10.alSource3f(sourceId, AL10.AL_POSITION, pos.x, pos.y, pos.z);
	}

	public void setVelocity(Vector3f vel) {
		AL10.alSource3f(sourceId, AL10.AL_VELOCITY, vel.x, vel.y, vel.z);
	}

	public void stop() {
		AL10.alSourcePause(sourceId);
		bufferOffset = AL10.alGetSourcei(sourceId, AL11.AL_SAMPLE_OFFSET);
		// soundId = AL10.alGetSourcei(sourceId, AL10.AL_BUFFER);
		AL10.alSourceStop(sourceId);
		removeEffect();
	}

	public void unpause() {
		if (this.isPlaying()) {
			AL10.alSourcePlay(sourceId);
		}
	}

	public void update() {
		AL10.alSourcef(sourceId, AL10.AL_GAIN, Globals.volume * vol);
	}
}
