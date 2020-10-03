package dgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import java.util.Map.Entry;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class DgenExe extends Application{
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
		@Override
	public void start(Stage primaryStage){
		// TODO Auto-generated method stub
			try {
			String currentDir = System.getProperty("user.dir").replace("\\" , "/");
			String srcLocation = currentDir + "/src";
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(DgenExe.class.getResource("Dgen.fxml"));
			Parent root;
			root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.setMaximized(false); 
			primaryStage.setScene(scene);
			primaryStage.setTitle("DGen - The Data Generator");
			primaryStage.setMaximized(true);
			primaryStage.getIcons().add(new Image(DgenExe.class.getResourceAsStream("resources/Icons/DGen.png")));
			primaryStage.show();
			checkForLicense();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "test",0);
		}
		
		
	}	
		
		private void checkForLicense() {
			Date date = new Date();
			Date endDate = new Date(120, 7, 31);
			DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			System.out.println(endDate);
			double diff = (endDate.getTime() - date.getTime()) / 8.64E7D;
		    System.out.println(diff);
		    if (diff < 15.0D && diff >= 0.0D) {
		    	(new Alerts("License")).showAlert("License Expires on " + format.format(endDate));
		    }
		    if (diff < 0.0D) {
		    	(new Alerts("License")).showAlert("License Expired");
		    	System.exit(-1);
		    } 
		}
	

}
