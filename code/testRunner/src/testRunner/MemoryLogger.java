package testRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemoryLogger extends Logger {
	String format = "%d%s";
	private long startTime;
	
	List<String> entries;
	public boolean disabled = false;
	private Boolean outputToConsole;
	public MemoryLogger(Boolean outputToConsole) {
		super("MemoryLogger",null);
		this.outputToConsole = outputToConsole;
		entries = new ArrayList<String>(100000);
		this.startTime = System.currentTimeMillis();
		this.setLevel(Level.ALL);
	}

	@Override
	public void severe(String msg) {
		this.log(Level.SEVERE, msg);
	}

	@Override
	public void info(String msg) {
		
		this.log(Level.INFO, msg);
	}
	
	@Override
	public void warning(String msg) {
		this.log(Level.WARNING, msg);
	}
	
	public void log(String msg) {
		log(Level.INFO, msg);
	}
	
	@Override
	public void log(Level level, String msg) {
		if(disabled) return;
		if(level.intValue() < super.getLevel().intValue()) return;
		if(msg.length() > 512){
			msg = msg.substring(0,511);
			entries.add("NEXT ENTRY TRUNCATED TO 512 CHARACTERS");
		}
		
		String dataToPost = String.format(format, System.currentTimeMillis() - this.startTime, msg);
		entries.add(dataToPost);
		if(outputToConsole)
			System.out.println(dataToPost);
		
	}
	
	public void dumpToFile(String path) throws FileNotFoundException{
//		PrintWriter out = new PrintWriter(new FileOutputStream(
//			    new File(path), 
//			    true /* append = true */));
		PrintWriter out = new PrintWriter(path);
		
		for(String entry : entries)
			out.println(entry);
		out.close();

	}

	public void clear() {
		entries.clear();
	}

	public void dumpToFileThreadSafe(String pathToStoreLog) throws FileNotFoundException, InterruptedException {
		disabled = true;
		Thread.sleep(1000);
		synchronized(entries){
			PrintWriter out = new PrintWriter(pathToStoreLog);
			
			for(String entry : entries)
				if(entry != null && !entry.equals("null"))
					out.println(entry);
			out.close();
		}
		disabled = false;
	}
}