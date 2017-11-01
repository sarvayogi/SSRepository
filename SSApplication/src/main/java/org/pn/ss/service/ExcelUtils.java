package org.pn.ss.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pn.ss.exception.SSException;
import org.pn.ss.model.RowException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExcelUtils<I> {

	public List<I> uploadExcel(String excelFilePath, I excelModelInstance) throws SSException {
		if (StringUtils.isEmpty(excelFilePath)) {
			throw new SSException("Uploaded File cannot be empty");
		}
		FileInputStream fis = null;
		try {
			byte[] bytesArray = Files.readAllBytes(Paths.get(excelFilePath));
			fis = new FileInputStream(excelFilePath);
			fis.read(bytesArray); // read file into bytes[]

			return readAndValidateExcel(bytesArray, excelModelInstance);

		} catch (IOException e) {
			throw new SSException("File upload failed " + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public List<I> uploadExcel(MultipartFile uploadfile, I excelModelInstance) throws SSException {
		if (uploadfile.isEmpty()) {
			throw new SSException("Uploaded File cannot be empty");
		}

		try {
			byte[] file = extractFileAsBytes(Arrays.asList(uploadfile));
			return readAndValidateExcel(file, excelModelInstance);
		} catch (IOException e) {
			throw new SSException("File upload failed " + e.getMessage());
		}

	}

	private byte[] extractFileAsBytes(List<MultipartFile> files) throws IOException {
		for (MultipartFile file : files) {

			if (file.isEmpty()) {
				continue; // next pls
			}

			return file.getBytes();

		}
		return null;

	}

	public List<I> readAndValidateExcel(byte[] file, I excelModelInstance) throws SSException {
		Workbook workbook = null;
		List<I> rows = new ArrayList<I>();

		List<String> columnNames = getFieldNames(excelModelInstance);
		if (columnNames == null || columnNames.size() == 0) {
			throw new SSException("The uploaded excel file has no columns");
		}
		checkDuplicateColumnNames(columnNames);
		InputStream is = null;
		try {

			is = new ByteArrayInputStream(file);
			workbook = new XSSFWorkbook(is);
			Sheet datatypeSheet = workbook.getSheetAt(0);
			rows = processRows(datatypeSheet, excelModelInstance, columnNames);
			List<RowException<I>> rowExceptions = validateRows(rows);
			processValidationErrors(rowExceptions);

		} catch (Exception e) {
			throw new SSException("Unable to read excel file" + e.getMessage());
		} finally {
			cleanUp(workbook, is);
		}
		return rows;
	}

	private void processValidationErrors(List<RowException<I>> rowExceptions) throws SSException {
		if (rowExceptions != null && rowExceptions.size() > 0) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter();
			try {
				throw new SSException(
						"Excel Validation Failed " + mapper.writeValueAsString(rowExceptions));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				throw new SSException("Critical Failure " + e.getMessage());
			}

		}

	}

	private void checkDuplicateColumnNames(List<String> columnNames) throws SSException {
		Set<String> uniqueSet = new HashSet<String>(columnNames);
		for (String temp : uniqueSet) {
			if (Collections.frequency(columnNames, temp) > 1) {
				throw new SSException("The uploaded excel file has dupl columns");
			}
		}
	}

	private List<RowException<I>> validateRows(List<I> rows) throws SSException {
		List<RowException<I>> rowExceptions = new ArrayList<RowException<I>>();
		for (I excelModelInstance : rows) {
			RowException<I> rowException = validateRow(excelModelInstance);
			if (rowException != null) {
				rowExceptions.add(rowException);
			}
		}
		return rowExceptions;
	}

	private RowException<I> validateRow(I excelModelInstance) throws SSException {
		List<String> messages = ValidationUtils.validate(excelModelInstance);
		RowException<I> rowException = null;
		if (messages != null && messages.size() > 0) {
			rowException = new RowException<I>();
			rowException.setInputObject(excelModelInstance);
			rowException.setMessages(messages);

		}
		return rowException;

	}

	private List<String> getFieldNames(I instance) {
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : Arrays.asList(instance.getClass().getDeclaredFields())) {
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	private List<I> processRows(Sheet datatypeSheet, I instance, List<String> columnNames)
			throws SSException {
		Iterator<Row> iterator = datatypeSheet.iterator();
		List<I> rows = new ArrayList<I>();
		int i = 0;
		while (iterator.hasNext()) {

			Row currentRow = iterator.next();
			if (i == 0) {
				i++;
				;
				continue;
			}
			processRow(currentRow, instance, columnNames);
			I clonedInstance;
			try {
				clonedInstance = (I) instance.getClass().newInstance();

				BeanUtils.copyProperties(clonedInstance, instance);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new SSException("Unable to copy bean properties ....");
			}
			rows.add(clonedInstance);
		}
		return rows;
	}

	private void processRow(Row currentRow, I instance, List<String> columnNames) throws SSException {
		int i = 0;
	
		for (String colName : columnNames) {
			try {

				if (currentRow.getCell(i) != null) {
					String value = null;
					if (currentRow.getCell(i).getCellTypeEnum() == CellType.STRING) {
						value = currentRow.getCell(i).getStringCellValue();
					} else if (currentRow.getCell(i).getCellTypeEnum() == CellType.NUMERIC) {
						value = "" + currentRow.getCell(i).getNumericCellValue();
					}
					if (StringUtils.isNotEmpty(value)) {
						PropertyUtils.setSimpleProperty(instance, colName, value);
					}
				}
				i++;
			} catch (Exception e) {
				throw new SSException(
						" Bean property setting failed for column Name " + colName + ":" + e.getMessage());
			}
		}

	}

	private void cleanUp(Workbook workbook, InputStream is) {
		if (workbook != null) {
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
