package org.pn.ss.model;

public class Association {
	
	private Node source;
	private Relationship relationship;
	private Node target;
	private String actionType ;
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Relationship getRelationship() {
		return relationship;
	}
	public void setRelationship(Relationship relationship) {
		this.relationship = relationship;
	}
	public Node getTarget() {
		return target;
	}
	public void setTarget(Node target) {
		this.target = target;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	@Override
	public String toString() {
		return "Association [source=" + source + ", relationship=" + relationship + ", target=" + target
				+ ", actionType=" + actionType + "]";
	}


}
