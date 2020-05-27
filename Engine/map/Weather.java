package map;

import java.util.Random;

import org.joml.Vector3f;

import procedural.SimplexNoise;

public class Weather {
	SimplexNoise noise;
	private Random random;
	private float actionTimer = 0f;
	
	private float x, y;
	private float weatherDirection;
	private float speed = .00001f;
	
	private float[][] weatherCells;
	
	public Weather(long seed, int cellArrSize) {
		noise = new SimplexNoise((int) seed);
		random = new Random(seed+11);
		weatherCells = new float[cellArrSize][cellArrSize];
		updateWeatherCells();
	}
	
	public void update() {
		actionTimer += Enviroment.timeSpeed;
		
		if (actionTimer >= Enviroment.DAY_LENGTH/2) {
			actionTimer = 0;
			doAction();
		}
		
		if (actionTimer >= 1000) {
			updateWeatherCells();
		}
		
		x += Math.sin(weatherDirection) * (speed*Enviroment.timeSpeed);
		y += Math.cos(weatherDirection) * (speed*Enviroment.timeSpeed);
	}

	private void updateWeatherCells() {
		for(int i = 0; i < weatherCells.length; i++) {
			for(int j = 0; j < weatherCells[0].length; j++) {
				weatherCells[i][j] = noise.noise(x+i, y+j);
			}
		}
	}

	private void doAction() {
		weatherDirection += random.nextFloat(); // Roughly equal to PI/3
	}
	
	public float[][] getWeatherCells() {
		return weatherCells;
	}

	public Vector3f determineSkyColor() {
		int pos = weatherCells.length/2;
		float currentWeather = weatherCells[pos][pos];
		float scale = 1;
		
		if (currentWeather < -.6f) {
			scale = (currentWeather < -.8f) ? .4f : .6f;
		}
		
		return new Vector3f(scale, scale, scale);
	}
}
