package asl.infrastructure;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemoryLogger extends Logger {
	String format = "%d\t%s";
	
	List<String> entries;

	private Boolean outputToConsole;
		
	public MemoryLogger(Boolean outputToConsole) {
		super("MemoryLogger",null);
		this.outputToConsole = outputToConsole;
		entries = new ArrayList<String>(1024);
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
		if(level.intValue() < super.getLevel().intValue()) return;
		
		String dataToPost = String.format(format, System.currentTimeMillis(), msg);
		entries.add(dataToPost);
		if(outputToConsole)
			System.out.println(dataToPost);
	}
	
	public void dumpToFile(String path) throws FileNotFoundException{
		PrintWriter out = new PrintWriter(path);
		for(String entry : entries)
			out.println(entry);
		out.close();

	}
}
