package dgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import dgen.verification.console.TextAreaAppender;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

public class DgenController implements Initializable{
	@FXML private Label lblWelcome;
	
	@FXML private TableView<Modules> tableview;
	
	@FXML private TableColumn<Modules, String> module;
	
	@FXML private TableColumn<Modules, String> dCount;
	
	@FXML private TableColumn<Modules, Boolean> executionFlagCH;
	
	@FXML private TableColumn<Modules, Boolean> executionFlagUS;
	
	@FXML private TextArea logArea;
	
	@FXML private Button BtnRun, BtnStop,BtnReport, addNewRow, addNewCol, deleteRow , deleteColumn, saveTestData;
	
	@FXML private Tab helpTab, consoleTab;
	
	@FXML private TabPane tabPaneConsole;
	
	@FXML private WebView userGuide;
	
	
	
	@FXML private ScrollPane helpAreaPane;
	
	@FXML private TableView<ObservableList<String>> tableViewData;
	
	@FXML private TreeView<TestDataTreeStructure> treeViewTestData;
	
	private TreeItem<TestDataTreeStructure> root;
	
	@FXML private TableView<EnvironmentSetup> environmentTableView;
	
	@FXML private TableColumn<EnvironmentSetup, String> environmentKey;
	
	@FXML private TableColumn<EnvironmentSetup, String> environmentValue;
	
	@FXML private Button btnEnvAdd, btnEnvDelete, btnEnvSave, encrypt;
	
	@FXML private MenuItem userGuideMenu, AboutMenu;
	
	private WebEngine webEngine;
	
	private ObservableList<EnvironmentSetup> environList = FXCollections.observableArrayList();
	
	private ObservableList<Modules> moduleList = FXCollections.observableArrayList();
	
	private String fileName, sheetName;
	
	private ObservableList<String> columnList = null;
	
	private ObservableList<ObservableList<String>> data = null;
	
	public static Logger logger = Logger.getLogger(DgenController.class);
	
	private String propertyFilePath = "./configs/config.properties";
	
	private Properties properties;
	
	private String inputPath = "./resources/InputData";
	
	private String currWBPath ;
	
	
	
	private FileInputStream file;
	private Thread runThread;
	private ExecutionProcess exec;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lblWelcome.setText("Welcome " + System.getProperty("user.name"));
		try {
			tabPaneConsole.getSelectionModel().select(0);
			BufferedReader reader = new BufferedReader(new FileReader("./configs/Log4j.properties"));
			Properties loggingProperties = new Properties();
				loggingProperties.load(reader);
				PropertyConfigurator.configure( loggingProperties );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	        
		getModules();
		setToolBarIcon();
		initializeTreeViewData();
	    TextAreaAppender.setTextArea(this.logArea);
	    getAllEnvironment();
	    logger.info("DGen Launched...");
	}
	
