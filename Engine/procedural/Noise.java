package procedural;

public abstract class Noise {
	
	public abstract float noise(float x, float y);
	
	public float fbm(float x, float y, int octaves, float roughness, float scale) {
		float noiseSum = 0;
		float layerFrequency = scale;
		float layerWeight = 1;
		float weightSum = 0;

		for (int octave = 0; octave < octaves; octave++) {
			noiseSum += noise(x * layerFrequency, y * layerFrequency) * layerWeight;
			layerFrequency *= 2;
			weightSum += layerWeight;
			layerWeight *= roughness;
		}
		return noiseSum / weightSum;
	}
	
	public float fbmt(float x, float y, int octaves, float roughness, float scale) {
		float noiseSum = 0;
		float layerFrequency = scale;
		float layerWeight = 1;
		float weightSum = 0;

		for (int octave = 0; octave < octaves; octave++) {
			noiseSum += noise(x * layerFrequency, y * layerFrequency) * layerWeight;
			layerFrequency *= 2;
			weightSum += layerWeight;
			layerWeight *= roughness;
		}
		return noiseSum / weightSum;
	}
	
	/*public float turbulence(float x, float y, float size) {
		float value = 0f;
		float startingSize = size;
		float drift = NoiseUtil.valueNoise2d((long)x, (long)y);
		
		while(size >= 1f) {
			value += noise((x+drift) / size, (y+drift) / size) * size;
			size /= 2.0f;
			drift = NoiseUtil.valueNoise2d((long)x, (long)y, (long)value);
		}
		
		return value / startingSize;
	}*/
}