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

public class DemoSystemApp extends Application {
    private EventBus bus;
    private LobbyPanel lobbyPanel;
    private CabinPanel cabinPanel;
    private CommandCenterPanel commandCenterPanel;

    private ElevatorController controller;
    private MockSim simulator;

    @Override
    public void start(Stage stage) {
        bootstrapCoreComponents();
        wireUiCallbacks();
        bootstrapDomainComponents();

        System.out.println("[DemoSystemApp] Booted with InMemoryEventBus + MockSim");

        Scene scene = new Scene(buildLayout(), 900, 600);
        stage.setTitle("Elevator Control Demo");
        stage.setScene(scene);
        stage.show();

        showCommandCenterWindow();
    }

    private void bootstrapCoreComponents() {
        bus = new InMemoryEventBus();
        lobbyPanel = new LobbyPanel();
        lobbyPanel.setSystemMode(true);
        cabinPanel = new CabinPanel();
        commandCenterPanel = new CommandCenterPanel();
    }

    private void wireUiCallbacks() {
        lobbyPanel.setOnUpPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_UP, lobbyPanel.getTargetFloor()));

        lobbyPanel.setOnDownPressed(() ->
                bus.publish(Topics.UI_HALL_CALL_DOWN, lobbyPanel.getTargetFloor()));

        cabinPanel.setOnFloorSelected(f ->
                bus.publish(Topics.UI_CABIN_SELECT, f));

        lobbyPanel.setOnFireToggled(() ->
                bus.publish(Topics.UI_FIRE_TOGGLED, lobbyPanel.isFireActive()));

        commandCenterPanel.setOnFireToggled(active ->
                bus.publish(Topics.UI_FIRE_TOGGLED, active));

        cabinPanel.setOnOverloadToggled(active ->
                bus.publish(Topics.UI_OVERLOAD_TOGGLED, active));

        cabinPanel.setOnObstructToggled(active ->
                bus.publish(Topics.UI_OBSTRUCT_TOGGLED, active));
    }

    private void bootstrapDomainComponents() {
        controller = new ElevatorController(bus, lobbyPanel, cabinPanel, commandCenterPanel);
        simulator = new MockSim(bus);

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


