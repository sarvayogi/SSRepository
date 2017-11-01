package org.pn.ss.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class Relationship {
	@NotNull
	private String name;
	private Map<String,String> attributes;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	@Override
	public String toString() {
		return "Relationship [name=" + name + ", attributes=" + attributes + "]";
	}
	
	

}
