package asl.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.ServerSettings;

public class Bootstrapper2 {
	private static String configurationFile = "conf.txt";

	public static ServerSettings StrapTheBoot(Logger logger) throws Exception{
		ServerSettings serverSettings = new ServerSettings();
		if (!new File(configurationFile).exists()){
			logger.log(Level.CONFIG, "Configuration file not found. Defaulting to in memory persistence.");
			serverSettings.PERSISTENCE_TYPE = PersistenceType.MEMORY;
			SaveTheStrapping(logger, serverSettings);
		} else
		try {
			// Read configuration file
			
			String[] settings = readFile(configurationFile).split("\n");
			Class<ServerSettings> serverSettingClass = ServerSettings.class;
			for (String line : settings) {
				String[] setting = line.split("\t");
				Field field = serverSettingClass.getDeclaredField(setting[0]);
				Class<?> type = field.getType();
				
				if(type.equals(Integer.TYPE))
					field.setInt(serverSettings, Integer.parseInt(setting[1]));
				else if(type.equals(Boolean.TYPE))
					field.setBoolean(serverSettings, Boolean.parseBoolean(setting[1]));
				else if(type.equals(String.class))
					field.set(serverSettings, setting[1]);
				else if(type.equals(PersistenceType.class))
					field.set(serverSettings, PersistenceType.valueOf(setting[1]));
				else
					logger.severe("Error while checking type of field " 
									+ field.getName() + ". Not int, boolean or String");
					
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not read from configuration." + e);
		}

		return serverSettings;
	}

	public static void SaveTheStrapping(Logger logger, ServerSettings settings) throws IllegalArgumentException, IllegalAccessException{
		StringBuilder sb = new StringBuilder();

		for(Field field : ServerSettings.class.getDeclaredFields()){
			sb.append(field.getName() + "\t" + field.get(settings) + "\n");
		}

		try {
			saveFile(configurationFile, sb.toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not save configuration file: " + e);
		}
	}

	private static String readFile(String path) throws IOException{
	    BufferedReader br = new BufferedReader(new FileReader(path));
	    try {
	        String line = br.readLine();
	        StringBuilder sb = new StringBuilder();

	        while (line != null) {
	            sb.append(line);
	            sb.append('\n');
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

	private static void saveFile(String path, String content) throws IOException{
		PrintWriter out = new PrintWriter(path);
		out.print(content);
		out.close();
	}

}
