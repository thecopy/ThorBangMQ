package asl.infrastructure.exceptions;

public class InvalidClientException extends Exception {
	public InvalidClientException(String message) {
        super(message);
    }

    public InvalidClientException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
