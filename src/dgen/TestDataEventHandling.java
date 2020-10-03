package dgen;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;


public class TestDataEventHandling {
	
	
	public static ObservableList<TestDataTreeStructure> getBaseRoot(String path){
		ObservableList<TestDataTreeStructure> rootItems = FXCollections.observableArrayList();
		File folder = new File(path);
		File[] files = folder.listFiles();
		for(File fl:files) {
			rootItems.add(new TestDataTreeStructure(fl.getName(), "SubFolder"));
		}
		
		return rootItems;
		
		
	}
	
	public static ObservableList<TestDataTreeStructure> getRoot(String path) {
		ObservableList<TestDataTreeStructure> rootItems = FXCollections.observableArrayList();
		File folder = new File(path);
		FileFilter fileFilter = new WildcardFileFilter("*.xlsx");
		File[] files = folder.listFiles(fileFilter);
		/*if (files.length > 0) {
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		}*/
		for(File fl:files) {
			rootItems.add(new TestDataTreeStructure(fl.getName(), "workbook"));
		}
		return rootItems;
	}
	
	public static ObservableList<TestDataTreeStructure> getSheets(TestDataTreeStructure rootName, String path) {
		
		ObservableList<TestDataTreeStructure> sheetItems = FXCollections.observableArrayList();
		File files = new File(path + rootName);
		try {
			FileInputStream ExcelFile = new FileInputStream(files);
			XSSFWorkbook workbook = new XSSFWorkbook(ExcelFile);
			int sheetCount = workbook.getNumberOfSheets();
			for(int i =0 ; i < sheetCount; i++) {
				sheetItems.add(new TestDataTreeStructure(workbook.getSheetName(i), "Sheet"));
			}
			workbook.close();
			ExcelFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return sheetItems;
		
	}
	
	
	public static  ObservableList<String > getTestDataColumn(String path,String workbookName, String sheetname) {
		ObservableList<String > columnItems = FXCollections.observableArrayList();
		File files = new File("./resources/datatable/" + workbookName);
		try {
			 Fillo fillo=new Fillo();
			 Connection connection;
			connection = fillo.getConnection( path + workbookName);
			String sql = "SELECT * FROM " +  sheetname ;
			Recordset  rs= connection.executeQuery(sql);
			for(String fieldName: rs.getFieldNames()) {
				columnItems.add(fieldName);
			}
			rs.close();
			connection.close();
			
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return columnItems;	
		
	}
	
	public static  ObservableList<ObservableList<String>> getTestDataItems(String path, String workbookName, String sheetname) {
		ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
		File files = new File("./resources/datatable/" + workbookName);
		try {
			Fillo fillo=new Fillo();
			Connection connection;
			connection = fillo.getConnection( path + workbookName);
			String sql = "SELECT * FROM " +  sheetname ;
			Recordset  rs= connection.executeQuery(sql);
			while(rs.next()) {
				ObservableList<String> row = FXCollections.observableArrayList();
				for(String fieldName: rs.getFieldNames()) {
					row.add(rs.getField(fieldName));
					
				}
				data.add(row);
			}
			
			rs.close();
			connection.close();
			
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;	
		
	}
	

}
