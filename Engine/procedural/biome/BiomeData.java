package procedural.biome;

import org.joml.Vector3f;

public class BiomeData {
	private Biome[] influencingBiomes;
	private float[] influence;
	public int mainBiomeId;
	private int subseed;
	
	public BiomeData(Biome[] biomeIds, float[] influence, int mainBiomeId, int subseed) {
		this.influence = influence;
		this.influencingBiomes = new Biome[biomeIds.length];
		for(int i = 0; i < influencingBiomes.length; i++) {
			this.influencingBiomes[i] = biomeIds[i];
		}
		this.mainBiomeId = mainBiomeId;
		this.subseed = subseed;
	}
	
	public Biome[] getInfluencingBiomes() {
		return influencingBiomes;
	}
	
	public float[] getInfluence() {
		return influence;
	}

	public Vector3f getColor() {
		Vector3f outColor = new Vector3f();
		for(int i = 0; i < influencingBiomes.length; i++) {
			if (influencingBiomes[i] == null) continue;
			Vector3f color = influencingBiomes[i].getGroundColor();
			
			outColor.add(Vector3f.mul(color, influence[i]));
		}
		
		return outColor;
	}

	public float getRoughness() {
		float outRoughness = 0;
		for(int i = 0; i < influencingBiomes.length; i++) {
			if (influencingBiomes[i] == null) continue;
			float roughness = influencingBiomes[i].terrainRoughness;
			outRoughness += (roughness * influence[i]);
		}
		
		return outRoughness;
	}
	
	public float getTerrainFactor() {
		float outTerrainFactor = 0;
		for(int i = 0; i < influencingBiomes.length; i++) {
			if (influencingBiomes[i] == null) continue;
			float terrainFactor = influencingBiomes[i].terrainHeightFactor;
			outTerrainFactor += (terrainFactor * influence[i]);
		}
		
		return outTerrainFactor;
	}
	
	public int getSubseed() {
		return subseed;
	}

	public Biome getMainBiome() {
		return influencingBiomes[mainBiomeId];
	}
}