package com.ingescape;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

import okhttp3.*;
import okio.ByteString;

public class Global  extends WebSocketListener {
	private WebSocket webSocket;
    private MessageHandler messageHandler;
	private static Logger _logger = LoggerFactory.getLogger(Global.class);

    GeneralUpdateTask task;
    Timer generalTimer = new Timer();

    Gson gson = new Gson();

    Timer messagesTimer;

    String endpoint;
    boolean isTryingToConnect = false;
    boolean isConnected = false;
    private int _tryConnect = 0;
    private int _failConnect = 0;

    List<WebSocketEventListener> webSocketEventListeners = new ArrayList<>();
    Map<String, Agent> createdAgents = new HashMap<>();


    public Global(String endpoint) {
    	assert(endpoint != null);
    	this.endpoint = endpoint;

    	//start timer for connection monitoring and for definition and mapping updates
    	this.task = new GeneralUpdateTask(this);
    	this.generalTimer.schedule(task, 10, 1000);
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL

    class GeneralUpdateTask extends TimerTask {
    	Global globalContext;

    	public GeneralUpdateTask(Global i) {
			this.globalContext = i;
		}

    	@Override
    	public void run() {
    		Logger logger = LoggerFactory.getLogger(Global.class);
    		if (!isConnected)
    		{
				if (!isTryingToConnect) // Not already currently trying to connect
				{
					isTryingToConnect = true;
					_tryConnect++;
					logger.info("Try connect {}...", _tryConnect);

					OkHttpClient client = new OkHttpClient.Builder()
							.readTimeout(0,  TimeUnit.MILLISECONDS) // 0 --> No timeout
							.writeTimeout(0, TimeUnit.MILLISECONDS) // 0 --> No timeout
							.connectTimeout(3, TimeUnit.SECONDS)
							//.pingInterval(1, TimeUnit.SECONDS)
							.build();

					Request request = new Request.Builder()
							.url(endpoint)
							.build();

					client.newWebSocket(request, globalContext);

					// Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
					client.dispatcher().executorService().shutdown();

					for (WebSocketEventListener listener : webSocketEventListeners) {
						listener.handleWebSocketEvent(WebSocketEvent.IGS_WEB_SOCKET_TRY_CONNECT, null);
					}
				}
				else {
					logger.warn("There is currently a try to connect, wait it success or fails before try again... ({} tries and {} fails))", _tryConnect, _failConnect);
				}
    		}
    		else {
    			for (Agent agent : createdAgents.values()) {
    				if (agent.definition.hasBeenUpdated) {
        				Object definitionObject = agent.definition.toJson();
        				Map<String, Object> updateDefinition = new HashMap<>();
        				updateDefinition.put("event", "update_definition");
        				updateDefinition.put("uuid", agent.uuid);
        				Map<String, Object> def = new HashMap<>();
        		        def.put("definition", definitionObject);
        				updateDefinition.put("definition", def);

        		    	String message = gson.toJson(updateDefinition);
        		    	agent._sendMessage(message);
        				logger.info("definition: {}", def);
        				agent.definition.hasBeenUpdated = false;
        			}
        			if (agent.mapping.hasBeenUpdated) {
						ArrayList<Map<String, Object>> mappingArray = agent.mapping.toJson();
						Map<String, Object> mappingObject = new HashMap<>();
        				mappingObject.put("mappings", mappingArray);

        				Map<String, Object> updateMapping = new HashMap<>();
        				updateMapping.put("event", "update_mapping");
        				updateMapping.put("uuid", agent.uuid);
        				updateMapping.put("mapping", mappingObject);
        		    	String message = gson.toJson(updateMapping);
        				logger.info("mapping: {}", mappingArray);
        				agent._sendMessage(message);
        				agent.mapping.hasBeenUpdated = false;
        			}
    			}
    		}
    	}
    }

    void _deliverMessages () {
    	for (Agent agent : createdAgents.values()) {
    		synchronized (agent.messagesBuffer) {
        	    while (agent.messagesBuffer.size() > 0) {
        	        String msg = agent.messagesBuffer.get(0);
        	        _logger.debug("Sending: {}", msg);
        	        this.webSocket.send(msg);
        	        agent.messagesBuffer.remove(0);
        	    }
        	}
    	}
    }


	 /////////////////////////////////////////////////////////////////////////////////////
    // WS

    // Invoked when a web socket has been accepted by the remote peer and may begin transmitting messages.
    @Override public void onOpen(WebSocket webSocket, Response response) {
    	_logger.info("Web socket OPENED");
    	if (_failConnect > 0)
    		_logger.debug("after {} try and {} fails", _tryConnect, _failConnect);

    	this.isTryingToConnect = false;
    	this.isConnected = true;
    	this.webSocket = webSocket;
    	_tryConnect = 0;
    	_failConnect = 0;

    	for (WebSocketEventListener listener : webSocketEventListeners) {
    		listener.handleWebSocketEvent(WebSocketEvent.IGS_WEB_SOCKET_OPENED, null);
    	}

    	for (Agent agent : createdAgents.values()) {
    		agent.isConnected = true;

    		//send init message
        	Map<String, String> init = new HashMap<>();
        	init.put("event", "init_pseudo_agent");
        	init.put("uuid", agent.uuid);
        	init.put("name", agent.definition.name);
        	String message = gson.toJson(init);
        	webSocket.send(message);
        	_logger.info("init: {}", message);

        	//send start message if needed
        	if (agent.isStarted) {
        		agent.isStarted = false;
        		agent.start();
        	}
    	}

    	//start messages timer
    	this.messagesTimer = new Timer();
    	this.messagesTimer.schedule(new TimerTask() {
    		@Override
    		public void run() {
    			//_logger.trace("messages time !");
    			_deliverMessages();
    		}
    	}, 50, 50);

    }

    // Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
    	webSocket.close(1000, null); // 1000 indicates a normal closure, meaning that the purpose for which the connection was established has been fulfilled.

    	_logger.info("CLOSING web socket ({}): {}", code, reason);
    	this.webSocket = null;
    	this.isConnected = false;

    	for (WebSocketEventListener listener : webSocketEventListeners) {
    		listener.handleWebSocketEvent(WebSocketEvent.IGS_WEB_SOCKET_CLOSING, null);
    	}

    	this.messagesTimer.cancel();
    	this.messagesTimer = null;

    	for (Agent agent : createdAgents.values()) {
    		agent.isConnected = false;
    		agent.definition.hasBeenUpdated = true;
    		agent.mapping.hasBeenUpdated = true;
    	}
    }

