package CabinGUI;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DemoCabinApp extends Application {
    @Override public void start(Stage stage) {
        CabinPanel panel = new CabinPanel();

        Button open   = new Button("Open Doors");
        Button close  = new Button("Close Doors");
        Button togOver= new Button("Toggle Overload");
        Button togObs = new Button("Toggle Obstruction");
        CheckBox auto = new CheckBox("Auto-close (3s)");

        open.setOnAction(e -> {
            panel.setDoorState(DoorState.OPEN);
            if (auto.isSelected()) {
                PauseTransition pt = new PauseTransition(Duration.seconds(3));
                pt.setOnFinished(ev -> panel.setDoorState(DoorState.CLOSED));
                pt.play();
            }
        });
        close.setOnAction(e -> panel.setDoorState(DoorState.CLOSED));
        togOver.setOnAction(e -> panel.setOverloaded(!panel.overloaded()));
        togObs.setOnAction(e -> panel.setObstructed(!panel.obstructed()));

        HBox controls = new HBox(10, open, close, new Separator(), togOver, togObs, new Separator(), auto);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(12, panel, controls);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root);
        stage.setTitle("Cabin GUI — Team 04 (Dropdown + Green/Red Doors + Images)");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setMinWidth(600);
        stage.setMinHeight(610);
        stage.setWidth(640);
        stage.setHeight(630);

        stage.show();
    }
    public static void main(String[] args) { launch(); }
}