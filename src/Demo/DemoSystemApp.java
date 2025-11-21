package Demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import Wiring.EventBus;
import Wiring.InMemoryEventBus;
import Wiring.Topics;
import Sim.MockSim;
import Control.ElevatorController;

import LobbyGUI.LobbyPanel;
import CabinGUI.CabinPanel;

/**
 * System demo:
 *  - Uses InMemoryEventBus + MockSim
 *  - Shows Lobby + Cabin panels
 *  - Wires UI events into the controller via the bus
 *
 * TODO:
 *  - Swap InMemoryEventBus with SoftwareBus adapter once available.
 *  - Replace MockSim with MotionSim.Simulator when ready.
 */
public class DemoSystemApp extends Application {

    private EventBus bus;
    private LobbyPanel lobbyPanel;
    private CabinPanel cabinPanel;
    private ElevatorController controller;
    private MockSim simulator;

    @Override
    public void start(Stage stage) {
        bootstrapCoreComponents();
        wireUiCallbacks();
        bootstrapDomainComponents();
        System.out.println("[DemoSystemApp] Booted with InMemoryEventBus + MockSim");

        Scene scene = new Scene(buildLayout(), 900, 420);
        stage.setTitle("Elevator Control Demo");
        stage.setScene(scene);
        stage.show();
    }

    private void bootstrapCoreComponents() {
        bus = new InMemoryEventBus();                 // swap with real bus adapter later
        lobbyPanel = new LobbyPanel();
        lobbyPanel.setSystemMode(true);               // disable self-movement; rely on controller
        cabinPanel = new CabinPanel();
    }

    private void wireUiCallbacks() {
        lobbyPanel.setOnUpPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_UP, lobbyPanel.getTargetFloor()));

        lobbyPanel.setOnDownPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_DOWN, lobbyPanel.getTargetFloor()));

        cabinPanel.setOnFloorSelected(f ->
                bus.publish(Topics.UI_CABIN_SELECT, f));

        // Fire alarm toggle → publish UI_FIRE_TOGGLED with current fireActive
        lobbyPanel.setOnFireToggled(() ->
                bus.publish(Topics.UI_FIRE_TOGGLED, lobbyPanel.isFireActive()));
    }

    private void bootstrapDomainComponents() {
        controller = new ElevatorController(bus, lobbyPanel, cabinPanel);
        simulator = new MockSim(bus); // replace with MotionSim.Simulator later
    }

    private HBox buildLayout() {
        HBox root = new HBox(32, lobbyPanel, cabinPanel);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color:linear-gradient(to bottom,#f8fafc,#e2e8f0);");
        return root;
    }

    public static void main(String[] args) {
        launch();
    }
}
