package map.weather;

import java.util.Random;

import org.joml.Vector3f;

import audio.AudioHandler;
import audio.Source;
import core.Resources;
import gl.Camera;
import gl.Window;
import map.Enviroment;
import util.MathUtil;

public class Weather {
	private static final int CLEAR = 0, RAIN = 1, THUNDER = 2, TORNADO = 3;
	
	private Random random;
	private float actionTimer = 0f;
	
	private PrecipitationEmitter emitter;
	
	public static float weatherCell;
	private float prevWeather, targetWeather;
	private float thunderTimer = 0, nextThunder = 10;
	public boolean freeze = false;
	
	public static int weather = CLEAR;
	
	private Source ambientSource;
	private Source eventSource;
	
	public Weather(long seed, int cellArrSize) {
		random = new Random(seed);
		updateWeatherCells();
		weatherCell = 0f;
		prevWeather = 0f;
		targetWeather = 0f;
		doAction();
		emitter = new PrecipitationEmitter();
		
		ambientSource = new Source();
		eventSource = new Source();
		
		Resources.addSound("thunder1", "ambient/thunder1.ogg");
		Resources.addSound("thunder_distant1", "ambient/distant_thunder1.ogg");
		Resources.addSound("thunder_distant2", "ambient/distant_thunder2.ogg");
		Resources.addSound("rain", "ambient/rain_ambient1.ogg");
	}
	
	public void tick(Camera camera) {
		if (!freeze) {
			actionTimer += Enviroment.timeSpeed;
			if (weather == THUNDER) {
				
				if (thunderTimer+Window.deltaTime > nextThunder && thunderTimer <= nextThunder) {
					switch((int)(Math.random()*3)) {
					case 0:
						eventSource.play("thunder1");
						break;
					case 1:
						eventSource.play("thunder_distant1");
						break;
					default:
						eventSource.play("thunder_distant2");
					}
				
				}
				
				thunderTimer += Window.deltaTime;
				
				if (thunderTimer > nextThunder + .2f) {
					nextThunder = 10 + (random.nextInt()&0x0F);
					thunderTimer = 0;
				}
			}
		}
		
		if (weather == RAIN || weather == THUNDER || weather == TORNADO) {
			emitter.update(camera, weatherCell);
			if (!ambientSource.isPlaying()) {
				ambientSource.setLooping(true);
				ambientSource.play("rain");
			}
		} else {
			if (ambientSource.isPlaying()) {
				ambientSource.stop();
				ambientSource.setLooping(false);
			}
		}
		
		updateWeatherCells();
		
		weatherCell = MathUtil.lerp(prevWeather, targetWeather, actionTimer/50000f);
		
		if (actionTimer >= 1500000f) {
			doAction();
		}
	}

	private void updateWeatherCells() {
		weather = CLEAR;
		if (weatherCell < -.5) {
			weather = (weatherCell < -.75f) ? THUNDER : RAIN;
		}
	}

	private void doAction() {
		prevWeather = weatherCell;
		weatherCell = targetWeather;
		targetWeather = -1 + random.nextInt(2);
		actionTimer = 0f;
	}
	
	public float getWeatherCell() {
		return weatherCell;
	}

	public Vector3f determineSkyColor() {
		float color = 1f - (Math.abs(weatherCell)/2f);
		
		if (thunderTimer > nextThunder && Math.abs(random.nextInt()&0xFF) < 10) {
			color = 10;
		}
		
		return new Vector3f(color, color, color);
	}
	
	public void setWeather(float value) {
		prevWeather = value;
		weatherCell = value;
		targetWeather = value;
		actionTimer = 0f;
	}

	public float getLightingDim() {
		if (determineSkyColor().x == 10f) {
			return 2f;
		} else {
			return 1;
		}
	}
	
	public void cleanUp() {
		Resources.removeSound("thunder1");
		Resources.removeSound("thunder_distant1");
		Resources.removeSound("thunder_distant2");
		Resources.removeSound("rain");
		ambientSource.delete();
		eventSource.delete();
	}
}
