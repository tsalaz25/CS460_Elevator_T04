package Demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import Wiring.EventBus;
import Wiring.InMemoryEventBus;
import Wiring.Topics;
import Control.ElevatorController;

import LobbyGUI.LobbyPanel;
import CabinGUI.CabinPanel;
import CommandCenterGUI.CommandCenterPanel;
import Sim.MockSim;   // <<-- restore this

/**
 * System demo:
 *  - Uses InMemoryEventBus + MockSim
 *  - Shows Lobby + Cabin panels
 *  - Adds a separate Command Center window
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
    private CommandCenterPanel commandCenterPanel;

    private ElevatorController controller;
    private MockSim simulator;   // <<-- restore field

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

        showCommandCenterWindow();
    }

    private void bootstrapCoreComponents() {
        bus = new InMemoryEventBus();                 // swap with real bus adapter later
        lobbyPanel = new LobbyPanel();
        lobbyPanel.setSystemMode(true);               // disable self-movement; rely on controller
        cabinPanel = new CabinPanel();
        commandCenterPanel = new CommandCenterPanel();
    }

    private void wireUiCallbacks() {
        // Lobby UI → bus
        lobbyPanel.setOnUpPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_UP, lobbyPanel.getTargetFloor()));

        lobbyPanel.setOnDownPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_DOWN, lobbyPanel.getTargetFloor()));

        // Cabin UI → bus
        cabinPanel.setOnFloorSelected(f ->
                bus.publish(Topics.UI_CABIN_SELECT, f));

        // Fire alarm toggle from Lobby → bus
        lobbyPanel.setOnFireToggled(() ->
                bus.publish(Topics.UI_FIRE_TOGGLED, lobbyPanel.isFireActive()));

        // Command center fire toggle → bus
        commandCenterPanel.setOnFireToggled(active ->
                bus.publish(Topics.UI_FIRE_TOGGLED, active));
    }

    private void bootstrapDomainComponents() {
        controller = new ElevatorController(bus, lobbyPanel, cabinPanel, commandCenterPanel);

        // Restore simulator so CTRL_CMD_MOVE_TO actually produces floor ticks
        simulator = new MockSim(bus);   // <<-- this was missing

        // Command center "Clear Requests" button → controller
        commandCenterPanel.setOnClearRequests(controller::clearAllRequests);
        lobbyPanel.setOnViewingFloorChanged(controller::pushUi);
    }

    private HBox buildLayout() {
        HBox root = new HBox(32, lobbyPanel, cabinPanel);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color:linear-gradient(to bottom,#f8fafc,#e2e8f0);");
        return root;
    }

    private void showCommandCenterWindow() {
        Stage ccStage = new Stage();
        ccStage.setTitle("Elevator Command Center");
        ccStage.setScene(new Scene(commandCenterPanel, 360, 400));
        ccStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

