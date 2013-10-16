package asl;

import java.io.IOException;
import java.util.logging.Logger;
import asl.infrastructure.Bootstrapping;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("ThorBangMQ");

		// Read configuration file
		ASLServerSettings settings = Bootstrapping.StrapTheBoot(logger);

		try {
			System.out.println("Starting ThorBang MQ Server");

			ASLSocketServer socketServer = ASLSocketServer.build(settings, logger);
			socketServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
