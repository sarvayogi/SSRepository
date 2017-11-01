package org.pn.ss.rest;

import java.util.List;

import org.pn.ss.exception.SSException;
import org.pn.ss.model.Graph;
import org.pn.ss.model.viam.VIAMNodeRelationshipExcelModel;
import org.pn.ss.service.ExcelUtils;
import org.pn.ss.service.Neo4jGraphAdapter;
import org.pn.ss.service.viam.VIAMGraphUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class VIAMSSController {

	@Autowired
	private ExcelUtils<VIAMNodeRelationshipExcelModel> excelUtils;
	@Autowired
	private VIAMGraphUtils VIAMGraphutils;
	@Autowired
	private Neo4jGraphAdapter neo4jGraphUtils;
	@PostMapping("/api/VIAM/upload")
	public ResponseEntity<String> seedVIAMGraphFromExcel(@RequestParam("file") MultipartFile uploadfile) {
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(uploadfile,
					new VIAMNodeRelationshipExcelModel());
			Graph graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			neo4jGraphUtils.createGraph(graph);
			
		} catch (SSException e) {
			return error(e.getMessage());
		}
		return ok("The file has been successfully uploaded");

	}
	
	@PostMapping("/api/VIAM/encrypt/upload")
	public ResponseEntity<String> seedEncryptedVIAMGraphFromExcel(@RequestParam("file") MultipartFile uploadfile) {
		try {
			List<VIAMNodeRelationshipExcelModel> VIAMExcelModels = excelUtils.uploadExcel(uploadfile,
					new VIAMNodeRelationshipExcelModel());
			Graph graph = VIAMGraphutils.createGraph(VIAMExcelModels);
			neo4jGraphUtils.createEncryptedGraph(graph);
		} catch (SSException e) {
			return error(e.getMessage());
		}
		return ok("The file has been successfully uploaded");

	}
	
	private ResponseEntity<String> error(String message) {
		return new ResponseEntity<String>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<String> ok(String message) {
		return new ResponseEntity<String>(message, HttpStatus.OK);
	}

}
