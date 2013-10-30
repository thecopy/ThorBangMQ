package asl.infrastructure.exceptions;

public class InvalidMessageException extends Exception {
	public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
