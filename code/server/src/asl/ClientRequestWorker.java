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
	private void interpreter(String msg) throws IOException {
		String[] methodWithArgs = msg.split(",", 2);

		switch(methodWithArgs[0]){
			case "HELLO":
				transport.Send("OK");
				break;
			case "MSG":
				transport.Send(this.ps.storeMessage(methodWithArgs[1]));
				break;
			case "PEEKQ":
				transport.Send(this.ps.peekQueue(methodWithArgs[1]));
				break;
			case "PEEKS":
				transport.Send(this.ps.peekQueueWithSender(methodWithArgs[1]));
				break;
			case "POPQ":
				transport.Send(this.ps.popQueue(methodWithArgs[1]));
				break;
			case "POPS":
				transport.Send(this.ps.popQueueWithSender(methodWithArgs[1]));
				break;
			case "CREATEQUEUE":
				transport.Send(this.ps.createQueue(methodWithArgs[1]));
				break;
			case "REMOVEQUEUE":
				transport.Send(this.ps.removeQueue(Long.parseLong(methodWithArgs[1])));
				break;
			case "CREATECLIENT":
				transport.Send(this.ps.createClient(methodWithArgs[1]));
				break;
			default:
				logger.log(Level.INFO, "Failed to interpert client message: " + msg);
				break;
		}
	}
}
