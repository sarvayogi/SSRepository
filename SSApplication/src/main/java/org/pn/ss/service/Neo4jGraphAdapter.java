package org.pn.ss.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.pn.ss.AppProperties;
import org.pn.ss.exception.SSException;
import org.pn.ss.model.Association;
import org.pn.ss.model.Graph;
import org.pn.ss.model.Node;
import org.pn.ss.model.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class Neo4jGraphAdapter {
	final static Logger logger = Logger.getLogger(Neo4jGraphAdapter.class);
	private List<String> coreFields = Arrays.asList("name", "aggrementType", "createdDate", "lastUpdatedDate",
			"isLatest", "versionNo");
	private static final String CIPHER_JSON = "C:\\Software\\Final\\src\\main\\resources\\transaction.json";

	public static Driver driver = null;

	@Autowired
	private AppProperties properties;

	@Autowired
	private GraphEncryptUtils graphEncryptUtils;

	public Driver getDriver() {
		return driver;
	}

	public AppProperties getProperties() {
		return properties;
	}

	public void setProperties(AppProperties properties) {
		this.properties = properties;
	}

	public Driver connect() {
		if (driver == null) {
			driver = GraphDatabase.driver(properties.getNeo4jUrl(),
					AuthTokens.basic(properties.getNeo4jUserName(), properties.getNeo4jPassword()));
		}
		return driver;
	}

	public void createGraph(Graph graph) throws SSException {
		if (graph == null)
			return;
		List<String> cipherQueries = new ArrayList<String>();
		processNodes(graph, cipherQueries);
		processRelationship(graph, cipherQueries);

		submitCyphertoNeo4j(cipherQueries);

	}

	public void createEncryptedGraph(Graph graph) throws SSException {
		graphEncryptUtils.encrypt(graph);
		createGraph(graph);
	}

	public void submitCyphertoNeo4j(List<String> cipherQueries) {
		connect();

		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				for (String cipherQuery : cipherQueries) {
					tx.run(cipherQuery);
				}
				tx.success(); // Mark this write as successful.

			}

		}
		// writeCipherAsJSON(cipherQueries);
	}

	public void deleteGraph(Graph graph) throws SSException {
		if (graph == null) {
			return;
		}

		Set<Node> nodes = getDistinctNodes(graph);
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		List<Node> nodeList = new ArrayList<Node>(nodes);
		deleteNodes(nodeList);

	}

	public void deleteGraphByKey(Graph graph) throws SSException {
		if (graph == null) {
			return;
		}

		Set<Node> nodes = getDistinctNodes(graph);
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		List<Node> nodeList = new ArrayList<Node>(nodes);
		deleteNodeByKey(nodeList);

	}

	public void deleteNodes(List<Node> nodes) throws SSException {
		if (nodes == null || nodes.size() == 0) {
			return;
		}

		List<String> deleteCyphers = new ArrayList<String>();

		for (Node node : nodes) {
			StringBuffer buf = new StringBuffer();
			buf.append("MATCH (a) where ");
			Map<String, String> nodeMap = convertNodeToMap(node);
			deleteEntryWithKey(nodeMap, "lastUpdatedDate");
			deleteEntryWithKey(nodeMap, "createdDate");
			Map<String, String> prefixedMap = prefixKeyswithAlias(nodeMap, "a");
			buf.append(generateWhereClause(prefixedMap));
			buf.append("  DETACH DELETE a");
			deleteCyphers.add(buf.toString());
		}
		submitCyphertoNeo4j(deleteCyphers);
	}

	public void deleteNodeByKey(List<Node> nodes) throws SSException {
		if (nodes == null || nodes.size() == 0) {
			return;
		}

		List<String> deleteCyphers = new ArrayList<String>();

		for (Node node : nodes) {
			StringBuffer buf = new StringBuffer();
			buf.append("MATCH (a) where ");
			Map<String, String> nodeMap = convertNodeToMap(node);
			Map<String, String> onlyKeyMap = deleteEntryWithoutKey(nodeMap, Arrays.asList("name"));
			Map<String, String> prefixedMap = prefixKeyswithAlias(onlyKeyMap, "a");
			buf.append(generateWhereClause(prefixedMap));
			buf.append("  DETACH DELETE a");
			deleteCyphers.add(buf.toString());
		}
		submitCyphertoNeo4j(deleteCyphers);
	}

	public void updateNodesByKey(List<Node> nodes) throws SSException {
		if (nodes == null || nodes.size() == 0) {
			return;
		}

		List<String> updateCyphers = new ArrayList<String>();

		for (Node node : nodes) {

			String cypherQuery = updateNodeByKey(node);
			updateCyphers.add(cypherQuery);
		}
		submitCyphertoNeo4j(updateCyphers);
	}

	private void processRelationship(Graph graph, List<String> cipherQueries) throws SSException {
		for (Association association : graph.getAssociations()) {
			String cipherQuery = null;
			if (association.getActionType().equalsIgnoreCase("Delete")) {
				cipherQuery = deleteRelationship(association);
			} else {
				cipherQuery = createNeo4jRelationshipIfNotExists(association);
			}
			if (StringUtils.isNotEmpty(cipherQuery)) {
				cipherQueries.add(cipherQuery);
			}

		}
	}

	private void processNodes(Graph graph, List<String> cipherQueries) throws SSException {
		Set<Node> nodes = getDistinctNodes(graph);
		for (Node node : nodes) {
			String cipherQuery = createOrUpdateNeo4jNode(node);
			if (StringUtils.isNotEmpty(cipherQuery)) {
				cipherQueries.add(cipherQuery);
			}
		}
	}

	private void writeCipherAsJSON(List<String> cipherQueries) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.writerWithDefaultPrettyPrinter();
		try {
			mapper.writeValue(new File(CIPHER_JSON), cipherQueries);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String updateNodeByKey(Node node) throws SSException {
		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a:Account {");
		Map<String, String> nodeMap = convertNodeToMap(node);
		Map<String, String> onlyKeyMap = deleteEntryWithoutKey(nodeMap, Arrays.asList("name", "isLatest"));
		buf.append(getPlaceHolderValues(onlyKeyMap));
		buf.append(" }) ");

		buf.append("  Set a += { ");
		deleteEntryWithKey(nodeMap, "name");
		buf.append(getPlaceHolderValues(nodeMap));
		buf.append(" } ");
		return buf.toString();
	}

	public boolean checkifNodeExistsbyKey(Node node) throws SSException {

		if (node == null)
			return false;

		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a:Account {");
		Map<String, String> nodeMap = convertNodeToMap(node);
		Map<String, String> onlyKeyMap = deleteEntryWithoutKey(nodeMap, Arrays.asList("name", "isLatest"));
		buf.append(getPlaceHolderValues(onlyKeyMap));
		buf.append(" }) ");

		buf.append("  RETURN a.name ");
		connect();
		try (Session session = driver.session()) {
			StatementResult result = session.run(buf.toString());
			while (result.hasNext()) {
				return true;
			}

		}
		return false;

	}

	public Node getLatestNodePropertiesFromDB(Node node) throws SSException {
		if (node == null) {
			return null;
		}

		node.setLatest(true);
		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a) where");
		Map<String, String> nodeMap = convertNodeToMap(node);
		Map<String, String> prefixedMap = prefixKeyswithAlias(nodeMap, "a");
		Map<String, String> onlyKeyMap = deleteEntryWithoutKey(prefixedMap, Arrays.asList("a.name"));
		buf.append(generateWhereClause(onlyKeyMap));
		buf.append(" return properties(a) ");

		connect();
		try (Session session = driver.session()) {
			StatementResult result = session.run(buf.toString());
			while (result.hasNext()) {
				Record record = result.next();
				Map<String, Object> tempMap = record.get(0).asMap();
				return convertToNodeFromMap(tempMap);

			}

		}
		return node;

	}

	public List<Node> getLeafNodes(Node node, int depth) throws SSException {
		if (node == null) {
			return null;
		}

		node.setLatest(true);
		StringBuffer buf = new StringBuffer();
		buf.append("Match (a:Account)-[*");
		if (depth > 0) {
			buf.append(depth);
		}
		buf.append("]->(s:Account) where");
		Map<String, String> nodeMap = convertNodeToMap(node);
		Map<String, String> prefixedMap = prefixKeyswithAlias(nodeMap, "a");
		Map<String, String> onlyKeyMap = deleteEntryWithoutKey(prefixedMap, Arrays.asList("a.name"));
		buf.append(generateWhereClause(onlyKeyMap));
		buf.append(" return properties(s) ");
		List<Node> nodes = new ArrayList<Node>();
		connect();
		try (Session session = driver.session()) {
			StatementResult result = session.run(buf.toString());
			while (result.hasNext()) {
				Record record = result.next();
				Map<String, Object> tempMap = record.get(0).asMap();
				Node tempNode = convertToNodeFromMap(tempMap);
				if (tempNode != null) {
					nodes.add(tempNode);
				}

			}

		}
		return nodes;

	}

	public List<Node> getLeafNodes(Node node) throws SSException {
		return getLeafNodes(node, -1);
	}

	private Node convertToNodeFromMap(Map<String, Object> nodeMap) {
		Node node = new Node();
		node.setNodeName((String) nodeMap.get("name"));
		node.setAggrementType((String) nodeMap.get("aggrementType"));
		node.setCreatedDate((String) nodeMap.get("createdDate"));
		node.setLastUpdatedDate((String) nodeMap.get("lastUpdatedDate"));
		String isLatest = (String) nodeMap.get("isLatest");
		if (StringUtils.isNotEmpty(isLatest) && isLatest.equalsIgnoreCase("true")) {
			node.setLatest(true);
		} else {
			node.setLatest(false);
		}

		node.setVersionNo(Integer.valueOf((String) nodeMap.get("versionNo")));
		Map<String, String> attributes = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {

			if (!coreFields.contains(entry.getKey())) {
				attributes.put(entry.getKey(), (String) entry.getValue());
			}

		}
		node.setAttributes(attributes);

		return node;
	}

	public boolean checkifRelationshipExistsbyKey(Association association) throws SSException {

		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a)-[r]-(b) WHERE");
		buf.append(" a.name =\"" + association.getSource().getNodeName() + "\"");
		buf.append(" AND b.name =\"" + association.getTarget().getNodeName() + "\"");
		buf.append(" RETURN TYPE(r)");

		connect();
		try (Session session = driver.session()) {
			StatementResult result = session.run(buf.toString());
			while (result.hasNext()) {
				return true;
			}

		}
		return false;

	}

	private String generateWhereClause(Map<String, String> map) throws SSException {
		StringBuffer buf = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			buf.append(" ");
			buf.append(entry.getKey());
			buf.append(" =");
			buf.append('"');
			buf.append(entry.getValue());
			buf.append('"');
			buf.append(" and");

		}
		String placeHolderValues = buf.toString();
		if (StringUtils.isEmpty(placeHolderValues)) {
			throw new SSException("Fatal Error : Node cannot be empty");
		}
		placeHolderValues = trimDelimiterAtTheEnd(placeHolderValues, "and");
		return placeHolderValues;
	}

	public Set<Node> getDistinctNodes(Graph graph) throws SSException {
		Set<Node> nodes = new HashSet<Node>();

		for (Association association : graph.getAssociations()) {
			nodes.add(association.getSource());
			nodes.add(association.getTarget());
		}
		return nodes;

	}

	private String deleteRelationship(Association association) throws SSException {
		// TODO Auto-generated method stub
		if (!checkifRelationshipExistsbyKey(association)) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a)-[r:" + association.getRelationship().getName() + "]-(b) WHERE");
		buf.append(" a.name =\"" + association.getSource().getNodeName() + "\"");
		buf.append(" AND b.name =\"" + association.getTarget().getNodeName() + "\"");
		buf.append(" DELETE r ");
		return buf.toString();

	}

	private String createNeo4jRelationshipIfNotExists(Association association) throws SSException {
		// TODO Auto-generated method stub
		if (checkifRelationshipExistsbyKey(association)) {
			logger.debug("relationship exists");
			return null;
		}
		StringBuffer buf = new StringBuffer();
		buf.append("MATCH (a:Account),(b:Account) WHERE");
		buf.append(" a.name =\"" + association.getSource().getNodeName() + "\"");
		buf.append(" AND a.isLatest =\"" + association.getSource().isLatest() + "\"");
		buf.append(" AND b.name =\"" + association.getTarget().getNodeName() + "\"");
		buf.append(" AND b.isLatest =\"" + association.getSource().isLatest() + "\"");
		buf.append(" CREATE (a)-[r:");
		buf.append(association.getRelationship().getName());
		if (association.getRelationship().getAttributes() != null
				&& !association.getRelationship().getAttributes().isEmpty()) {
			Map<String, String> relMap = convertRelationshipToMap(association.getRelationship());
			buf.append("{");
			buf.append(getPlaceHolderValues(relMap));
			buf.append("}");

		}

		buf.append(" ]->(b)");
		return buf.toString();

	}
	// TODO Auto-generated method stub

	private String createNeo4jNode(Node node) throws SSException {
		StringBuffer buf = new StringBuffer();
		buf.append("CREATE (a:Account {");
		Map<String, String> nodeMap = convertNodeToMap(node);
		buf.append(getPlaceHolderValues(nodeMap));
		buf.append(" }) ");
		return buf.toString();
	}

	private String createOrUpdateNeo4jNode(Node node) throws SSException {

		if (checkifNodeExistsbyKey(node)) {

			return updateNodeByKey(node);
		} else {
			return createNeo4jNode(node);
		}

	}

	public String trimDelimiterAtTheEnd(String someString, String delimiter) {
		if (StringUtils.isEmpty(someString)) {
			return someString;
		}
		if (someString.endsWith(delimiter)) {
			someString = someString.substring(0, someString.length() - (delimiter.length()));
		}
		return someString;
	}

	private String getPlaceHolderValues(Map<String, String> nodeMap) throws SSException {
		return getPlaceHolderValues(nodeMap, ",");
	}

	private String getPlaceHolderValues(Map<String, String> nodeMap, String delimiter)
			throws SSException {
		StringBuffer buf = new StringBuffer();
		for (Map.Entry<String, String> entry : nodeMap.entrySet()) {

			buf.append(entry.getKey());
			buf.append(":");
			buf.append('"');
			buf.append(entry.getValue());
			buf.append('"');
			buf.append(delimiter);

		}
		String placeHolderValues = buf.toString();
		if (StringUtils.isEmpty(placeHolderValues)) {
			throw new SSException("Fatal Error : Node cannot be empty");
		}
		placeHolderValues = trimDelimiterAtTheEnd(placeHolderValues, delimiter);
		return placeHolderValues;
	}

	private Map<String, String> prefixKeyswithAlias(Map<String, String> nodeMap, String alias)
			throws SSException {
		Map<String, String> perfixedMap = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : nodeMap.entrySet()) {

			String key = alias + "." + entry.getKey();
			String value = entry.getValue();
			perfixedMap.put(key, value);
		}

		return perfixedMap;
	}

	private void deleteEntryWithKey(Map<String, String> nodeMap, String key) throws SSException {
		nodeMap.remove(key);

	}

	private Map<String, String> deleteEntryWithoutKey(Map<String, String> nodeMap, List<String> keys)
			throws SSException {
		Map<String, String> tempMap = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : nodeMap.entrySet()) {
			if (keys.contains(entry.getKey())) {
				tempMap.put(entry.getKey(), entry.getValue());
			}

		}
		nodeMap = new HashMap<String, String>();
		nodeMap.putAll(tempMap);
		return nodeMap;
	}

	private Map<String, String> convertNodeToMap(Node node) {
		Map<String, String> nodeMap = new HashMap<String, String>();
		nodeMap.put("name", node.getNodeName());
		nodeMap.put("aggrementType", node.getAggrementType());
		nodeMap.put("isLatest", String.valueOf(node.isLatest()));
		if (StringUtils.isEmpty(node.getCreatedDate())) {
			nodeMap.put("createdDate", String.valueOf(System.currentTimeMillis()));
		}

		nodeMap.put("lastUpdatedDate", String.valueOf(System.currentTimeMillis()));

		nodeMap.put("versionNo", String.valueOf(node.getVersionNo()));
		if (node.getAttributes() != null && node.getAttributes().size() > 0) {
			nodeMap.putAll(node.getAttributes());
		}
		return nodeMap;

	}

	private Map<String, String> convertRelationshipToMap(Relationship relationship) {
		Map<String, String> relMap = new HashMap<String, String>();
		relMap.put("name", relationship.getName());
		relMap.putAll(relationship.getAttributes());
		return relMap;

	}

	public static void main(String[] args) {
		Neo4jGraphAdapter utils = new Neo4jGraphAdapter();
		logger.debug(utils.trimDelimiterAtTheEnd("this and this and,", "and,"));
	}

}
