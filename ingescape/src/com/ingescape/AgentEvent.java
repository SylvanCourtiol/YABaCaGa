package com.ingescape;

public enum AgentEvent {
	IGS_PEER_ENTERED, 
	IGS_PEER_EXITED,  
	IGS_AGENT_ENTERED,
	IGS_AGENT_UPDATED_DEFINITION,
	IGS_AGENT_KNOWS_US,
	IGS_AGENT_EXITED,
	IGS_AGENT_UPDATED_MAPPING,
	IGS_AGENT_WON_ELECTION,
	IGS_AGENT_LOST_ELECTION;
	
	public static AgentEvent fromInt(int x) {
        switch(x) {
        case 1:
            return IGS_PEER_ENTERED;
        case 2:
            return IGS_PEER_EXITED;
        case 3:
            return IGS_AGENT_ENTERED;
        case 4:
            return IGS_AGENT_UPDATED_DEFINITION;
        case 5:
            return IGS_AGENT_KNOWS_US;
        case 6:
            return IGS_AGENT_EXITED;
        case 7:
            return IGS_AGENT_UPDATED_MAPPING;
        case 8:
            return IGS_AGENT_WON_ELECTION;
        case 9:
            return IGS_AGENT_LOST_ELECTION;
        }
        return null;
    }
}
