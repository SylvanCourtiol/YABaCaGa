package com.ingescape;

import java.util.*;
import java.lang.reflect.Method;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Agent {
	private static final Logger _logger = LoggerFactory.getLogger(Agent.class);
    String uuid;
    public Definition definition = new Definition();
    public Mapping mapping = new Mapping();
    List<AgentEventListener> agentEventListeners = new ArrayList<>();
    Map<String, List<IopListener>> inputListeners = new HashMap<>();
    Map<String, List<IopListener>> outputListeners = new HashMap<>();
    Map<String, List<IopListener>> parameterListeners = new HashMap<>();
    boolean isStarted = false;

    boolean isConnected = false;
    List<String> messagesBuffer = new Vector<>();
    Gson gson = new Gson();

    public Agent(String name) {
    	assert(name != null);
       	this.uuid = UUID.randomUUID().toString();
    	this.definition.name = name;
    	this.definition.hasBeenUpdated = true;
    	this.mapping.hasBeenUpdated = true;

    	if (this.isConnected)
    	{
    		Map<String, String> init = new HashMap<>();
        	init.put("event", "init_pseudo_agent");
        	init.put("uuid", this.uuid);
        	init.put("name", this.definition.name);
        	String message = gson.toJson(init);
        	_sendMessage(message);
        	_logger.info("init: {}", message);
    	}
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL


    void _sendMessage(String message) {
    	this.messagesBuffer.add(message);
    }

    void _writeIop(DefinitionIop iop) {
    	Map<String, Object> iopWrite = new HashMap<>();
    	iopWrite.put("event", "write_iop");
    	iopWrite.put("uuid", this.uuid);
    	Map<String, Object> iopInstance = new HashMap<>();
    	iopWrite.put("iop", iopInstance);
    	iopInstance.put("type", Iop.toInt(iop.type));
    	iopInstance.put("name", iop.name);
    	iopInstance.put("value_type", IopType.toInt(iop.valueType));
    	if (iop.value == null) {
    		if (iop.valueType == IopType.IGS_IMPULSION_T) {
    			_logger.debug("assessing impulsion");
        		iopInstance.put("value", "");
    		}
    		else {
    			_logger.error("rejecting null value for {}", iop.name);
    			return;
    		}
    	}
    	else {
    		Method convert = null;
        	switch (iop.valueType) {
        	case IGS_INTEGER_T:
        		_logger.debug("assessing {} as int", iop.value.getClass());
        		if (iop.value.getClass() == Integer.class) {
        			iopInstance.put("value", iop.value);
        			break;
        		}
        		else if (iop.value.getClass() == String.class) {
        			int v;
        			try { v = Integer.parseInt((String)iop.value); }
        			catch (NumberFormatException e) {
        				_logger.error("string {} cannot be parsed as int", iop.value);
        				return;
        			}
        			iopInstance.put("value", v);
        			break;
        		}
        		try {
        			convert = iop.value.getClass().getMethod("intValue");
        		} catch (Exception e) {}
        		if (convert != null) {
        			try {
        				iopInstance.put("value", convert.invoke(iop.value));
        			} catch (Exception e) {}
        		}
        		else {
        			_logger.error("could not convert {} value to int" + iop.name);
        			return;
        		}
        		break;

        	case IGS_DOUBLE_T:
        		_logger.debug("assessing {} as double", iop.value.getClass());
        		if (iop.value.getClass() == Double.class) {
        			iopInstance.put("value", iop.value);
        			break;
        		}
        		else if (iop.value.getClass() == String.class) {
        			double v;
        			try { v = Double.parseDouble((String)iop.value); }
        			catch (NumberFormatException e) {
        				_logger.error("string {} cannot be parsed as double", iop.value);
        				return;
        			}
        			iopInstance.put("value", v);
        			break;
        		}
        		try {
        			convert = iop.value.getClass().getMethod("doubleValue");
        		} catch (Exception e) {}
        		if (convert != null) {
        			try {
        				iopInstance.put("value", convert.invoke(iop.value));
        			} catch (Exception e) {}
        		}
        		else {
        			_logger.error("could not convert {} value to double", iop.name);
        			return;
        		}
        		break;

        	case IGS_STRING_T:
        		_logger.debug("assessing {} as string", iop.value.getClass());
        		if (iop.value.getClass() == String.class) {
        			iopInstance.put("value", iop.value);
        			break;
        		}
        		try {
        			convert = iop.value.getClass().getMethod("toString");
        		} catch (Exception e) {}
        		if (convert != null) {
        			try {
        				iopInstance.put("value", convert.invoke(iop.value));
        			} catch (Exception e) {}
        		}
        		else {
        			_logger.error("could not convert {} value to string", iop.name);
        			return;
        		}
        		break;

        	case IGS_BOOL_T:
        		_logger.debug("assessing {} as boolean", iop.value.getClass());
        		if (iop.value.getClass() == Boolean.class) {
        			iopInstance.put("value", iop.value);
        			break;
        		}
        		else if (iop.value.getClass() == String.class) {
        			boolean v;
        			try { v = Boolean.parseBoolean((String)iop.value); }
        			catch (NumberFormatException e) {
        				_logger.error("string {} cannot be parsed as boolean", iop.value);
        				return;
        			}
        			iopInstance.put("value", v);
        			break;
        		}
        		try {
        			convert = iop.value.getClass().getMethod("booleanValue");
        		} catch (Exception e) {}
        		if (convert != null) {
        			try {
        				iopInstance.put("value", convert.invoke(iop.value));
        			} catch (Exception e) {}
        		}
        		else {
        			_logger.error("could not convert {} value to boolean", iop.name);
        			return;
        		}
        		break;

        	case IGS_IMPULSION_T:
        		_logger.debug("assessing impulsion");
        		iopInstance.put("value", "");
        		break;

        	case IGS_DATA_T:
        		_logger.debug("assessing {} as data (class [B, i.e. byte[])", iop.value.getClass());
        		if (iop.value.getClass().toString().equals("class [B")) {
        			iopInstance.put("value", Base64.getMimeEncoder().encodeToString((byte[])iop.value));
        			break;
        		}
        		try {
        			convert = iop.value.getClass().getMethod("getBytes");
        		} catch (Exception e) {}
        		if (convert != null) {
        			try {
        				iopInstance.put("value", Base64.getMimeEncoder().encodeToString((byte[])convert.invoke(iop.value)));
        			} catch (Exception e) {}
        		}
        		else {
        			_logger.error("could not convert {} value to bytes", iop.name);
        			return;
        		}
        		break;

        	default:
        		break;
        	}
    	}
    	_logger.info("Writing {}", gson.toJson(iopWrite));
    	this._sendMessage(gson.toJson(iopWrite));

    	List<IopListener> l = null;
    	switch (iop.type) {
		case IGS_INPUT_T:
			l = this.inputListeners.get(iop.name);
			break;
		case IGS_OUTPUT_T:
			l = this.outputListeners.get(iop.name);
			break;
		case IGS_PARAMETER_T:
			l = this.parameterListeners.get(iop.name);
			break;

		default:
			break;
		}
    	if (l != null) {
    		for (IopListener listener : l) {
    			listener.handleIOP(this, iop.type, iop.name, iop.valueType, iop.value);
    		}
    	}

    }


    /////////////////////////////////////////////////////////////////////////////////////
    // INGESCAPE
    public void start() {
    	if (!isStarted) {
    		isStarted = true;
    		if (isConnected) {
    			Map<String, String> json = new HashMap<>();
    			json.put("event", "start");
    			json.put("uuid", this.uuid);
    			String message = gson.toJson(json);
    			this._sendMessage(message);
    			_logger.info("start: {}", message);
    		}
    	}
    }

    public void stop() {
    	if (isStarted) {
    		isStarted = false;
    		if (isConnected) {
    			Map<String, String> json = new HashMap<>();
    			json.put("event", "stop");
    			json.put("uuid", this.uuid);
    			String message = gson.toJson(json);
    			this._sendMessage(message);
    			_logger.info("stop: {}", message);
    		}
    	}
    }

    public void setName(String name) {
    	this.definition.setName(name);
    }

    public String name() {
    	return this.definition.name;
    }

    public void observeAgentEvents(AgentEventListener listener) {
    	this.agentEventListeners.add(listener);
    }

//    public void outputSet(String name, Object value) {
//    	DefinitionIop iop = this.definition.outputs.get(name);
//    	if (iop != null) {
//    		iop.value = value;
//    		_writeIop(iop);
//    	}
//    	else {
//    		//TODO: error, iop does not exist
//    	}
//    }

    public void outputSetBool(String name, boolean value) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = value;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

    public void outputSetInt(String name, int value) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = value;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

    public void outputSetDouble(String name, double value) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = value;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

    public void outputSetString(String name, String value) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = value;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

    public void outputSetData(String name, byte[] value) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = value;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

    public void outputSetImpulsion(String name) {
    	DefinitionIop iop = this.definition.outputs.get(name);
    	if (iop != null) {
    		iop.value = null;
    		_writeIop(iop);
    	}
    	else {
    		//TODO: error, iop does not exist
    	}
    }

	//observe iops
	public void observeInput(String name, IopListener listener) {
		List<IopListener> listeners = this.inputListeners.get(name);
		if (listeners == null) {
			listeners = new Vector<>();
			this.inputListeners.put(name, listeners);
		}
		listeners.add(listener);
	}

	public void observeParameter(String name, IopListener listener) {
		List<IopListener> listeners = this.parameterListeners.get(name);
		if (listeners == null) {
			listeners = new Vector<>();
			this.parameterListeners.put(name, listeners);
		}
		listeners.add(listener);
	}

	//services
	public void serviceInit(String name, ServiceListener listener) {
		DefinitionService service = this.definition.services.get(name);
		if (service == null) {
			service = new DefinitionService();
			service.name = name;
			service.listener = listener;
			this.definition.services.put(name, service);
			this.definition.hasBeenUpdated = true;
		}
		else {
			//TODO: error, service already exists
		}
	}

	public void serviceRemove(String name) {
		DefinitionService service = this.definition.services.get(name);
		if (service != null) {
			this.definition.services.remove(name);
			this.definition.hasBeenUpdated = true;
		}
		else {
			//TODO: error, service does not exist
		}
	}

	public void serviceArgAdd(String serviceName, String argumentName, IopType argumentType) {
		DefinitionService service = this.definition.services.get(serviceName);
		if (service != null) {
			ServiceArgument arg = new ServiceArgument();
			arg.name = argumentName;
			arg.type = argumentType;
			service.arguments.add(arg);
			this.definition.hasBeenUpdated = true;
		}
		else {
			//TODO: error, service does not exist
		}
	}

	public void serviceArgRemove(String serviceName, String argumentName) {
		DefinitionService service = this.definition.services.get(serviceName);
		if (service != null) {
			for (ServiceArgument arg : service.arguments) {
				if (arg.name.equals(argumentName)) {
					service.arguments.remove(arg);
					this.definition.hasBeenUpdated = true;
				}
			}
		}
		else {
			//TODO: error, service does not exist
		}
	}

	public void serviceCall(String agentNameOrUUID, String serviceName, List<Object> arguments, String token) {
		boolean okForSending = true;
		Map<String, Object> root = new HashMap<>();
		root.put("event", "send_call");
		root.put("uuid", this.uuid);
		root.put("agent_name", agentNameOrUUID);
		root.put("service_name", serviceName);
		root.put("token", token);
		List<Object> args = new Vector<>();
		root.put("arguments_call", args);
		for (Object o : arguments) {
			Map<String,Object> arg = new HashMap<>();
			if (o instanceof Integer) {
				arg.put("type", IopType.toInt(IopType.IGS_INTEGER_T));
				arg.put("value", o);
				args.add(arg);
			}
			else if (o instanceof Double || o instanceof Float) {
				arg.put("type", IopType.toInt(IopType.IGS_DOUBLE_T));
				arg.put("value", o);
				args.add(arg);
			}
			else if (o instanceof Boolean) {
				arg.put("type", IopType.toInt(IopType.IGS_BOOL_T));
				arg.put("value", o);
				args.add(arg);
			}
			else if (o instanceof String) {
				arg.put("type", IopType.toInt(IopType.IGS_STRING_T));
				arg.put("value", o);
				args.add(arg);
			}
			else if (o instanceof byte[]) {
				arg.put("type", IopType.toInt(IopType.IGS_DATA_T));
				byte[] array = (byte[]) o;
				arg.put("value", Base64.getMimeEncoder().encodeToString(array));
				args.add(arg);
			}
			else {
				_logger.error("Type {} is not allowed as service argument in serviceCall", o.getClass());
				okForSending = false;
			}
		}
		if (okForSending) {
			String msg = gson.toJson(root);
			_logger.info("Call service: {}", msg);
			this._sendMessage(msg);
		}
	}
}
