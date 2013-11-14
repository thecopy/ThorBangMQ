package server.tests;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FakeLogger extends Logger {

	protected FakeLogger() {
		super("fakelogger", null);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void log(Level level, String msg) {
		
	}
	
	@Override
	public void info(String msg) {
	}
	
	@Override
	public void warning(String msg) {
	}
	@Override
	public void fine(String msg) {
	}

}
