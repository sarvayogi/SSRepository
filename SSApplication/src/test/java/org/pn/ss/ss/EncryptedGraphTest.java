package org.pn.ss.ss;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pn.ss.exception.SSException;
import org.pn.ss.model.Graph;
import org.pn.ss.model.Node;
import org.pn.ss.model.viam.VIAMNodeRelationshipExcelModel;
import org.pn.ss.service.ExcelUtils;
import org.pn.ss.service.GraphEncryptUtils;
import org.pn.ss.service.Neo4jGraphAdapter;
import org.pn.ss.service.viam.VIAMGraphUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EncryptedGraphTest {
	final static Logger logger = Logger.getLogger(EncryptedGraphTest.class);
	private static final String INSERT_ONLY_EXCEL = "C:\\Software\\Final\\src\\main\\resources\\VIAM.xlsx";
	private static final String UPDATE_EXCEL = "C:\\Software\\Final\\src\\main\\resources\\VIAMupdate.xlsx";
	@Autowired
	private ExcelUtils<VIAMNodeRelationshipExcelModel> excelUtils;
	@Autowired
	private VIAMGraphUtils VIAMGraphutils;
	@Autowired
	private Neo4jGraphAdapter neo4jGraphUtils;
	@Autowired
	private GraphEncryptUtils graphEncryptUtils;
	//@Test
	public void testuploadEncryptedSuccessExcel() throws IOException, SSException {
		testDeleteEncryptedNodesByKey();
		testifEncryptedNodeExistsByKey();
		
		Graph graph = seedVIAMGraphFromExcelFile(INSERT_ONLY_EXCEL);
		neo4jGraphUtils.createEncryptedGraph(graph);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter();
		logger.debug(mapper.writeValueAsString(graph));
		mapper.writeValue(new File("C:\\Software\\Final\\src\\main\\resources\\graph.json"),
				graph);
		testifEncryptedNodeExistsByKey();
	}

	 //@Test
	public void testupdateEncryptedUploadSuccessExcel() throws IOException, SSException {
		
		Graph graph = seedVIAMGraphFromExcelFile(UPDATE_EXCEL);
		neo4jGraphUtils.createEncryptedGraph(graph);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter();
		logger.debug(mapper.writeValueAsString(graph));
		mapper.writeValue(new File("C:\\Software\\Final\\src\\main\\resources\\graph.json"),
				graph);
		testifEncryptedNodeExistsByKey();
	}



	//@Test
	public void testDeleteEncryptedNodes() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			graphEncryptUtils.encrypt(graph);
			neo4jGraphUtils.deleteGraph(graph);

		} catch (SSException e) {
			e.printStackTrace();
		}

	}	

	// @Test
	 public void testDeleteEncryptedNodesByKey() throws IOException {

			Graph graph = new Graph();
			try {
				List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
						new VIAMNodeRelationshipExcelModel());
				graph = VIAMGraphutils.createGraph(VIAMExcelModels);
				graphEncryptUtils.encrypt(graph);
				neo4jGraphUtils.deleteGraphByKey(graph);

			} catch (SSException e) {
				e.printStackTrace();
			}

		}

	// @Test
	public void testifEncryptedNodeExistsByKey() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			graphEncryptUtils.encrypt(graph);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				logger.debug(neo4jGraphUtils.checkifNodeExistsbyKey(node));
			}

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

	
	private Graph seedVIAMGraphFromExcelFile(String excelFilePath) {
		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(excelFilePath,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			

		} catch (SSException e) {
			e.printStackTrace();
		}
		return graph;

	}
	//@Test
	public void testGetChildNodes() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			graphEncryptUtils.encrypt(graph);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				Map<String, String> map = node.getAttributes();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.equals("isPhysical") && value.startsWith("6")){
						List<Node> leafNodes = neo4jGraphUtils.getLeafNodes(node);
						graphEncryptUtils.decryptNodes(leafNodes);
						if (leafNodes == null|| leafNodes.size()== 0){return;}
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter();
						logger.debug(mapper.writeValueAsString(leafNodes));
						return;
					}		
				}
				
			}

		} catch (SSException e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testGetChildNodesAtDepth() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			graphEncryptUtils.encrypt(graph);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				Map<String, String> map = node.getAttributes();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.equals("isPhysical") && value.startsWith("6")){
						List<Node> leafNodes = neo4jGraphUtils.getLeafNodes(node,2);
						graphEncryptUtils.decryptNodes(leafNodes);
						if (leafNodes == null|| leafNodes.size()== 0){return;}
						ObjectMapper mapper = new ObjectMapper();
						mapper.writerWithDefaultPrettyPrinter();
						logger.debug(mapper.writeValueAsString(leafNodes));
						return;
					}		
				}
			}

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

}
