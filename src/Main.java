import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import LobbyGUI.LobbyPanel;
import CabinGUI.CabinPanel;
import Wiring.EventBus;
import Wiring.InMemoryEventBus;
import Wiring.Topics;
import Control.ElevatorController;
import Sim.MockSim;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // --- Bus ---
        EventBus bus = new InMemoryEventBus();

        // --- UI Panels ---
        LobbyPanel lobby = new LobbyPanel();
        CabinPanel cabin = new CabinPanel();

        // In system mode the lobby does NOT self-move; controller + sim drive it
        lobby.setSystemMode(true);

        // --- Wire UI -> Bus (publish UI events) ---
        lobby.setOnUpPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_UP, lobby.getTargetFloor())
        );

        lobby.setOnDownPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_DOWN, lobby.getTargetFloor())
        );

        cabin.setOnFloorSelected(floor ->
                bus.publish(Topics.UI_CABIN_SELECT, floor)
        );

        // --- Core Logic + Simulation ---
        ElevatorController controller = new ElevatorController(bus, lobby, cabin);
        MockSim sim = new MockSim(bus);

        // --- Layout ---
        HBox root = new HBox(20, lobby, cabin);
        root.setPadding(new Insets(16));

        Scene scene = new Scene(root, 800, 400);
        stage.setTitle("Elevator System Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}