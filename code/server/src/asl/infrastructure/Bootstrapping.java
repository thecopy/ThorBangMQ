package asl.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.ASLServerSettings;
import asl.Persistence.IPersistence;
import asl.Persistence.InMemoryPersistence;

public class Bootstrapping {
	private static String useMemoryPersistenceSetting = "useinmemorypersistence";
	private static String configurationFile = "conf.txt";
		
	public static ASLServerSettings StrapTheBoot(Logger logger){
		ASLServerSettings serverSettings = new ASLServerSettings();
		if (!new File(configurationFile).exists()){
			logger.log(Level.CONFIG, "Configuration file not found. Defaulting to in memory persistence.");
			serverSettings.UseInMemoryPersister = true;
			SaveTheStrapping(logger, serverSettings);
		} else
		try {
			// Read configuration file
			String[] settings = readFile(configurationFile).split("\n");

			for (String line : settings) {
				if(line.toLowerCase().startsWith(useMemoryPersistenceSetting)){
					if(line.split("\t")[1].equals("1"))
						serverSettings.UseInMemoryPersister = true;
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not read from configuration." + e);
		}
		
		return serverSettings;
	}
	
	public static void SaveTheStrapping(Logger logger, ASLServerSettings settings){
		StringBuilder sb = new StringBuilder();
		
		sb.append(useMemoryPersistenceSetting + "\t" + (settings.UseInMemoryPersister ? "1" : "0"));
		sb.append("\n");
		
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
