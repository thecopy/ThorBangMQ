package asl.infrastructure.exceptions;

@SuppressWarnings("serial")
public class InvalidClientException extends Exception {
	public long id;
	
	public InvalidClientException(long id) {
        super();
        this.id = id;
    }

    public InvalidClientException(long id, Throwable throwable) {
        super(throwable);
        this.id = id;
    }
}
