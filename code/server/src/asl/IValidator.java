package asl;

public interface IValidator {
	Boolean validateMessage(long reciever, long sender, long queue, String context, String content);
}
