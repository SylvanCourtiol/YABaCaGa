package server;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;
import callbacks.*;

public class Server implements AgentEventListener, WebSocketEventListener, ServiceListener {

	private static Logger _logger = LoggerFactory.getLogger(Server.class);

	public Server() {}
	
	@Override
	public void handleCallToService(Agent agent, String senderAgentName, String senderAgentUUID, String serviceName, List<Object> arguments, String token) {
		_logger.debug("**received service call from {} ({}): {} (with token {})", senderAgentName, senderAgentUUID, serviceName, arguments, token);
	
	}

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


    	Server Server = new Server();
        globalContext.observeWebSocketEvents(Server);

        Agent agent = globalContext.agentCreate("YABaCaGaServer");
		agent.observeAgentEvents(Server);

		agent.definition.setClass("Server");

		agent.serviceInit("enterPlayer", Server);
		agent.serviceArgAdd("enterPlayer", "player", IopType.IGS_DATA_T);
		
		agent.serviceInit("receiveBet", Server);
		agent.serviceArgAdd("receiveBet", "playerId", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "cardId", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "runes", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "rage", IopType.IGS_BOOL_T);
		
		
        agent.definition.outputCreate("title", IopType.IGS_STRING_T);
        agent.definition.outputCreate("chatMessage", IopType.IGS_STRING_T);
        agent.definition.outputCreate("clear", IopType.IGS_IMPULSION_T);
        agent.definition.outputCreate("ui_command", IopType.IGS_STRING_T);

		agent.start();
		Thread.sleep(2000);
		
		agent.outputSetString("title", "YABaCaGa !");
		agent.outputSetString("chatMessage", "Server connected.");
		
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
