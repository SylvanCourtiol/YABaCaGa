package yabacaga.hmi;

import java.util.Arrays;
import client.Client;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

/**
 * Client start point of the GUI.
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class ClientMain extends Application {
	
	private static String NAME = null;
	@Override
	public void start(Stage primaryStage) {
		try {
			Client.getClient(new String[]{"8080", ClientMain.NAME}, primaryStage);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/yabacaga/hmi/editor.fxml"));
			//loader.setController(new EditorController());
			BorderPane root = (BorderPane)loader.load();
			Scene scene = new Scene(root,1100,680);
			scene.getStylesheets().add(getClass().getResource("/yabacaga/hmi/application.css").toExternalForm());
			primaryStage.setTitle("YABaCaGa");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setOnCloseRequest(e -> {
				if (e.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
					Client.getClient().getAgent().stop();
					System.exit(0);
				}
			});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ClientMain.NAME = args[0];
		String[] arguments = Arrays.copyOfRange(args, 1, args.length);
		launch(arguments);
	}
}
