package org.pn.ss.model;

import java.util.List;

public class Graph {
	private List<Association> associations;
	private boolean isVersoningEnabled = true ;
	public List<Association> getAssociations() {
		return associations;
	}
	public void setAssociations(List<Association> associations) {
		this.associations = associations;
	}
	public boolean isVersoningEnabled() {
		return isVersoningEnabled;
	}
	public void setVersoningEnabled(boolean isVersoningEnabled) {
		this.isVersoningEnabled = isVersoningEnabled;
	}
	@Override
	public String toString() {
		return "Graph [associations=" + associations + ", isVersoningEnabled=" + isVersoningEnabled + "]";
	}
	
}
