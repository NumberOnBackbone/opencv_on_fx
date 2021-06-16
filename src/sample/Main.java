package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class Main extends Application {

    private static final int ROOT_SCENE_WIDTH = 800;
    private static final int ROOT_SCENE_HEIGHT = 600;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){


        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root, ROOT_SCENE_WIDTH, ROOT_SCENE_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());

            primaryStage.setTitle("OPEN_CV_APP");
            primaryStage.setScene(scene);
            primaryStage.show();

            Controller controller = loader.getController();
            primaryStage.setOnCloseRequest(windowEvent -> controller.setClosed());

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
