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

public class DemoSystemApp extends javafx.application.Application {

    /** TODO: create bus (InMemoryEventBus first), UIs (Lobby, Cabin), controller, simulator; wire callbacks */
    @Override public void start(javafx.stage.Stage stage) {
        // TODO: EventBus bus = new InMemoryEventBus(); // later: new SoftwareBusAdapter(...)
        // TODO: LobbyPanel lobby = new LobbyPanel(); lobby.setSystemMode(true);
        // TODO: CabinPanel cabin = new CabinPanel();

        // TODO: lobby.setOnUpPressed(()  -> bus.publish(Topics.UI_HALL_CALL_UP,   lobby.getTargetFloor()));
        // TODO: lobby.setOnDownPressed(() -> bus.publish(Topics.UI_HALL_CALL_DOWN, lobby.getTargetFloor()));
        // TODO: (optional) cabin.setOnFloorSelected(f -> bus.publish(Topics.UI_CABIN_SELECT, f));

        // TODO: new ElevatorController(bus, lobby, cabin);
        // TODO: new Simulator(bus); // or your MotionSim binding

        // TODO: layout both panels in an HBox; set Scene; show()
    }

    public static void main(String[] args) { launch(); }
}
