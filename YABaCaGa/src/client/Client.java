package client;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;
import callbacks.*;

public class Client implements AgentEventListener, WebSocketEventListener {

	private static Logger _logger = LoggerFactory.getLogger(Client.class);

	public Client() {}

	@Override
	public void handleAgentEvent(Agent agent, AgentEvent event, String uuid, String name, Object eventData) {
		_logger.debug("**received agent event for {} ({}): {} with data {}", name, uuid, event, eventData);
	}

	@Override
	public void handleWebSocketEvent(WebSocketEvent event, Throwable t) {
		if (t != null) {
			_logger.error("**received web socket event {} with exception {}", event, t.toString());
		}
		else {
			_logger.debug("**received web socket event {}", event);
		}
	}

	public static void main(String[] args) throws InterruptedException {

        _logger.info("Start Java app 'Client'");
        _logger.info("is DEBUG enabled ? {}", _logger.isDebugEnabled());

    	Global globalContext = new Global("ws://localhost:12345");

        InputCallback inputCB = new InputCallback();

    	Client Client = new Client();
        globalContext.observeWebSocketEvents(Client);

        Agent agent = globalContext.agentCreate("Client");
		agent.observeAgentEvents(Client);

		agent.definition.setClass("Client");

        agent.definition.inputCreate("receiveFromServer", IopType.IGS_DATA_T);
        agent.observeInput("receiveFromServer", inputCB);
        agent.definition.outputCreate("sendToServer", IopType.IGS_DATA_T);

		agent.start();

		System.out.println("Press Enter to stop the agent");
		Scanner scanner = new Scanner(System.in);
        try {
            scanner.nextLine();
        } catch(IllegalStateException | NoSuchElementException e) {
            // System.in has been closed
            System.out.println("System.in was closed; exiting");
        }

        agent.stop();
    }
}
