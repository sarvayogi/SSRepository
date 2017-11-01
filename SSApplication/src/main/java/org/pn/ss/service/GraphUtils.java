package org.pn.ss.service;

import java.util.ArrayList;
import java.util.List;

import org.pn.ss.exception.SSException;
import org.pn.ss.model.Association;
import org.pn.ss.model.Graph;
import org.pn.ss.model.Node;
import org.pn.ss.model.Relationship;

public abstract class GraphUtils<M> {

	
	public Graph createGraph(List<M> modelInstances) throws SSException {
		List<Association> associations = new ArrayList<Association>();
		Graph graph = new Graph();
		if(modelInstances == null || modelInstances.size() == 0){
			return graph;
		}
		for( M modelInstance : modelInstances){
			associations.add(createAssociation(modelInstance));
		}
		graph.setAssociations(associations);
		return graph;
		
	}
		
	public Association createAssociation(M modelInstance) throws SSException {
		Node sourceNode = createSourceNode(modelInstance);
		Relationship relationship = createRelationShip(modelInstance);
		Node targetNode = createTargetNode(modelInstance);
		Association association = new Association();
		association.setSource(sourceNode);
		association.setRelationship(relationship);
		association.setTarget(targetNode);
		setAssociationActionType(association ,modelInstance);
		return association;
	}

	public abstract Relationship createRelationShip(M modelInstance) throws SSException;

	public abstract Node createTargetNode(M modelInstance) throws SSException;

	public abstract Node createSourceNode(M modelInstance) throws SSException;
	
	public abstract void setAssociationActionType(Association association ,M modelInstance) throws SSException;
	
	

}
