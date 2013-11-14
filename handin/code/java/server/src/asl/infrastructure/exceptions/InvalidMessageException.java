package asl.infrastructure.exceptions;

@SuppressWarnings("serial")
public class InvalidMessageException extends Exception {
	public long id;
	
	public InvalidMessageException(long id) {
        super();
        this.id = id;
    }

    public InvalidMessageException(long id, Throwable throwable) {
        super(throwable);
        this.id = id;
    }
}
