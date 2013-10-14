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
	public int priority;
	
	public Message(long receiverId, long senderId, long timestamp, long queueId, long contextId, int priority, String content){
		this.content = content;
		this.receiverId = receiverId;
		this.senderId = senderId;
		this.contextId = contextId;
		this.timestamp = timestamp;
		this.queueId = queueId;
		this.priority = priority;
	}
	
	public Message(long queueId, long contextId, int priority, String content){
		this.content = content;
		this.contextId = contextId;
		this.queueId = queueId;
		this.priority = priority;
	}
	
	public Message(long queueId, int priority, String content){
		this.content = content;
		this.queueId = queueId;
		this.priority = priority;
	}
	
	public Message(String str) {
		// Interpret string
	}
	
	public ByteBuffer toByteBuffer() {
		// use data from object to generate a bytebuffer
		return ASLServerSettings.CHARSET.encode("dummy string");
	}
	
}
