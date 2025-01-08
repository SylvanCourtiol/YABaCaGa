package com.ingescape;
import java.util.*;

public class Mapping {
	static long indexReference = 0;
	List<MappingElement> elements = new ArrayList<>();
	public boolean hasBeenUpdated = false;
	
	static class MappingElement{
		long index;
		String input;
		String agent;
		String output;
		
		MappingElement() {
			this.index = Mapping.indexReference++;
		}
	}
	
	public Mapping() {
		// TODO Auto-generated constructor stub
	}
	
	public void clear() {
		this.elements.clear();
		this.hasBeenUpdated = true;
	}
	
	public ArrayList<Map<String, Object>> toJson() {
		ArrayList<Map<String, Object>> root = new ArrayList<>();
		for (MappingElement elem : this.elements) {
			Map<String, Object> elemHash = new HashMap<>();
			root.add(elemHash);
			elemHash.put("map_id", elem.index);
			elemHash.put("fromInput", elem.input);
			elemHash.put("toAgent", elem.agent);
			elemHash.put("toOutput", elem.output);
		}
		return root;
	}
	
	//main attributes		
	public void add(String inputName, String otherAgentName, String otherAgentOutputName) {
		boolean alreadyExists = false;
		for (MappingElement e : elements) {
			if (e.input.equals(inputName) && e.agent.equals(otherAgentName) && e.output.equals(otherAgentOutputName)) {
				System.out.println("error mapping already exists: " + inputName + "." + otherAgentName + "." + otherAgentOutputName);
				alreadyExists = true;
				break;
			}
		}

		if (!alreadyExists) {
			MappingElement e = new MappingElement();
			e.input = inputName;
			e.agent = otherAgentName;
			e.output = otherAgentOutputName;
			this.elements.add(e);
			this.hasBeenUpdated = true;
		}
	}
	public void remove(String inputName, String otherAgentName, String otherAgentOutputName) {
		for (MappingElement e : elements) {
			if (e.input.equals(inputName) && e.agent.equals(otherAgentName) && e.output.equals(otherAgentOutputName)) {
				this.elements.remove(e);
				this.hasBeenUpdated = true;
			}
		}
	}
	
}
