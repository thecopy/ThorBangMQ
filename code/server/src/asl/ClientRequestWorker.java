package asl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.infrastructure.IProtocolService;
import asl.network.ITransport;

public class ClientRequestWorker implements Runnable{

	private String requestString = null;
	private Logger logger;
	private ITransport transport;
	private IProtocolService ps;

	public ClientRequestWorker(Logger logger, IProtocolService protocolService, ITransport transport, String requestString) {
		this.requestString = requestString;
		this.logger = logger;
		this.transport = transport;
		this.ps = protocolService;

		logger.info(String.format("Processing Request: %s", requestString));
	}

	@Override
	public void run() {
		try {
			interpreter(requestString);
		} catch (IOException e) {
			logger.severe("Error while writing to client: " + e);
		}
	}

	// TODO handle exceptions
	private void interpreter(String msg) throws IOException{
		String[] methodWithArgs = msg.split(",", 2);

		switch(methodWithArgs[0]){
		case "HELLO":
			transport.Send("OK");
			break;
		case "MSG":
			this.ps.storeMessage(methodWithArgs[1]);
			break;
		case "PEEKQ":
			this.ps.sendMessage(this.ps.peekQueue(methodWithArgs[1]));
			break;
		case "PEEKS":
			this.ps.sendMessage(this.ps.peekQueueWithSender(methodWithArgs[1]));
			break;
		case "POPQ":
			this.ps.sendMessage(this.ps.popQueue(methodWithArgs[1]));
			break;
		case "POPS":
			this.ps.sendMessage(this.ps.popQueueWithSender(methodWithArgs[1]));
			break;
		case "CREATEQUEUE":
			transport.Send(String.valueOf(this.ps.createQueue(methodWithArgs[1])));
			break;
		case "REMOVEQUEUE":
			this.ps.removeQueue(Long.parseLong(methodWithArgs[1]));
			transport.Send("OK");
			break;
		case "CREATECLIENT":
			transport.Send(String.valueOf(this.ps.createClient(methodWithArgs[1])));
			break;
		default:
			logger.log(Level.WARNING, "Failed to interpert client message: " + msg);
			break;
		}
	}
}
