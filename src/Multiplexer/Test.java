package Multiplexer;

import MotionSim.API.MotorAPI;
import Devices.Motor.Motion;
import Devices.Motor.Direction;
import MotionSim.API.SensorAPI;
import CabinGUI.CabinPanel;
import CabinGUI.DoorState;
import LobbyGUI.LobbyPanel;
import SoftwareBus.Bus.*;
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

import java.util.Scanner;



public class Test extends Application {
    public static final int NUM_FLOORS = 10;


    public static void main(String[] args) {
            launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Bus bus = new Bus();
        MotorAPI motor = new MotorAPI(NUM_FLOORS);
        SensorAPI sensor = new SensorAPI(NUM_FLOORS);
        LobbyPanel lobby = new LobbyPanel();
        Scene scene = new Scene(lobby, 380, 320);
        primaryStage.setTitle("Lobby GUI");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(360);
        primaryStage.setMinHeight(300);
        primaryStage.show();
        CabinPanel cabin = new CabinPanel();
        Button open   = new Button("Open Doors");
        Button close  = new Button("Close Doors");
        Button togOver= new Button("Toggle Overload");
        Button togObs = new Button("Toggle Obstruction");
        CheckBox auto = new CheckBox("Auto-close (3s)");


        open.setOnAction(e -> {
            cabin.setDoorState(DoorState.OPEN);
            if (auto.isSelected()) {
                PauseTransition pt = new PauseTransition(Duration.seconds(3));
                pt.setOnFinished(ev -> cabin.setDoorState(DoorState.CLOSED));
                pt.play();
            }
        });
        close.setOnAction(e -> cabin.setDoorState(DoorState.CLOSED));
        togOver.setOnAction(e -> cabin.setOverloaded(!cabin.overloaded()));
        togObs.setOnAction(e -> cabin.setObstructed(!cabin.obstructed()));

        HBox controls = new HBox(10, open, close, new Separator(), togOver, togObs, new Separator(), auto);
        controls.setPadding(new Insets(10));

        VBox cabinRoot = new VBox(12, cabin, controls);
        cabinRoot.setPadding(new Insets(12));

        Stage cabinStage = new Stage();
        Scene cabinScene = new Scene(cabinRoot);

        cabinStage.setTitle("Cabin GUI");
        cabinStage.setScene(cabinScene);
        cabinStage.sizeToScene();
        cabinStage.show();







        Topics.subscribeAll(bus,1);
        Topic motorCommand = new Topic(Topics.MOTOR_COMMAND,1);
        Topic doorCommand = new Topic(Topics.DOOR_COMMAND,1);
        Topic carRequest = new Topic(Topics.CAR_REQUEST,1);
        Topic cabinReset = new Topic(Topics.CABIN_BUTTON_RESET,1);
        Thread simulationThread = new Thread(() -> {


            Multiplexer multiplexer = new Multiplexer(1,bus,motor,sensor,lobby,cabin);

        });
        Thread simThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Press ENTER to open doors");
            scanner.nextLine();
            bus.publish(new Message(doorCommand,new int[]{0,-1,-1,-1}));
            System.out.println("Press ENTER to reset");
            scanner.nextLine();
            bus.publish(new Message(cabinReset,new int[]{0,-1,-1,-1}));

            System.out.println("Press ENTER to move motor up");
            scanner.nextLine();
            bus.publish(new Message(motorCommand,new int[]{1}));

            System.out.println("Press ENTER to stop motor");
            scanner.nextLine();
            bus.publish(new Message(motorCommand,new int[]{0}));
        });
        simThread.setDaemon(true);
        simThread.start();
        simulationThread.setDaemon(true);
        simulationThread.start();

    }
}
