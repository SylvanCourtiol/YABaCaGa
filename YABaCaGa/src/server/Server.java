package server;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;
import callbacks.*;

public class Server implements AgentEventListener, WebSocketEventListener {

	private static Logger _logger = LoggerFactory.getLogger(Server.class);

	public Server() {}

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

        _logger.info("Start Java app 'Server'");
        _logger.info("is DEBUG enabled ? {}", _logger.isDebugEnabled());

    	Global globalContext = new Global("ws://127.0.0.1:8080");

        InputCallback inputCB = new InputCallback();

    	Server Server = new Server();
        globalContext.observeWebSocketEvents(Server);

        Agent agent = globalContext.agentCreate("YABaCaGaServer");
		agent.observeAgentEvents(Server);

		agent.definition.setClass("Server");

        agent.definition.inputCreate("receiveFromPlayer", IopType.IGS_DATA_T);
        agent.observeInput("receiveFromPlayer", inputCB);
        agent.definition.inputCreate("image", IopType.IGS_DATA_T);
        agent.observeInput("image", inputCB);
        agent.definition.outputCreate("sendToPlayer", IopType.IGS_DATA_T);
        agent.definition.outputCreate("prompt", IopType.IGS_STRING_T);
        agent.definition.outputCreate("title", IopType.IGS_STRING_T);
        agent.definition.outputCreate("backgroundColor", IopType.IGS_STRING_T);
        agent.definition.outputCreate("chatMessage", IopType.IGS_STRING_T);
        agent.definition.outputCreate("clear", IopType.IGS_IMPULSION_T);
        agent.definition.outputCreate("ui_command", IopType.IGS_STRING_T);
        agent.definition.outputCreate("labelsVisible", IopType.IGS_BOOL_T);

		agent.start();
		Thread.sleep(1000);
		
		agent.outputSetString("chatMessage", "Coucou");
		
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
