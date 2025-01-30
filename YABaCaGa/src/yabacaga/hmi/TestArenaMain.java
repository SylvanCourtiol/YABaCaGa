package yabacaga.hmi;
	
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

/**
 * Client start point of the GUI.
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class TestArenaMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/yabacaga/hmi/arena.fxml"));
			//loader.setController(new EditorController());
			BorderPane root = (BorderPane)loader.load();
			Scene scene = new Scene(root,1083,1078);
			scene.getStylesheets().add(getClass().getResource("/yabacaga/hmi/application.css").toExternalForm());
			primaryStage.setTitle("YABaCaGa");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
