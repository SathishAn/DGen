package dgen;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EncriptionDialog {
	
	public void showAlertAndWait() {
		Stage window = new Stage();
		window.setTitle("Encrypt");
		window.setResizable(false);
	    window.initModality(Modality.APPLICATION_MODAL);
	    window.setMinHeight(200.0D);
	    window.setMinWidth(400.0D);
	    
	    Label actualLbl = new Label("Actual Text : ");
	    actualLbl.setWrapText(true);
	    actualLbl.setAlignment(Pos.TOP_LEFT);
	    
	    TextField actualText = new TextField();
	    actualText.setPromptText("Enter Actual Text");
	    actualText.setMinWidth(250.0D);
	    actualText.setAlignment(Pos.BASELINE_LEFT);
	    
	    Label encodedLbl = new Label("Encoded Text : ");
	    encodedLbl.setWrapText(true);
	    encodedLbl.setAlignment(Pos.BASELINE_LEFT);
	    
	    Label encodedText = new Label();
	    encodedText.setMinWidth(250.0D);
	    encodedText.setAlignment(Pos.BASELINE_LEFT);
	    
	    Button encodeBtn = new Button("Encrypt");
	    encodeBtn.setPrefHeight(30.0D);
	    encodeBtn.setPrefWidth(100.0D);
	    encodeBtn.setTextAlignment(TextAlignment.CENTER);
	    encodeBtn.setAlignment(Pos.CENTER);
	    
	    encodeBtn.setOnAction(e->{
	    	String actualTextValue = actualText.getText();
	    	if(actualTextValue == null || actualTextValue.trim().isEmpty()) {
	    		return;
	    	}
	    	 String encryptedTextValue = EncryptionFile.encrypt(actualTextValue.trim());
	    	 encodedText.setText(encryptedTextValue);
	    });
	    
	    Button copyBtn = new Button("Copy");
	    copyBtn.setPrefHeight(30.0D);
	    copyBtn.setPrefWidth(100.0D);
	    copyBtn.setTextAlignment(TextAlignment.CENTER);
	    copyBtn.setAlignment(Pos.CENTER);
	    
	    copyBtn.setOnAction(e -> {
	    	String encodedTextValue = encodedText.getText();
	          if (encodedTextValue == null || encodedTextValue.trim().isEmpty()) {
	            
	            return;
	          } 
	          StringSelection selection = new StringSelection("Encrypted :~" + encodedTextValue.trim());
	          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	          clipboard.setContents(selection, selection);
	          
	        });
	    
	    Button clearBtn = new Button("Clear");
	    clearBtn.setPrefHeight(30.0D);
	    clearBtn.setPrefWidth(100.0D);
	    clearBtn.setTextAlignment(TextAlignment.CENTER);
	    clearBtn.setAlignment(Pos.CENTER);
	    clearBtn.setOnAction(e -> {
	          actualText.clear();
	          encodedText.setText("");
	        });
	    
	   
	    
	    HBox hbox = new HBox();
	    hbox.setAlignment(Pos.CENTER);
	    hbox.getChildren().addAll(new Node[] { actualLbl, actualText });
	    hbox.setSpacing(20.0D);
	    
	    HBox hbox1 = new HBox();
	    hbox1.setAlignment(Pos.CENTER);
	    hbox1.getChildren().addAll(new Node[] { encodedLbl, encodedText });
	    hbox1.setSpacing(10.0D);
	    
	    HBox hbox2 = new HBox();
	    hbox2.setAlignment(Pos.CENTER);
	    hbox2.getChildren().addAll(new Node[] { encodeBtn, copyBtn, clearBtn });
	    hbox2.setSpacing(10.0D);
	    
	    VBox vBox = new VBox();
	    vBox.setAlignment(Pos.CENTER);
	    vBox.getChildren().addAll(new Node[] { hbox, new Label(), hbox1, new Label(), hbox2 });
	    
	    Scene scene = new Scene(vBox);
	    window.setScene(scene);
	    window.showAndWait();
	}
	
	

}