    // Invoked when both peers have indicated that no more messages will be transmitted and the
    // connection has been successfully released. No further calls to this listener will be made.
    @Override public void onClosed(WebSocket webSocket, int code, String reason) {
    	_logger.info("CLOSED web socket ({}): {}", code, reason);

    	for (WebSocketEventListener listener : webSocketEventListeners) {
    		listener.handleWebSocketEvent(WebSocketEvent.IGS_WEB_SOCKET_CLOSED, null);
    	}
    }

    // Invoked when a web socket has been closed due to an error reading from or writing to the network.
    // Both outgoing and incoming messages may have been lost. No further calls to this listener will be made.
    @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    	if (this.isConnected)
    	{
    		//_logger.error("FAILURE: {}", t.toString(), t);
    		_logger.error("FAILURE web socket: {}", t.toString());

    		this.isConnected = false; // Reset to allow to open a new web socket
        	for (Agent agent : createdAgents.values()) {
        		agent.isConnected = false;
        		agent.definition.hasBeenUpdated = true;
        		agent.mapping.hasBeenUpdated = true;
        	}

	    	for (WebSocketEventListener listener : webSocketEventListeners) {
	    		listener.handleWebSocketEvent(WebSocketEvent.IGS_WEB_SOCKET_FAILED, t);
	    	}
    	}
    	else
    	{
    		this.isTryingToConnect = false; // Reset to allow a new try
    		_failConnect++;
    		//_logger.error("FAILURE ({}): {}", _failConnect, t.toString(), t);
    		_logger.error("FAILURE web socket ({}): {}", _failConnect, t.toString());
    	}

    	// if (t instanceof java.net.ConnectException) { // Default exception when try connection fails
    	// else if (t instanceof java.io.EOFException) { // Default exception when igsProxy stopped
    	// else if (t instanceof java.net.UnknownServiceException) { // CLEARTEXT communication to IP not permitted by network security policy
    	// else if (t instanceof java.net.SocketException) {
    	// 		- socket failed: EPERM (Operation not permitted)
    	// 		- Software caused connection abort
    	// else if (t instanceof java.net.SocketTimeoutException) { // connect timed out

