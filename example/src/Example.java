import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import ch.qos.logback.classic.*;
//import ch.qos.logback.core.util.StatusPrinter;

import com.ingescape.*;

public class Example implements AgentEventListener, ServiceListener, IopListener, WebSocketEventListener {

	private static Logger _logger = LoggerFactory.getLogger(Example.class);

	public Example() {

		//LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		//StatusPrinter.print(lc);
	}

	@Override
	public void handleIOP(Agent agent, Iop iop, String name, IopType type, Object value) {
		_logger.debug("**received input {} with type {} and value {}", name, type, value);

		if (iop == Iop.IGS_INPUT_T && type == IopType.IGS_DATA_T) {
			byte[] data = (byte[])value;
			Path pathToFile = Paths.get("/Users/steph/Desktop/mydata.png");
			try {
				Files.write(pathToFile, data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handleCallToService(Agent agent, String senderAgentName, String senderAgentUUID,
									String serviceName, List<Object> arguments, String token) {
		_logger.debug("**received service call from {} ({}): {} (with token {})", senderAgentName, senderAgentUUID, serviceName, arguments, token);
	}

	@Override
	public void handleAgentEvent(Agent agent, AgentEvent event, String uuid, String name, Object eventData) {
		_logger.debug("**received agent event for {} ({}): {} with data {}", name, uuid, event, eventData);
	}

	@Override
	public void handleWebSocketEvent(WebSocketEvent event, Throwable t) {
		if (t != null) { // (event == WebSocketEvent.IGS_WEB_SOCKET_FAILED)
			_logger.error("**received web socket event {} with exception {}", event, t.toString());
		}
		else {
			_logger.debug("**received web socket event {}", event);
		}
	}


	public static void main(String[] args) throws InterruptedException {

        _logger.info("Start Java app 'IngeScape agent test'");
        _logger.info("is DEBUG enabled ? {}", _logger.isDebugEnabled());

		Global globalContext = new Global("ws://localhost:9009");
		//Global globalContext = new Global("ws://10.0.0.35:5003");
        //Global globalContext = new Global("ws://192.168.1.18:5000");

		Example test = new Example();
		globalContext.observeWebSocketEvents(test);

		Agent a = globalContext.agentCreate("javaTest0");
		a.observeAgentEvents(test);

		a.definition.setName("javaTest");
		a.definition.setDescription("java test agent");
		a.definition.setVersion("1.0");

		a.definition.inputCreate("myInt", IopType.IGS_INTEGER_T);
		a.definition.inputCreate("myDouble", IopType.IGS_DOUBLE_T);
		a.definition.inputCreate("myString", IopType.IGS_STRING_T);
		a.definition.inputCreate("myBool", IopType.IGS_BOOL_T);
		a.definition.inputCreate("myImpulsion", IopType.IGS_IMPULSION_T);
		a.definition.inputCreate("myData", IopType.IGS_DATA_T);
		a.definition.outputCreate("myInt", IopType.IGS_INTEGER_T);
		a.definition.outputCreate("myDouble", IopType.IGS_DOUBLE_T);
		a.definition.outputCreate("myString", IopType.IGS_STRING_T);
		a.definition.outputCreate("myBool", IopType.IGS_BOOL_T);
		a.definition.outputCreate("myImpulsion", IopType.IGS_IMPULSION_T);
		a.definition.outputCreate("myData", IopType.IGS_DATA_T);
		a.definition.parameterCreate("myInt", IopType.IGS_INTEGER_T);
		a.definition.parameterCreate("myDouble", IopType.IGS_DOUBLE_T);
		a.definition.parameterCreate("myString", IopType.IGS_STRING_T);
		a.definition.parameterCreate("myBool", IopType.IGS_BOOL_T);
		a.definition.parameterCreate("myImpulsion", IopType.IGS_IMPULSION_T);
		a.definition.parameterCreate("myData", IopType.IGS_DATA_T);
		a.observeInput("myInt", test);
		a.observeInput("myDouble", test);
		a.observeInput("myString", test);
		a.observeInput("myBool", test);
		a.observeInput("myImpulsion", test);
		a.observeInput("myData", test);

		a.serviceInit("javaCall", test);
		a.serviceArgAdd("javaCall", "myInt", IopType.IGS_INTEGER_T);
		a.serviceArgAdd("javaCall", "myDouble", IopType.IGS_DOUBLE_T);
		a.serviceArgAdd("javaCall", "myBool", IopType.IGS_BOOL_T);
		a.serviceArgAdd("javaCall", "myString", IopType.IGS_STRING_T);
		a.serviceArgAdd("javaCall", "myData", IopType.IGS_DATA_T);

		//a.mapping.add("myInt", "iosAgent", "myInt");
		a.mapping.add("myDouble", "macosAgent", "value1");
		//a.mapping.add("myString", "iosAgent", "myString");
		//a.mapping.add("myBool", "iosAgent", "myBool");
		//a.mapping.add("myImpulsion", "iosAgent", "myImpulsion");
		//a.mapping.add("myData", "iosAgent", "myData");

		a.start();

		//outputs
		Thread.sleep(3000);
		a.outputSetInt("myInt", 5);
		a.outputSetDouble("myDouble", 6.6);
		a.outputSetString("myString", "test string");
		a.outputSetBool("myBool", true);
		a.outputSetImpulsion("myImpulsion");
		byte[] myBytes = "java test for byte array".getBytes();
		a.outputSetData("myData", myBytes);

		//services
		Thread.sleep(3000);
		List<Object> arguments1 = Arrays.asList(9, 7.8, true, "java call string test", "java byte array".getBytes());
		a.serviceCall("macosAgent", "MAC_CALL", arguments1, "token");

		List<Object> arguments2 = Arrays.asList();
		a.serviceCall("macosAgent", "PONG_CALL", arguments2, "token");

		try {
			byte[] fileContent = Files.readAllBytes(Paths.get("/Users/steph/Desktop/Ingescape_API_map.pdf"));
			a.outputSetData("myData", fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Thread.sleep(20000);
		//a.stop();
		//a = null;
	}

}
