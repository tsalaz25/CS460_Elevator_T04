package CommandCenter;

import javafx.application.Application;

import javafx.stage.Stage;

public class ObjetTesting extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        ElevatorCommandCenterDisplay center = new ElevatorCommandCenterDisplay(primaryStage);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