	private void getModules() {
		
		this.module.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
		this.dCount.setCellValueFactory(new PropertyValueFactory<>("iCount"));
		this.executionFlagCH.setCellValueFactory(new PropertyValueFactory<>("iCHSelect"));
		this.executionFlagUS.setCellValueFactory(new PropertyValueFactory<>("iUSSelect"));
		moduleList = getModuleList();
		tableview.setItems(moduleList);
		tableview.setEditable(true);
		this.dCount.setCellFactory(TextFieldTableCell.forTableColumn());
		this.dCount.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setiCount(e.getNewValue());
		});
		this.tableview.setRowFactory(tr->{
			TableRow<Modules> row = new TableRow<>();
			row.setOnMouseClicked(event->{
				if(event.getClickCount() == 1 &&  !row.isEmpty()) {
					Modules item = row.getItem();
					DgenController.this.tabPaneConsole.getSelectionModel().select(1);
					System.out.println(item.getModuleName());
					try {
						FileInputStream input = new FileInputStream(new File("./userGuide/help/" + item.getModuleName() + ".png"));
						DgenController.this.helpAreaPane.setContent(new ImageView( new Image(input)));
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
					
				}
			});
			
			return row;
		});
		
			
	}
	
	private void initializeTreeViewData() {
		root = new TreeItem<TestDataTreeStructure>(new TestDataTreeStructure("DGen Data", "Root"));
		root.setExpanded(true);
		this.treeViewTestData.setCellFactory(CheckBoxTreeCell.<TestDataTreeStructure>forTreeView());
		ObservableList<TestDataTreeStructure> subFolder = TestDataEventHandling.getBaseRoot(inputPath);
		for(TestDataTreeStructure fld:subFolder) {
			TreeItem<TestDataTreeStructure> node = makeBranch(root, fld);
			ObservableList<TestDataTreeStructure> roots = TestDataEventHandling.getRoot(inputPath + "/"+ fld.getNode());
			for (TestDataTreeStructure rootName : roots) {
				if(rootName.getNode().startsWith("~$")) {
					
				}else {
					TreeItem<TestDataTreeStructure> subnode = makeBranch(node, rootName);
					 ObservableList<TestDataTreeStructure> items = TestDataEventHandling.getSheets(rootName, inputPath + "/"+ fld.getNode() +	 "/");
					 for (TestDataTreeStructure iter : items) {
						 makeBranch(subnode, iter); 
						 }
				}
			 
			  }
			 
			
		}
		
		
		
		
		
		this.treeViewTestData.setRoot(root);
		this.treeViewTestData.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2) {
					tableViewData.getItems().clear();
					tableViewData.getColumns().clear();
					TreeItem<TestDataTreeStructure> item = DgenController.this.treeViewTestData.getSelectionModel().getSelectedItem();
					System.out.println(item.getValue().toString());
					DgenController.this.sheetName = item.getValue().toString();
					TreeItem<TestDataTreeStructure> parentNode = item.getParent();
					String grandParent = parentNode.getParent().getValue().getNode();
					String parent = item.getParent().getValue().getNode();
					DgenController.this.currWBPath = DgenController.this.inputPath + "/" + grandParent + "/" ;
					DgenController.this.fileName = parent;
					System.out.println(parent);
					columnList = TestDataEventHandling.getTestDataColumn(DgenController.this.currWBPath, parent, item.getValue().getNode());
					System.out.println("The Size is " + columnList.size());
					for (int i = 0; i < columnList.size(); i++) {
						final int j = i;
						TableColumn<ObservableList<String>, String> col = new TableColumn<>(columnList.get(j));
						col.setCellValueFactory(
								new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
									public ObservableValue<String> call(
											CellDataFeatures<ObservableList<String>, String> param) {

										return new SimpleStringProperty(param.getValue().get(j).toString());
									}
								});
						col.setCellFactory(TextFieldTableCell.forTableColumn());
						col.setOnEditCommit(e -> {
							System.out.println(e.getTablePosition().getRow());
							System.out.println(j);
							e.getTableView().getItems().get(e.getTablePosition().getRow()).set(j, e.getNewValue());
							System.out.println(
									columnList.get(j) + e.getNewValue() + " - " + e.getTablePosition().getRow());
						});
						col.prefWidthProperty().bind(DgenController.this.tableViewData.widthProperty().multiply(0.15));
						DgenController.this.tableViewData.getColumns().add(col);
					}
					DgenController.this.data = TestDataEventHandling.getTestDataItems(DgenController.this.currWBPath, parent,
							item.getValue().getNode());
					DgenController.this.tableViewData.setEditable(true);
					DgenController.this.tableViewData.setItems(DgenController.this.data);
				}
			}
		});
	}
	
	private TreeItem<TestDataTreeStructure> makeBranch(TreeItem<TestDataTreeStructure> root,
			TestDataTreeStructure child) {
		Node iconImage;
		if (child.getNodeType().equalsIgnoreCase("sheet")) {

			iconImage = new ImageView(new Image(getClass().getResourceAsStream("resources/Icons/" + "sheets.png")));
		} else if(child.getNodeType().equalsIgnoreCase("workbook")){
			iconImage = new ImageView(new Image(getClass().getResourceAsStream("resources/Icons/" + "excel.png")));
		}else {
			iconImage = new ImageView(new Image(getClass().getResourceAsStream("resources/Icons/" + "folderIcon.png")));
		}

		TreeItem<TestDataTreeStructure> childNode = new TreeItem<TestDataTreeStructure>(child,
				iconImage);
		
		root.getChildren().add(childNode);
		return childNode;

	}
	
	
	private void setToolBarIcon() {
		this.setButtonGraphics(this.BtnRun, "execution");
		this.setButtonGraphics(this.BtnStop, "stop");
		this.setButtonGraphics(this.BtnReport, "viewReports");
		this.setButtonGraphics(this.btnEnvAdd, "addNew");
		this.setButtonGraphics(this.btnEnvDelete, "Delete");
		this.setButtonGraphics(this.deleteColumn, "columnRemove");
		this.setButtonGraphics(this.btnEnvSave, "save");
		this.setButtonGraphics(this.encrypt, "encrypt");
		this.setButtonGraphics(this.addNewRow, "addNew");
		this.setButtonGraphics(this.deleteRow, "Delete");
		this.setButtonGraphics(this.addNewCol, "InsertRow");
		this.setButtonGraphics(this.saveTestData, "save");
		
	}

	private void setButtonGraphics(Button TempButton, String image) {
		Image imgAdd = new Image(getClass().getResourceAsStream("resources/Icons/" + image +".png"));
		Button newButton = TempButton;
		newButton.setGraphic(new ImageView(imgAdd));
	}
	
	private ObservableList<Modules> getModuleList() {
		ObservableList<Modules> list = FXCollections.observableArrayList();
		File files = new File("./resources/DGenInput.xlsx");
		try {
			Fillo fillo=new Fillo();
			Connection connection;
			connection = fillo.getConnection( "./resources/DGenInput.xlsx");
			String sql = "SELECT * FROM Dashboard";
			Recordset  rs= connection.executeQuery(sql);
			while(rs.next()) {
				list.add(new Modules(rs.getField("TestDataScript"), rs.getField("DataType"),  "", rs.getField("CH"), rs.getField("US") , rs.getField("BNK")));
			}
			
			for(String fieldName: rs.getFieldNames()) {
				
			}
			rs.close();
			connection.close();
			
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		/*File folderCH = new File("./resources/InputData/CH");
		File[] listOfFiles = folderCH.listFiles();
		for(File fl:listOfFiles) {
			File folderUS = new File("./resources/InputData/US/"+ fl.getName());
			if(folderUS.exists()) {
				list.add(new Modules(fl.getName().substring(0, fl.getName().indexOf(".xlsx")), "", true, true));
			}else {
				list.add(new Modules(fl.getName().substring(0, fl.getName().indexOf(".xlsx")), "", true, false));
			}
		}*/
		return list;
	}
	
	@FXML
	private void Run() {
		this.tabPaneConsole.getSelectionModel().select(this.consoleTab);
		this.logArea.clear();
		//this.exec = new ExecutionProcess(moduleList);
		this.runThread = new Thread(exec);
		logger.info("Initiated the Execution");
		runThread.start();
		
	}
	
	@FXML
	private void stop() {
		if(this.runThread != null) {
			logger.info("The Process has been stopped");
			this.runThread.stop();
			
		}
		
	}
	
	
	private void getAllEnvironment() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(propertyFilePath));
			properties = new Properties();
			properties.load(reader);
			reader.close();
			for(Entry<Object, Object> prop:properties.entrySet()) {
				environList.add(new EnvironmentSetup(prop.getKey().toString(), prop.getValue().toString()));
			}
			this.environmentKey.setCellValueFactory(new PropertyValueFactory("environKey"));
			this.environmentValue.setCellValueFactory(new PropertyValueFactory("environValue"));
			this.environmentValue.setCellFactory(TextFieldTableCell.forTableColumn());
			this.environmentValue.setOnEditCommit(e -> {
				e.getTableView().getItems().get(e.getTablePosition().getRow()).setEnvironValue(e.getNewValue());
			});
			environmentTableView.setEditable(true);
			environmentTableView.setItems(environList);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@FXML
	private void addNewProperties() {
		this.environList.add(new EnvironmentSetup("", ""));
	}
	
	@FXML
	private void removeProperties() {
		environList.remove(this.environmentTableView.getSelectionModel().getSelectedItem());
	}
	 
	@FXML 
	private void saveProperties() {
		try {
			properties.clear();
			for(EnvironmentSetup environ:environList) {
				properties.setProperty(environ.getEnvironKey(), environ.getEnvironValue());
			}
			properties.store(new FileOutputStream(new File(propertyFilePath) ), null);
			logger.info("Environment setup saved successfully...");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	@FXML
	private void addNewRow() {
		ObservableList<String> row = FXCollections.observableArrayList();
		for (int i = 0; i < columnList.size(); i++) {
			row.add("");
		}
		this.data.add(row);
	}
	
	@FXML
	private void deleteColumn() {
		SelectionDialog Sd = new SelectionDialog();
		String sFileName = Sd.showAlertAndWait(columnList);
		
		ObservableList<ObservableList<String>> dataMapping = FXCollections.observableArrayList();
		for(int i = 0 ; i < columnList.size(); i ++) {
			if(sFileName.equals(columnList.get(i))) {
				this.tableViewData.getColumns().remove(i);
				ObservableList<ObservableList<String>> tableData = this.tableViewData.getItems();
				for(ObservableList<String> rowData:tableData) {
					dataMapping.add(rowData);
				}
				for(ObservableList<String> rowData:dataMapping) {
					rowData.remove(i);
				}
				
				columnList.remove(sFileName);
				break;
				
			}
		}
		this.data.setAll(dataMapping);
		this.tableViewData.getColumns().clear();
		for (int i = 0; i < columnList.size(); i++) {
			final int j = i;
			TableColumn<ObservableList<String>, String> col = new TableColumn<>(columnList.get(j));
			col.setCellValueFactory(
					new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(
								CellDataFeatures<ObservableList<String>, String> param) {

							return new SimpleStringProperty(param.getValue().get(j).toString());
						}
					});
			col.setCellFactory(TextFieldTableCell.forTableColumn());
			col.setOnEditCommit(e -> {
				
				e.getTableView().getItems().get(e.getTablePosition().getRow()).set(j, e.getNewValue());
				System.out.println(
						columnList.get(j) + e.getNewValue() + " - " + e.getTablePosition().getRow());
			});
			this.tableViewData.getColumns().add(col);
			
		}
		
		
		
		
		
		
	}

	
	@FXML
	private void addNewColumn() {
		String sFileName = JOptionPane.showInputDialog("Please input column Name");
		columnList.add(sFileName);
		final int colCount = columnList.size()-1;
		TableColumn<ObservableList<String>, String> col = new TableColumn<>(sFileName);
		col.setCellValueFactory(celldata-> new ReadOnlyStringWrapper(""));
		this.tableViewData.getColumns().add(col);	
		col.setCellFactory(TextFieldTableCell.forTableColumn());
		System.out.println("The columne count is " + colCount);
		col.setOnEditCommit(e -> {
			int rowNum = e.getTablePosition().getRow();
			((ObservableList<String>)e.getTableView().getItems().get(rowNum)).set(colCount, e.getNewValue());
		});
		col.prefWidthProperty().bind(tableViewData.widthProperty().multiply(0.15));
		for(ObservableList<String>d:data) {
			d.add("");
		}
	}
	
	@FXML
	private void deleteRow() {
		int index = this.tableViewData.getSelectionModel().getSelectedIndex();
		this.data.remove(index);
	}
	
	@FXML
	private void saveFile() {
		Row row;
		try {
			this.file = new FileInputStream(this.currWBPath + this.fileName);
			Workbook workbook = WorkbookFactory.create(this.file);
			Sheet sheet = workbook.getSheet(this.sheetName);
			row = sheet.getRow(0);
			if (row != null) {
				for (int i = 0; i < columnList.size(); i++) {
					Cell cell = row.getCell(i);
					if(cell == null) {
						cell = row.createCell(i);
					}
					cell.setCellValue(columnList.get(i));
				}
			}
			int colNum = row.getLastCellNum();
			for(int i = this.columnList.size(); i<colNum; i++) {
				Cell cell = row.getCell(i);
				row.removeCell(cell);
			}
			
			int rowNum = sheet.getLastRowNum();
			for(int i = this.data.size()+1 ; i <= rowNum; i++) {
				row = sheet.getRow(i);
					sheet.removeRow(row);
			}
			rowNum = 1;
			for (ObservableList<String> rowItem : this.data) {
				 row = sheet.getRow(rowNum);
				if (row == null) {
					row = sheet.createRow(rowNum);
				}
				colNum = 0;
				for (String val : rowItem) {
					Cell cell = row.getCell(colNum);
					if (cell == null) {
						cell = row.createCell(colNum);
					}
					cell.setCellValue(val);
					colNum++;
				}
				
				int colLastNum = row.getLastCellNum();
				for(int i = this.columnList.size()+1; i< colLastNum; i++) {
					Cell cell = row.getCell(i);
					if(cell != null) {
						row.removeCell(cell);
					}
					
				}
				
				rowNum++;
			}
			FileOutputStream fos = new FileOutputStream(this.currWBPath + this.fileName);
			workbook.write(fos);
			fos.flush();
			fos.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@FXML
	private void loadReports() {
		
		this.exec.loadReportPath();
	}
	
	
	@FXML
	private void encryptString() {
		EncriptionDialog ED = new EncriptionDialog();
		ED.showAlertAndWait();
	}
	
	private void loadUserGuide() {
		webEngine = userGuide.getEngine();
		System.out.println(getClass().getResource("userGuide/About.html"));
		webEngine.load(getClass().getResource("userGuide/About.html").toString());
	}
	
	@FXML
	private void loadUserGuideDocument() {
		Process p;
		try {
			System.out.println(System.getProperty("user.dir"));
			String path = System.getProperty("user.dir") + "\\userGuide\\DGen - User Guide.docx";
			
			
			p = Runtime.getRuntime()
			        .exec("rundll32 url.dll,FileProtocolHandler " + path);
			p.waitFor();
			logger.info("User Guide Loaded Successfully");
		} catch (IOException e) {
			logger.error("Unable to load User Guide");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@FXML
	private void loadAbout() {
		About ab = new About();
		ab.showAlert();
	}

}
