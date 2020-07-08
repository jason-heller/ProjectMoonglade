package dev;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;

import core.Application;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleHandler;
import gl.res.Model;
import gl.res.Vbo;
import map.Chunk;
import map.Enviroment;
import map.tile.Tile;
import procedural.biome.Biome;
import procedural.biome.BiomeVoronoi;
import procedural.biome.BiomeVoronoi.BiomeNode;
import procedural.terrain.GenTerrain;
import scene.entity.EntityHandler;
import scene.overworld.Overworld;
import ui.UI;

public class Debug {
	public static boolean debugMode = true;
	public static boolean terrainWireframe = false;
	public static boolean flatTerrain = false;
	public static boolean structureMode = false;
	
	public static void checkVbo(Vbo vbo) {
		vbo.bind();
		int id = vbo.getId();
		int size = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
		Console.log("VBO { id=" + id, "size=" + size + "}");
		vbo.unbind();
	}
	
	public static void checkVao(Model model) {
		int id = model.id;
		int numVbos = model.getNumVbos();
		
		Console.log("");
		Console.log("VAO { id=" + id + "}");
		
		for(int i = 0; i < numVbos; i++) {
			checkVbo(model.getVbo(i));
		}
		
		Console.log("");
	}

	public static void uiDebugInfo(Overworld overworld) {
		Camera camera = overworld.getCamera();
		Enviroment env = overworld.getEnviroment();
		float weather = env.getWeather().getWeatherCell();
		
		int culled = 0;
		for (final Chunk[] chunkBatch : env.getTerrain().get()) {
			for (final Chunk chunk : chunkBatch) {
				if (chunk != null && chunk.isCulled()) {
					culled++;
				}
			}
		}
		
		String cx = String.format("%.1f", camera.getPosition().x);
		String cy = String.format("%.1f", camera.getPosition().y);
		String cz = String.format("%.1f", camera.getPosition().z);
		
		BiomeVoronoi biomeVoronoi = env.getBiomeVoronoi();
		BiomeNode biomeCellData = biomeVoronoi.getClosest();
		Biome biome = biomeCellData.biome;
		float[] cp = GenTerrain.getClimateProperties(camera.getPosition().x, camera.getPosition().z);

		UI.drawString("#rX: "+cx+" #gY: "+cy+" #bZ: "+cz+"\n"
				+ "#wFPS: "+(int)Window.framerate+"/"+Window.maxFramerate+"\n"
				+ "TPS: "+Application.TICKS_PER_SECOND + "\n"
				+ "chunk xz: "+Math.floor(camera.getPosition().x/Chunk.CHUNK_SIZE)+", "+Math.floor(camera.getPosition().z/Chunk.CHUNK_SIZE) + "\n"
				+ "Biome: " + biome.getName() + " \n"
				+ "weather: "+weather+"\n"
				+ "climate: "+GenTerrain.getTemperature(cp[0])+"/"+GenTerrain.getMoisture(cp[1])+"\n"
				+ "dt: "+Window.deltaTime +"\n"
				+ "time: " + (int)Enviroment.exactTime+"\n"
				+ "facing: " + overworld.getCamFacingByte()
				, 5, 5, .25f, false);
	}
}
