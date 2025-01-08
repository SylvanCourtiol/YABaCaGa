package com.ingescape;
import java.util.*;

public class Definition {
	String name;
	String description;
	String version;
	Map<String, DefinitionIop> inputs = new HashMap<>();
	Map<String, DefinitionIop> outputs = new HashMap<>();
	Map<String, DefinitionIop> parameters = new HashMap<>();
	Map<String, DefinitionService> services = new HashMap<>();
	public boolean hasBeenUpdated = false;
	
	public Definition() {
		this.hasBeenUpdated = false;
	}
	
	public void clear() {
		this.description = "";
		this.version = "";
		this.inputs.clear();
		this.outputs.clear();
		this.parameters.clear();
		this.services.clear();
		this.hasBeenUpdated = true;
	}
	
	public Map<String, Object> toJson() {
		Map<String, Object> root = new HashMap<>();
		root.put("name", this.name);
		root.put("description", this.description);
		root.put("version", this.version);
		List<Object> i = new ArrayList<>();
		List<Object> o = new ArrayList<>();
		List<Object> p = new ArrayList<>();
		List<Object> c = new ArrayList<>();
		root.put("inputs", i);
		root.put("outputs", o);
		root.put("parameters", p);
		root.put("services", c);
		for (DefinitionIop iop : inputs.values()) {
			Map<String, Object> iopMap = new HashMap<>();
			i.add(iopMap);
			iopMap.put("name", iop.name);
			iopMap.put("value", "");
			iopMap.put("type", IopType.toNormalizedName(iop.valueType));
		}
		for (DefinitionIop iop : outputs.values()) {
			Map<String, Object> iopMap = new HashMap<>();
			o.add(iopMap);
			iopMap.put("name", iop.name);
			iopMap.put("value", "");
			iopMap.put("type", IopType.toNormalizedName(iop.valueType));
		}
		for (DefinitionIop iop : parameters.values()) {
			Map<String, Object> iopMap = new HashMap<>();
			p.add(iopMap);
			iopMap.put("name", iop.name);
			iopMap.put("value", "");
			iopMap.put("type", IopType.toNormalizedName(iop.valueType));
		}
		for (DefinitionService service : services.values()) {
			Map<String, Object> serviceMap = new HashMap<>();
			c.add(serviceMap);
			serviceMap.put("name", service.name);
			serviceMap.put("description", service.description);
			List<Object> arguments = new ArrayList<>();
			serviceMap.put("arguments", arguments);
			for (ServiceArgument arg : service.arguments) {
				Map<String, Object> argMap = new HashMap<>();
				arguments.add(argMap);
				argMap.put("name", arg.name);
				argMap.put("type", IopType.toNormalizedName(arg.type));
			}
		}
		return root;
	}
	
	//main attributes
	public void setName(String name) {
		this.name = name;
		this.hasBeenUpdated = true;
	}
	public String name() {
		return this.name;
	}
	public void setDescription(String description) {
		this.description = description;
		this.hasBeenUpdated = true;
	}
	public String description() {
		return this.description;
	}
	public void setVersion(String version) {
		this.version = version;
		this.hasBeenUpdated = true;
	}
	public String version() {
		return this.version;
	}
	
	//create/remove iops
	public void inputCreate(String name, IopType type) {
		DefinitionIop iop = this.inputs.get(name);
		if (iop == null) {
			iop = new DefinitionIop();
			iop.type = Iop.IGS_INPUT_T;
			iop.name = name;
			iop.valueType = type;
			this.inputs.put(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop exists
		}
	}
	public void inputRemove(String name) {
		DefinitionIop iop = this.inputs.get(name);
		if (iop != null) {
			this.inputs.remove(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop does not exists
		}
	}
	public void outputCreate(String name, IopType type) {
		DefinitionIop iop = this.outputs.get(name);
		if (iop == null) {
			iop = new DefinitionIop();
			iop.type = Iop.IGS_OUTPUT_T;
			iop.name = name;
			iop.valueType = type;
			this.outputs.put(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop exists
		}
	}
	public void outputRemove(String name) {
		DefinitionIop iop = this.outputs.get(name);
		if (iop != null) {
			this.outputs.remove(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop does not exists
		}
	}
	public void parameterCreate(String name, IopType type) {
		DefinitionIop iop = this.parameters.get(name);
		if (iop == null) {
			iop = new DefinitionIop();
			iop.type = Iop.IGS_PARAMETER_T;
			iop.name = name;
			iop.valueType = type;
			this.parameters.put(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop exists
		}
	}
	public void parameterRemove(String name) {
		DefinitionIop iop = this.parameters.get(name);
		if (iop != null) {
			this.parameters.remove(name, iop);
			this.hasBeenUpdated = true;
		} else {
			//TODO: error, iop does not exists
		}
	}
	
}
