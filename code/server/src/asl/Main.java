package asl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.infrastructure.Bootstrapping;

public class Main {
	static Logger logger = Logger.getLogger("ThorBangMQ");
	
	public static void main(String[] args) throws Exception {
		logger.setLevel(Level.ALL); // performance, yay
		
		// Read configuration file
		ServerSettings settings = Bootstrapping.StrapTheBoot(logger);
		settings = parseArgs(args, settings);
		logger.info(String.format("Using in memory persister: %s", settings.UseInMemoryPersister));
		
		try {
			System.out.println("Starting ThorBang MQ Server");

			ThorBangMQServer socketServer = ThorBangMQServer.build(settings, logger);
			socketServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static ServerSettings parseArgs(String[] args, ServerSettings settings) {
		if (args.length < 3) {
			logger.severe("Arguments are: <DB_IP> <DB_MAX_CONNECTIONS> <NUM_WORKERTHREADS>");
			System.exit(-1);
		}
		settings.DB_SERVER_NAME = args[0];
		settings.UseInMemoryPersister = false;
		settings.DB_MAX_CONNECTIONS = Integer.parseInt(args[1]);
		settings.NUM_CLIENTREQUESTWORKER_THREADS = Integer.parseInt(args[2]);
		if (args.length >= 3 && args[3].equals("ALL")) {
			logger.setLevel(Level.ALL);
		} else {
			logger.setLevel(Level.OFF);
		}
		return settings;
	}

}
