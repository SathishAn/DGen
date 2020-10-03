package dgen;

import com.sun.glass.events.MouseEvent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Modules {
	private final SimpleStringProperty moduleName;
	private final SimpleStringProperty iDataType;
	private final SimpleStringProperty iCount;
	private CheckBox iCHSelect;
	private CheckBox iUSSelect;
	private CheckBox iBNKSelect;
	
	public Modules(String moduleName, String dataType ,String count, String chStatus, String usStatus, String bnkStatus){
		this.moduleName = new SimpleStringProperty(moduleName);
		this.iDataType = new SimpleStringProperty(dataType);
		this.iCount = new SimpleStringProperty(count);
		this.iUSSelect = new CheckBox();
		this.iCHSelect = new CheckBox();
		this.iBNKSelect = new CheckBox();
		if(chStatus.equalsIgnoreCase("NO")) {
			iCHSelect.setDisable(true);
		}
		if(usStatus.equalsIgnoreCase("NO")) {
			iUSSelect.setDisable(true);
		}
		if(bnkStatus.equalsIgnoreCase("NO")) {
			iBNKSelect.setDisable(true);
		}
		
	}
	
	public String getModuleName() {
		return moduleName.get();
	}
	
	
	public void setModuleName(String moduleName) {
		this.moduleName.set(moduleName);
	}
	
	public String getIDataType() {
		return iDataType.get();
	}
	
	
	public void setIDataType(String dataType) {
		this.iDataType.set(dataType);
	}
	
	public String getICount() {
		return iCount.get();
	}
	
	public void setiCount(String iCount) {
		this.iCount.set(iCount);
	}
	
	public CheckBox getICHSelect() {
		return iCHSelect;
	}
	
	public void setICHSelect(CheckBox select) {
		this.iCHSelect=select;
	}
	
	public CheckBox getIUSSelect() {
		return iUSSelect;
	}
	
	public void setIUSSelect(CheckBox select) {
		this.iUSSelect=select;
	}
	
		
}
