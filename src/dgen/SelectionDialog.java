package dgen;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SelectionDialog {
	private String selectionText = null; 
	
	public String showAlertAndWait(ObservableList<String> columnList) {
		
		Stage window = new Stage();
		window.setTitle("Encrypt");
		window.setResizable(false);
	    window.initModality(Modality.APPLICATION_MODAL);
	    window.setMinHeight(200.0D);
	    window.setMinWidth(400.0D);
	    
	    Label actualLbl = new Label("Select Column : ");
	    actualLbl.setWrapText(true);
	    actualLbl.setAlignment(Pos.TOP_LEFT);
	    
	    ComboBox<String> actualText = new ComboBox<>();
	    actualText.setPromptText("Select Actual Column");
	    actualText.setMinWidth(250.0D);
	    actualText.setItems(columnList);
	    
	    Button removeButton = new Button("Remove");
	    removeButton.setPrefHeight(30.0D);
	    removeButton.setPrefWidth(100.0D);
	    removeButton.setTextAlignment(TextAlignment.CENTER);
	    removeButton.setAlignment(Pos.CENTER);
	    
	    removeButton.setOnAction(e->{
	    	String actualTextValue = actualText.getSelectionModel().getSelectedItem();
	    	if(actualTextValue == null || actualTextValue.trim().isEmpty()) {
	    		return;
	    	}
	    	SelectionDialog.this. selectionText = actualTextValue;
	    	window.close();
	    	
	    });
	    
	        
	    Button clearBtn = new Button("Clear");
	    clearBtn.setPrefHeight(30.0D);
	    clearBtn.setPrefWidth(100.0D);
	    clearBtn.setTextAlignment(TextAlignment.CENTER);
	    clearBtn.setAlignment(Pos.CENTER);
	    clearBtn.setOnAction(e -> {
	          actualText.getSelectionModel().clearSelection();
	        }); 
	    HBox hbox = new HBox();
	    hbox.setAlignment(Pos.CENTER);
	    hbox.getChildren().addAll(new Node[] { actualLbl, actualText });
	    hbox.setSpacing(20.0D);
	    
	    HBox hbox2 = new HBox();
	    hbox2.setAlignment(Pos.CENTER);
	    hbox2.getChildren().addAll(new Node[] { removeButton, clearBtn });
	    hbox2.setSpacing(10.0D);
	    
	    VBox vBox = new VBox();
	    vBox.setAlignment(Pos.CENTER);
	    vBox.getChildren().addAll(new Node[] { hbox, new Label(), new Label(), hbox2 });
	    
	    Scene scene = new Scene(vBox);
	    window.setScene(scene);
	    window.showAndWait();
	    
	    return selectionText;
	}
	
	

}
