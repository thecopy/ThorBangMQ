package asl.infrastructure.exceptions;

public class PersistenceException extends Exception {
	public PersistenceException(String message) {
        super(message);
    }
	
	public PersistenceException() {
		super();
	}
	
    public PersistenceException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public PersistenceException(Throwable throwable) {
    	super(throwable);
    }
}
