package Demo;

import javafx.application.Application;
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

public class DemoSystemApp extends Application {
    @Override
    public void start(Stage stage) {
    }

    public static void main(String[] args) { launch(); }
}
