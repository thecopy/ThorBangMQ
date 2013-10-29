package asl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.Persistence.DbPersistence;
import asl.infrastructure.Bootstrapper2;
import asl.infrastructure.HttpLogger;
import asl.infrastructure.MemoryLogger;
import asl.infrastructure.PersistenceType;

public class Main {
	static final Logger logger = new MemoryLogger(false /*output to console*/);
	
	public static void main(String[] args) throws Exception {		
		logger.setLevel(Level.SEVERE);
		
		// Read configuration file
		ServerSettings settings = Bootstrapper2.StrapTheBoot(logger);
		settings = parseArgs(args, settings);
		
		try {
			System.out.println("Starting ThorBang MQ Server");

			final ThorBangMQServer socketServer = ThorBangMQServer.build(settings, logger);
			final IntervalLogger intervalLogger = new IntervalLogger(10000, logger, Level.SEVERE);
			
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					socketServer.start();
					
				}
			});
			Thread intervalLoggerThread = new Thread(intervalLogger);
			
			t.start();
			intervalLoggerThread.start();
			
			System.out.println("ThorBangMQ Server Started.");
			System.out.println("Commands:");
			System.out.println("  d <file>\tDump logfile into specified file");
			System.out.println("  q\t\tQuit");
			System.out.println("  qd <file>\tQuit and dump logfile into specified file");
			
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        String input = null;
	        System.out.print(": ");
	        
			while((input = br.readLine()) != null){
				if(input.startsWith("d ")){
					dumpLog(input.substring(2));
				}
				else if(input.startsWith("qd")){
					dumpLog(input.substring(3));
					stopServer(socketServer, intervalLogger, t, intervalLoggerThread);
					System.out.println("Bye :)");
					break;
				}
				else if(input.equals("q")){
					stopServer(socketServer, intervalLogger, t, intervalLoggerThread);
					System.out.println("Bye :)");
					break;
				}
				else{
					System.out.println("Unkown command '" + input + "'");
				}
		        System.out.print(": ");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static ServerSettings parseArgs(String[] args, ServerSettings settings) {
		if (args.length < 0) {
			System.out.println("Optional argument: cleardb=true");
			return settings;
		}

		if(args[1].equals("cleardb=true")){
			DbPersistence dbPersistence = new DbPersistence(new PoolingDataSource(), null);
			dbPersistence.deleteSchema();
			dbPersistence.createSchema();
			dbPersistence.buildSchema();
		}
		else{
			System.out.println("Unkown argument " + args[1]);
		}
		
		return settings;
	}
	
	private static void stopServer(ThorBangMQServer server, IntervalLogger logger, Thread serverThread, Thread intervalLoggerThread) throws InterruptedException{
		System.out.println("Shutting interval logger...");
		logger.stop();
		
		System.out.println("Shutting down server...");
		server.stop();
		
		System.out.println("Waiting for server to stop...");
		serverThread.join();	
		intervalLoggerThread.join();
	}
	
	private static void dumpLog(String path) throws FileNotFoundException{
		if(!(logger instanceof MemoryLogger)){
			System.out.println("Current logger (" + logger.getClass() + ") does not support dumping");
			return;
		}
		((MemoryLogger)logger).dumpToFile(path);
		System.out.println("Log file dumped to " + System.getProperty("user.dir") + "/" + path);
	}

}
