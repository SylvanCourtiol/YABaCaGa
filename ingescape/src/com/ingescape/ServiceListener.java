package com.ingescape;

import java.util.*;

public interface ServiceListener {
	//TODO: handle arguments
	public void handleCallToService(Agent agent, String senderAgentName, String senderAgentUUID,
									String serviceName, List<Object> arguments, String token);
}
