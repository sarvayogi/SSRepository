package org.pn.ss.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pn.ss.exception.SSException;
import org.pn.ss.model.Association;
import org.pn.ss.model.Graph;
import org.pn.ss.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class GraphEncryptUtils {
	@Autowired
	EncryptUtils encryptUtils;
	public void encrypt(Graph graph) throws SSException {
		if (graph== null) return;
		for (Association association : graph.getAssociations()){
			 encryptNode(association.getSource());
			 encryptNode(association.getTarget());
		}
		
	}
	public void decrypt(Graph graph) throws SSException {
		if (graph== null) return;
		for (Association association : graph.getAssociations()){
			 decryptNode(association.getSource());
			 decryptNode(association.getTarget());
		}
		
	}
	public void encryptNode(Node node) throws SSException {
		if (node == null){ return;}
		node.setNodeName(encryptUtils.encrypt(node.getNodeName()));
		node.setAggrementType(encryptUtils.encrypt(node.getAggrementType()));
		Map<String,String> attributes = node.getAttributes();
		
		Map<String,String> tempMap = new HashMap<String, String>();
		encryptMap(attributes, tempMap);
		node.setAttributes(tempMap);
	}
	private void encryptMap(Map<String, String> attributes, Map<String, String> tempMap)
			throws SSException {
		if(attributes == null || attributes.isEmpty()){ return ;}
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			String key = entry.getKey();
			String value = encryptUtils.encrypt(entry.getValue());
			tempMap.put(key, value);
			
		}
	}
	public void decryptNode(Node node) throws SSException {
		if (node == null){ return;}
		node.setNodeName(encryptUtils.decrypt(node.getNodeName()));
		node.setAggrementType(encryptUtils.decrypt(node.getAggrementType()));
		Map<String,String> attributes = node.getAttributes();
		
		Map<String,String> tempMap = new HashMap<String, String>();
		decryptMap(attributes, tempMap);
		node.setAttributes(tempMap);
	}
	private void decryptMap(Map<String, String> attributes, Map<String, String> tempMap)
			throws SSException {
		if(attributes == null || attributes.isEmpty()){ return ;}
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			String key = entry.getKey();
			String value = encryptUtils.decrypt(entry.getValue());
			tempMap.put(key, value);
			
		}
	}
	public void encryptNodes(List<Node> nodes) throws SSException {
		if (nodes == null || nodes.size() ==0) return;
		for(Node node : nodes){
			encryptNode(node);
		}
		
	}
	public void decryptNodes(List<Node> nodes) throws SSException {
		if (nodes == null || nodes.size() ==0) return;
		for(Node node : nodes){
			decryptNode(node);
		}
		
	}
}