    	t.printStackTrace();
    }

    // Invoked when a text (type `0x1`) message has been received.
    @Override public void onMessage(WebSocket webSocket, String text) {
    	//_logger.debug("MESSAGE: {}", text);
    	if (this.messageHandler != null) {
    		this.messageHandler.handleMessage(text);
    	}
    	else {
    		this._handleMessage(text);
    	}
    }

    // Invoked when a binary (type `0x2`) message has been received.
    @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
    	_logger.debug("MESSAGE (ByteString):  {}", bytes.hex());
    }

    void _handleMessage(String message) {
    	_logger.debug("Received: {}", message);
    	JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
    	JsonElement event = msg.get("event");
    	JsonElement uuid = msg.get("uuid");
    	if ((event != null) && event.isJsonPrimitive()
    			&& (uuid != null) && uuid.isJsonPrimitive())
    	{
    		Agent concernedAgent = this.createdAgents.get(uuid.getAsString());
    		if (concernedAgent == null) {
    	    	_logger.error("Unknown agent with uuid {}", uuid.getAsString());
    	    	return;
    		}

    		//_logger.debug("event: {}", event.toString());
    		if (event.getAsString().equals("agent_event_raised")) {
    			//TODO: add checks to JSON elements
    			JsonElement agentEvent = msg.get("agent_event");
    			JsonElement agentUUID = msg.get("agent_uuid");
    			JsonElement agentName = msg.get("agent_name");
    			JsonElement eventData = msg.get("event_data");
    			for (AgentEventListener listener : concernedAgent.agentEventListeners) {
    	            listener.handleAgentEvent(concernedAgent, AgentEvent.fromInt(agentEvent.getAsInt()), agentUUID.getAsString(),
    	            						  agentName.getAsString(), eventData.getAsString());
    	        }
    		}
    		else if (event.getAsString().equals("iop_written")) {
    			//TODO: add checks to JSON elements
    			JsonElement iopName = msg.get("name");
    			JsonElement typeS = msg.get("type");
    			Iop type = Iop.fromInt(typeS.getAsInt());
    			JsonElement valueTypeS = msg.get("value_type");
    			IopType valueType = IopType.fromInt(valueTypeS.getAsInt());
    			JsonElement value = msg.get("value");
    			DefinitionIop iop = null;
    			List<IopListener> listeners = null;
    			switch (type) {
				case IGS_INPUT_T:
					iop = concernedAgent.definition.inputs.get(iopName.getAsString());
					listeners = concernedAgent.inputListeners.get(iopName.getAsString());
					break;
				case IGS_OUTPUT_T:
					iop = concernedAgent.definition.outputs.get(iopName.getAsString());
					listeners = concernedAgent.outputListeners.get(iopName.getAsString());
					break;
				case IGS_PARAMETER_T:
					iop = concernedAgent.definition.parameters.get(iopName.getAsString());
					listeners = concernedAgent.parameterListeners.get(iopName.getAsString());
					break;

				default:
					break;
				}
    			if (iop != null) {
    				switch (valueType) {
					case IGS_INTEGER_T:
						iop.value = value.getAsInt();
						break;
					case IGS_DOUBLE_T:
						iop.value = value.getAsDouble();
						break;
					case IGS_BOOL_T:
						iop.value = value.getAsBoolean();
						break;
					case IGS_IMPULSION_T:
						iop.value = "";
						break;
					case IGS_STRING_T:
						iop.value = value.getAsString();
						break;
					case IGS_DATA_T:
						try {
				            iop.value = Base64.getDecoder().decode(value.getAsString().getBytes("UTF-8"));
				        } catch (UnsupportedEncodingException e) {
				            e.printStackTrace();
				        }
						break;

					default:
						break;
					}
    				_logger.info("Updated iop '{}' with value {}", iopName.getAsString(), iop.value.toString());
    				if (listeners != null) {
    					for (IopListener l : listeners) {
    						l.handleIOP(concernedAgent, type, iopName.getAsString(), valueType, iop.value);
    					}
    				}
    			}
    		}
    		else if (event.getAsString().equals("call_received")) {
    			String senderAgentName = msg.get("sender_name").getAsString();
    			String senderAgentUUID = msg.get("sender_uuid").getAsString();
    			String serviceName = msg.get("service_name").getAsString();
    			DefinitionService service = concernedAgent.definition.services.get(serviceName);
    			JsonArray arguments = msg.get("arguments").getAsJsonArray();
    			String token = msg.get("token").getAsString();
    			List<Object> argumentsList = new Vector<>();
    			for (JsonElement elmt : arguments) {
    				JsonObject obj = elmt.getAsJsonObject();
    				IopType argType = IopType.fromInt(obj.get("type").getAsInt());
    				JsonElement value = obj.get("value");
    				switch (argType) {
					case IGS_INTEGER_T:
						argumentsList.add(value.getAsInt());
						break;
					case IGS_DOUBLE_T:
						argumentsList.add(value.getAsDouble());
						break;
					case IGS_BOOL_T:
						argumentsList.add(value.getAsBoolean());
						break;
					case IGS_STRING_T:
						argumentsList.add(value.getAsString());
						break;
					case IGS_DATA_T:
						//TODO: decode data
						argumentsList.add(value.getAsString());
						break;

					default:
						break;
					}
    			}
    			_logger.debug("Received call to service '{}' from {}", serviceName, senderAgentName);
				if (service.listener != null) {
					service.listener.handleCallToService(concernedAgent, senderAgentName, senderAgentUUID, serviceName, argumentsList, token);
				}

    		}
    		else {
    			_logger.error("Unsupported event: {}", event.toString());
    		}
    	}
    	else {
    		//ignore or trigger error
    		//_logger.error();
    	}
    }

    //optional external message handler (overrides internal handling)
    public static interface MessageHandler {
        public void handleMessage(String message);
    }
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void observeWebSocketEvents(WebSocketEventListener listener) {
    	this.webSocketEventListeners.add(listener);
    }

    public Agent agentCreate(String name) {
		Agent newAgent = new Agent(name);
		this.createdAgents.put(newAgent.uuid, newAgent);
		return newAgent;
    }

    public void agentDestroy(Agent agent) {

    	if (this.isConnected)
    	{
    		Map<String, String> destroy = new HashMap<>();
    		destroy.put("event", "destroy_pseudo_agent");
    		destroy.put("uuid", agent.uuid);
    		destroy.put("name", agent.definition.name);
	    	String message = gson.toJson(destroy);
        	webSocket.send(message);
	    	_logger.info("destroy: {}", message);
    	}
    	this.createdAgents.remove(agent.uuid);
    	agent = null;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // TESTING
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		Logger logger = LoggerFactory.getLogger(Global.class);
		logger.info("Start javaTest !");

		Global globalContext = new Global("ws://localhost:9009");
		Agent a = globalContext.agentCreate("mySuperAgent");

		//definition
		a.definition.clear();
		a.definition.setName("javaTest");
		a.definition.setDescription("java test agent");
		a.definition.setVersion("0.0");
		a.definition.inputCreate("test", IopType.IGS_DATA_T);
		a.definition.inputRemove("test");
		a.definition.inputRemove("test");
		a.definition.clear();

		//mapping
		a.mapping.clear();
		a.mapping.add("myInt", "otherAgent", "otherOutput");
		a.mapping.add("myDouble", "otherAgent2", "otherOutput2");
		a.mapping.clear();
		a.mapping.add("myInt", "otherAgent", "otherOutput");
		a.mapping.add("myDouble", "otherAgent2", "otherOutput2");

		a.definition.setName("javaTest");
		a.definition.setDescription("java test agent");
		a.definition.setVersion("0.0");
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

		a.start();

		Thread.sleep(2000);
		//iops
		a.outputSetImpulsion("myInt");
		a.outputSetInt("myInt", 5);
		a.outputSetDouble("myInt", 6.6);
		a.outputSetString("myInt", "12.5");
		a.outputSetString("myInt", "15");
		a.outputSetImpulsion("myDouble");
		a.outputSetInt("myDouble", 5);
		a.outputSetDouble("myDouble", 6.6);
		a.outputSetString("myDouble", "12.5");
		a.outputSetString("myDouble", "15");
		a.outputSetImpulsion("myString");
		a.outputSetInt("myString", 5);
		a.outputSetDouble("myString", 6.6);
		a.outputSetString("myString", "test toto");
		a.outputSetString("myString", a.gson.toString());
		a.outputSetImpulsion("myBool");
		a.outputSetBool("myBool", false);
		a.outputSetBool("myBool", true);
		a.outputSetString("myBool", "false");
		a.outputSetString("myBool", "true");
		a.outputSetInt("myBool", 5);
		a.outputSetDouble("myBool", 6.6);
		a.outputSetString("myBool", "test toto");
		a.outputSetString("myBool", a.gson.toString());
		a.outputSetImpulsion("myImpulsion");
		a.outputSetBool("myImpulsion", true);
		a.outputSetInt("myImpulsion", 5);
		a.outputSetDouble("myImpulsion", 6.6);
		a.outputSetString("myImpulsion", "test toto");
		a.outputSetString("myImpulsion", a.gson.toString());
		a.outputSetImpulsion("myData");
		a.outputSetBool("myData", true);
		a.outputSetInt("myData", 5);
		a.outputSetDouble("myData", 6.6);
		a.outputSetString("myData", a.gson.toString());
		a.outputSetString("myData", "test toto");
		byte[] myBytes = "test for bytes array".getBytes();
		a.outputSetData("myData", myBytes);

//		Thread.sleep(2000);
//		a.stop();
//		a = null;
	}
}
