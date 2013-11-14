package asl.infrastructure.exceptions;

@SuppressWarnings("serial")
public class InvalidQueueException extends Exception {
	public long id;
	
	public InvalidQueueException(long id) {
        super();
        this.id = id;
    }

    public InvalidQueueException(long id, Throwable throwable) {
        super(throwable);
        this.id = id;
    }
}
