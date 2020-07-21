package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import dev.Console;
import map.Enviroment;
import map.weather.Weather;
import scene.overworld.Overworld;
import scene.overworld.inventory.Inventory;

public class SaveDataIO {
	public static void writeSaveData(Overworld overworld) {
		String filename = getFilename();
		
		try {
			File file = new File(filename);
			PrintWriter writer = new PrintWriter(file);
			Enviroment env = overworld.getEnviroment();
			Vector3f pos = overworld.getPlayer().position;

			writer.write("worldname=" + Overworld.worldName);
			writer.write("\nseed=" + Overworld.worldSeed);
			writer.write("\ntime=" + env.getTime());
			writer.write("\nweather=" + env.getWeather().getWeatherCell());
			writer.write("\nlocation=" + pos.x + "," + pos.y + "," + pos.z);
			writer.write("\nyaw=" + overworld.getCamera().getYaw());
			writer.write("\npitch=" + overworld.getCamera().getPitch());
			writer.write("\nhp=" + overworld.getPlayer().getHp());

			Inventory inv = overworld.getInventory();
			
			String itemData = "";
			String amtData = "";
			final int len = inv.getItems().length;
			for(int i = 0; i < len; i++) {
				itemData += inv.getItems()[i] + ",";
				amtData += inv.getQuantities()[i] + ",";
			}
			
			writer.write("\nitems=" + itemData);
			writer.write("\nquantities=" + amtData);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readSaveData(Overworld overworld) {
		String filename = getFilename();
		
		Map<String, String> map = new HashMap<String, String>();

	    String line;
	    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			
			while ((line = reader.readLine()) != null) {
		        String[] parts = line.split("=", 2);
		        
		        if (parts.length >= 2) {
		            String key = parts[0];
		            String value = parts[1];
		            map.put(key, value);
		        }
		    }
			
			reader.close();
			
			Overworld.worldName = map.get("worldname");
			Overworld.worldSeed = map.get("seed");
			int time = Integer.parseInt(map.get("time"));
			Enviroment.exactTime = time;
			Weather.weatherCell = (Float.parseFloat(map.get("weather")));

			String[] pos = map.get("location").split(",");
			Vector3f camPos = new Vector3f(Float.parseFloat(pos[0]), Float.parseFloat(pos[1]),
					Float.parseFloat(pos[2]));
			overworld.getCamera().setPosition(camPos);
			overworld.getCamera().setYaw(Float.parseFloat(map.get("yaw")));
			overworld.getCamera().setPitch(Float.parseFloat(map.get("pitch")));
			//overworld.getPlayer().setHp(Integer.parseInt(map.get("hp")));
			
			Inventory inv = overworld.getInventory();
			
			String[] itemList = map.get("items").split(",");
			String[] qtyList = map.get("quantities").split(",");
			
			final int len = itemList.length-1;
			for(int i = 0; i < len; i++) {
				int item = Integer.parseInt(itemList[i]);
				int qty = Integer.parseInt(qtyList[i]);
				inv.addItem(item, qty);
			}
			
		} catch (FileNotFoundException e) {
			Console.log("Tried to load save.data, no such file exists.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    
	    
	}

	private static String getFilename() {
		return "saves/" + Overworld.worldFileName + "/save.dat";
	}
}
