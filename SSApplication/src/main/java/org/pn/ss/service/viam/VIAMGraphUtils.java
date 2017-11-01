package org.pn.ss.service.viam;

import java.util.HashMap;
import java.util.Map;

import org.pn.ss.exception.SSException;
import org.pn.ss.model.Association;
import org.pn.ss.model.Node;
import org.pn.ss.model.Relationship;
import org.pn.ss.model.viam.VIAMNodeRelationshipExcelModel;
import org.pn.ss.service.GraphUtils;
import org.springframework.stereotype.Service;

@Service
public class VIAMGraphUtils extends GraphUtils<VIAMNodeRelationshipExcelModel>{

	@Override
	public Relationship createRelationShip( VIAMNodeRelationshipExcelModel modelInstance)
			throws SSException {
		Relationship relationship = new Relationship();
		relationship.setName("linkTo");
		return relationship;
		
	}

	@Override
	public Node createTargetNode( VIAMNodeRelationshipExcelModel modelInstance)
			throws SSException {
		Node node = new Node();
		node.setNodeName(modelInstance.getTargetNodeName());
		node.setAggrementType(modelInstance.getTargetAggrementType());
		Map<String,String> attributes = new HashMap<String,String>();
		attributes.put("isPhysical", modelInstance.getIsTargetNodePhysical());
		node.setAttributes(attributes);
		return node;
	}

	@Override
	public Node createSourceNode( VIAMNodeRelationshipExcelModel modelInstance)
			throws SSException {
		Node node = new Node();
		node.setNodeName(modelInstance.getSourceNodeName());
		node.setAggrementType(modelInstance.getSourceAggrementType());
		Map<String,String> attributes = new HashMap<String,String>();
		attributes.put("isPhysical", modelInstance.getIsSourceNodePhysical());
		node.setAttributes(attributes);
		return node;
	}

	@Override
	public void setAssociationActionType(Association association, VIAMNodeRelationshipExcelModel modelInstance)
			throws SSException {
		association.setActionType(modelInstance.getAction());
		
	}

	

}
