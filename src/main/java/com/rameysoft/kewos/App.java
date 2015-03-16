package com.rameysoft.kewos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * TextSearch Application BootStrap
 * @author M. Mubasher Usman
 * @version ${project.version}
 *
 */
public class App extends Application{

    public static void main( String[] args ){
  	    launch(args);
    }

	

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(App.class.getResource("/fxml/main.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("tiwulfx.css");//load tiwulfx.css
        //scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("KEWOS Engine (KeyWords Search Engine)");
        stage.getIcons().add(new Image("/images/search.png"));
	    
        stage.setScene(scene);
        stage.setResizable(false);
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
          @Override
          public void handle(WindowEvent we) {
              we.consume();
              
              Platform.exit();
          }
        });  
        stage.show();
		
	}
}
