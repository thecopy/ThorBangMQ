package asl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class Message {
	public String content;
	public long receiverId;
	public long senderId;
	public long contextId;
	public long timestamp;
	public long queueId;
	public long id;
	public int priority;

	public Message(long receiverId, long senderId, long timestamp, long queueId, long id, int priority, long contextId, String content){
		this.content = content;
		this.receiverId = receiverId;
		this.senderId = senderId;
		this.contextId = contextId;
		this.timestamp = timestamp;
		this.queueId = queueId;
		this.priority = priority;
        this.id = id;
	}

	public Message(String str) {
		// Interpret string
	}
}
