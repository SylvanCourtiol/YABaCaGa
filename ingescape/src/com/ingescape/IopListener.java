package com.ingescape;

public interface IopListener {
	//TODO: check if we need to specialize by value type
	public void handleIOP (Agent agent, Iop iop, String name, IopType type, Object value);
}
