package com.ingescape;

public interface AgentEventListener {
	public void handleAgentEvent (Agent agent, AgentEvent event, String uuid, String name, Object eventData);
}
