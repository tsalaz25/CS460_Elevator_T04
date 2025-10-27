package LobbyGUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DemoLobbyApp extends Application {
    @Override
    public void start(Stage stage) {
        LobbyPanel panel = new LobbyPanel();

        Scene scene = new Scene(panel, 360, 220);
        stage.setTitle("Demo Lobby Panel");
        stage.setScene(scene);
        stage.setMinWidth(360);
        stage.setMinHeight(240);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
