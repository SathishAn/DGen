package dgen;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Alerts {
	private String title;
	
	public Alerts() { this.title = "Information Dialog"; }
	
	public Alerts(String title) {
		if (title != null && !title.trim().isEmpty()) {
		      this.title = title.trim();
		    } else {
		      this.title = "Information Dialog";
		    } 
	}
	
	
	public void showAlert(String message) {
		Stage window = new Stage();
		window.setTitle(this.title);
		window.setResizable(false);
	    window.initModality(Modality.APPLICATION_MODAL);
	    window.setMinHeight(150.0D);
	    window.setMinWidth(250.0D);
	    
	    Label msg = new Label(message);
	    msg.setLineSpacing(9.0D);
	    msg.setWrapText(true);
	    msg.setAlignment(Pos.TOP_LEFT);
	    
	    Button okButton = new Button("Ok");
	    okButton.setPrefHeight(30.0D);
	    okButton.setPrefWidth(80.0D);
	    okButton.setTextAlignment(TextAlignment.CENTER);
	    okButton.setAlignment(Pos.CENTER);
	    
	    okButton.setOnAction(e->{
	    	window.close();
	    });
	    
	  
	    
	    
	    HBox hbox2 = new HBox();
	    hbox2.setAlignment(Pos.BOTTOM_CENTER);
	    hbox2.getChildren().addAll(new Node[] { okButton });
	    hbox2.setSpacing(10.0D);
	    
	    VBox vBox = new VBox();
	    vBox.setAlignment(Pos.CENTER);
	    vBox.setSpacing(30.0D);
	    vBox.getChildren().addAll(new Node[] { msg, hbox2 });
	    
	    Scene scene = new Scene(vBox);
	    window.setScene(scene);
	    window.showAndWait();
	}

}
