package asl.infrastructure.exceptions;

public class InvalidQueueException extends Exception {
	public InvalidQueueException(String message) {
        super(message);
    }

    public InvalidQueueException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
