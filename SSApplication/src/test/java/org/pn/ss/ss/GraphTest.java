package org.pn.ss.ss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pn.ss.exception.SSException;
import org.pn.ss.model.Graph;
import org.pn.ss.model.Node;
import org.pn.ss.model.viam.VIAMNodeRelationshipExcelModel;
import org.pn.ss.service.ExcelUtils;
import org.pn.ss.service.Neo4jGraphAdapter;
import org.pn.ss.service.viam.VIAMGraphUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphTest {
	final static Logger logger = Logger.getLogger(GraphTest.class);
	private static final String INSERT_ONLY_EXCEL = "C:\\Software\\Final\\src\\main\\resources\\VIAM.xlsx";
	private static final String UPDATE_EXCEL = "C:\\Software\\Final\\src\\main\\resources\\VIAMupdate.xlsx";
	@Autowired
	private ExcelUtils<VIAMNodeRelationshipExcelModel> excelUtils;
	@Autowired
	private VIAMGraphUtils VIAMGraphutils;
	@Autowired
	private Neo4jGraphAdapter neo4jGraphUtils;

	//@Test
	public void testuploadSuccessExcel() throws IOException, SSException {
		testDeleteNodesByKey();
		testifNodeExistsByKey();
		Graph graph = seedVIAMGraphFromExcelFile(INSERT_ONLY_EXCEL);
		neo4jGraphUtils.createGraph(graph);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter();
		logger.debug(mapper.writeValueAsString(graph));
		File file = new File("C:\\Software\\Final\\src\\main\\resources\\graph.json");
		mapper.writeValue(file,	graph);
		testifNodeExistsByKey();
	}

	// @Test
	public void testupdateUploadSuccessExcel() throws IOException, SSException {
		Graph graph = seedVIAMGraphFromExcelFile(UPDATE_EXCEL);
		neo4jGraphUtils.createGraph(graph);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter();
		logger.debug(mapper.writeValueAsString(graph));
		mapper.writeValue(new File("C:\\Software\\Final\\src\\main\\resources\\graph.json"),
				graph);
		testifNodeExistsByKey();
	}

	// @Test
	public void testDeleteNodes() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);

			neo4jGraphUtils.deleteGraph(graph);

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testDeleteNodesByKey() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);

			neo4jGraphUtils.deleteGraphByKey(graph);

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testUpdateNodes() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);
			List<Node> nodeList = new ArrayList<Node>(nodes);
			int i = 0;
			for (Node node : nodeList) {
				node.setAggrementType(node.getAggrementType() + i);
				Map<String, String> tempMap = new HashedMap<String, String>();
				for (Map.Entry<String, String> entry : node.getAttributes().entrySet()) {
					String value = entry.getValue() + i;
					tempMap.put(entry.getKey(), value);

				}
				node.setAttributes(tempMap);
			}
			neo4jGraphUtils.updateNodesByKey(nodeList);

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testifNodeExistsByKey() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				logger.debug(neo4jGraphUtils.checkifNodeExistsbyKey(node));
			}

		} catch (SSException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testuploadFailureExcel() throws IOException {
		Graph graph = seedVIAMGraphFromExcelFile(
				"C:\\Software\\Final\\src\\main\\resources\\VIAMerror.xlsx");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter();
		logger.debug(mapper.writeValueAsString(graph));
		mapper.writeValue(new File(INSERT_ONLY_EXCEL), graph);
	}

	 @Test
	public void testGetChildNodes() throws IOException {
		 
		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				Map<String, String> map = node.getAttributes();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.equals("isPhysical") && value.equalsIgnoreCase("Yes")) {
						List<Node> leafNodes = neo4jGraphUtils.getLeafNodes(node);
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
	// @Test
	public void testGetChildNodesAtDepth() throws IOException {

		Graph graph = new Graph();
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(INSERT_ONLY_EXCEL,
					new VIAMNodeRelationshipExcelModel());
			graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			Set<Node> nodes = neo4jGraphUtils.getDistinctNodes(graph);

			for (Node node : nodes) {
				Map<String, String> map = node.getAttributes();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.equals("isPhysical") && value.equalsIgnoreCase("Yes")) {
						List<Node> leafNodes = neo4jGraphUtils.getLeafNodes(node,2);
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

}
