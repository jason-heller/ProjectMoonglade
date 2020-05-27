package dev;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;

import core.Application;
import core.Window;
import core.res.Model;
import core.res.Vbo;
import gl.Camera;
import map.Biome;
import map.Chunk;
import map.Enviroment;
import procedural.BiomeVoronoi;
import scene.overworld.Overworld;
import ui.UI;

public class Debug {
	
	public static boolean terrainWireframe = false;
	
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
		int wpos = env.getWeather().getWeatherCells().length/2;
		float weather = env.getWeather().getWeatherCells()[wpos][wpos];
		
		String cx = String.format("%.1f", camera.getPosition().x);
		String cy = String.format("%.1f", camera.getPosition().y);
		String cz = String.format("%.1f", camera.getPosition().z);
		
		BiomeVoronoi biomeVoronoi = env.getBiomeVoronoi();
		float[] biomeCellData = biomeVoronoi.getClosest();
		Biome biome = Biome.values()[(int)biomeCellData[2]];
		
		float[] biomeCellData2 = biomeVoronoi.getSecondClosest();
		Biome biome2 = Biome.values()[(int)biomeCellData2[2]];
		
		float biomeTransition = biomeVoronoi.getTransition();
		
		String interp = String.format("%.2f", biomeTransition);
		
		UI.drawString("#rX: "+cx+" #gY: "+cy+" #bZ: "+cz+"\n"
				+ "#wFPS: "+(int)Window.framerate+"/"+Window.maxFramerate+"\n"
				+ "TPS: "+Application.TICKS_PER_SECOND + "\n"
				+ "chunk xz: "+Math.floor(camera.getPosition().x/Chunk.CHUNK_SIZE)+", "+Math.floor(camera.getPosition().z/Chunk.CHUNK_SIZE) + "\n"
				+ "Biome: " + biome + " \n"
				+ "2nd Closest Biome: " + biome2 + " \n"
				+ "interp: "+interp+"\n"
				+ "weather: "+weather
				, 5, 5, .25f, false);
		
		float[][] w = overworld.getEnviroment().getWeather().getWeatherCells();
		for(int i = 0; i < w.length; i++) {
			for(int j = 0; j < w.length; j++) {
				Vector3f color = new Vector3f(w[i][j],w[i][j],w[i][j]);
				UI.drawRect(400+(i*6), 50+(j*6), 6, 6, color);
			}
		}
	}
}
