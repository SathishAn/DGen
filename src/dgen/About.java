package dgen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class About {
	
	public void showAlert() {
		Stage window = new Stage();
		window.setTitle("About");
		window.setResizable(false);
	    window.initModality(Modality.APPLICATION_MODAL);
	    window.setMinHeight(150.0D);
	    window.setMinWidth(250.0D);
	    
	    FileInputStream input;
		
			
			Image image = new Image(About.class.getResourceAsStream("resources/Icons/About.png"));
		    ImageView imageView = new ImageView(image);
		    HBox hbox = new HBox(imageView);
		    Scene scene = new Scene(hbox);
		    window.getIcons().add(new Image(About.class.getResourceAsStream("resources/Icons/DGen.png")));
		    window.setScene(scene);
		    window.showAndWait();
		
		
		
	 /*   
	    
	    Label about = new Label("DGen");
	    about.setLineSpacing(9.0D);
	    about.setWrapText(true);
	    about.setFont(new Font("Verdana", 30));
	    about.setAlignment(Pos.TOP_CENTER);
	    
	    Label versionLabel = new Label("Version : ");
	    versionLabel.setLineSpacing(9.0D);
	    versionLabel.setWrapText(true);
	    versionLabel.setAlignment(Pos.CENTER_LEFT);
	    
	    Label versionValue = new Label("DGen.V1.0");
	    versionValue.setLineSpacing(9.0D);
	    versionValue.setWrapText(true);
	    versionValue.setAlignment(Pos.TOP_LEFT);
	    
	    
	    Label authLabel = new Label("Author : ");
	    authLabel.setLineSpacing(9.0D);
	    authLabel.setWrapText(true);
	    authLabel.setAlignment(Pos.TOP_LEFT);
	    
	    Label authValue = new Label("TechM - Core Banking");
	    authValue.setLineSpacing(9.0D);
	    authValue.setWrapText(true);
	    authValue.setAlignment(Pos.TOP_LEFT);
	    
	    
	  
	    
	    
	    Button okButton = new Button("Ok");
	    okButton.setPrefHeight(30.0D);
	    okButton.setPrefWidth(80.0D);
	    okButton.setTextAlignment(TextAlignment.CENTER);
	    okButton.setAlignment(Pos.CENTER);
	    
	    okButton.setOnAction(e->{
	    	window.close();
	    });
	    
	    
	    HBox hbox1 = new HBox();
	    hbox1.setAlignment(Pos.CENTER_LEFT);
	    hbox1.getChildren().addAll(new Node[] { versionLabel, versionValue });
	    hbox1.setSpacing(10.0D);
	    

	    HBox hbox2 = new HBox();
	    hbox2.setAlignment(Pos.TOP_LEFT);
	    hbox2.getChildren().addAll(new Node[] { authLabel, authValue });
	    hbox2.setSpacing(10.0D);
	    
	    
	    
	    HBox hbox3 = new HBox();
	    hbox3.setAlignment(Pos.BOTTOM_CENTER);
	    hbox3.getChildren().addAll(new Node[] { okButton });
	    hbox3.setSpacing(10.0D);
	    
	    VBox vBox = new VBox();
	    vBox.setAlignment(Pos.CENTER);
	    vBox.setSpacing(15.0D);
	    vBox.getChildren().addAll(new Node[] {about, hbox1, hbox2, hbox3 });*/
	    
	    
	}

}
