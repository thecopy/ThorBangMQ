package asl.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpLogger extends Logger {
	private ExecutorService executor = Executors.newFixedThreadPool(1); 

	private String name;
	private int session_id;
	
	String jsonFormat = "{\"log_entry\":{\"name\":\"%s\",\"session_id\":%d,\"msg\":\"%s\",\"timestamp\":%d}}";

	private String urlPath;
	
	public HttpLogger(String name, String urlPath) {
		super(name, null);
		
		this.urlPath = urlPath;
		this.name = name;
		this.session_id = new Random().nextInt(Integer.MAX_VALUE);
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
	
	@Override
	public void log(Level level, String msg) {
		String dataToPost = String.format(jsonFormat, name, session_id, msg, new Date().getTime());
		executor.submit(new Request(urlPath, dataToPost));
	}
	
	public class Request implements Callable<InputStream> {
	    private String url;
		private String data;

	    public Request(String url, String data) {
	        this.url = url;
	        this.data = data;
	    }

	    @Override
	    public InputStream call() throws Exception {
	    	URLConnection connection = new URL(url).openConnection();
	    	connection.setDoOutput(true); // Triggers POST.
	    	connection.setRequestProperty("Content-Type", "application/json");
	    	OutputStream output = connection.getOutputStream();
	    	try {
	    	     output.write(data.getBytes(Charset.forName("ASCII")));
	    	} finally {
	    	     try { output.close(); } catch (IOException logOrIgnore) { logOrIgnore.printStackTrace();}
	    	}
	    	return connection.getInputStream();

	    }
	}

}