package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;
import testRunner.MemoryLogger;

public class BigMessage extends testRunner.Test {
	
	@Override
	public String[] getArgsDescriptors() {
		return new String[0];
	}

	@Override
	public void init(String[] args) throws Exception {
		

	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) {
		ThorBangMQ client;
		try {
			client = ThorBangMQ.build(host, port, 1);
			client.createClient("bigmessagetest_client");
			long queue =  client.createQueue("bigtest_queue");
			
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 1000; i++){
				sb.append("some_really_long_message_content");
			}
			sb.append("<END>");
			
			client.SendMessage(1, queue, 1, 1, sb.toString());
		} catch (IOException | InvalidQueueException | InvalidClientException | ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public String getInfo() {
		return "Sends a big message to the server";
	}

	@Override
	public String getIdentifier() {
		return "bigMessage";
	}
}
