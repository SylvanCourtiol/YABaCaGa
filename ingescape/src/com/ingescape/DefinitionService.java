package com.ingescape;

import java.util.*;

class DefinitionService {
	String name;
	String description;
	ServiceListener listener;
	List<ServiceArgument> arguments = new Vector<>();
	public DefinitionService() {}
}
