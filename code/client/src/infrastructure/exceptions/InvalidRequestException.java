package infrastructure.exceptions;

@SuppressWarnings("serial")
public class InvalidRequestException extends Exception {
	
	public InvalidRequestException() {
        super();
    }

    public InvalidRequestException(Throwable throwable) {
        super(throwable);
    }
}
