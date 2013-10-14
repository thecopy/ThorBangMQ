package asl.Persistence;

import java.util.ArrayList;
import java.util.Enumeration;

import asl.Client;
import asl.Message;

public class InMemoryPersistence implements IPersistence {
	private long id = 0;
	private int queues = 0;
	private ArrayList<Message> messages;
	
	public InMemoryPersistence(){
		messages = new ArrayList<Message>();
	}
	
	public InMemoryPersistence(ArrayList<Message> messages){
		this.messages = messages;
	}
	
	@Override
	public void deleteMessage(long messageId) {
		for(int i = 0; i < messages.size(); i++)
			if(messages.get(i).id == messageId)
			{ 
				messages.remove(i);
				return;
			}
		
	}

	@Override
	public void storeMessage(Message message) {
		message.id = ++id;
		messages.add(message);
	}

	@Override
	public Message getMessageByPriority(long queueId, long recieverId) {
		Message message = null;
		
		for(int i = 0; i < messages.size(); i++){
			Message m = messages.get(i);
			if(m.receiverId == recieverId && m.queueId == queueId)
			{
				if(message == null){	
					message = m;
				}else if((message.priority < m.priority)
						|| (message.priority == m.priority && message.timestamp > m.timestamp)){
					message = m;
				}
			}
		}
		
		return message;
	}

	@Override
	public Message getMessageByTimestamp(long queueId, long recieverId) {
		Message message = null;
		
		for(int i = 0; i < messages.size(); i++){
			Message m = messages.get(i);
			if(m.receiverId == recieverId && m.queueId == queueId)
			{
				if(message == null){	
					message = m;
				}else if(message.timestamp > m.timestamp){
					message = m;
				}
			}
		}
		
		return message;
	}

	@Override
	public Message getMessageBySender(long queueId, long recieverId, long senderId) {
		Message message = null;
		
		for(int i = 0; i < messages.size(); i++){
			Message m = messages.get(i);
			if(m.receiverId == recieverId && m.queueId == queueId && m.senderId == senderId)
			{
				if(message == null){	
					message = m;
				}else if(message.timestamp > m.timestamp){
					message = m;
				}
			}
		}
		
		return message;
	}

	@Override
	public long createQueue(String name) {
		return ++queues;
	}

	@Override
	public void removeQueue(long queueId) {
		
	}

	@Override
	public Enumeration<Client> getAllClients() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageById(long id) {
		for(int i = 0; i < messages.size(); i++){
			Message m = messages.get(i);
			if(m.id == id)
				return m;
		}
		
		return null;
	}

}
