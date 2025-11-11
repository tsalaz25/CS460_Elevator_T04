package MotionSim;

import API.MotorAPI;
import MotionSim.API.SensorAPI;
import MotionSim.Devices.Sensor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import MotionSim.Devices.*;

import java.util.List;

import static Devices.Motor.CABIN_HEIGHT;
import static Devices.Motor.FLOOR_HEIGHT;


public class Simulator extends Application {

    public static final int NUM_FLOORS = 10;

    private MotorAPI motorAPI;
    private SensorAPI sensorAPI;
    private int startFloor = 6;
    private int targetFloor = 3;

    private ElevatorModel model;
    private Rectangle elevator;
    private Rectangle[] sensorRects;
    private double shaftHeight;
    private double floorHeightPixels;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        motorAPI = new MotorAPI(NUM_FLOORS);
        sensorAPI = new SensorAPI(NUM_FLOORS);

        Devices.Motor.Direction direction = (targetFloor > startFloor) ? Devices.Motor.Direction.UP : Devices.Motor.Direction.DOWN;
        motorAPI.setDirection(direction);

        double initialPos = (startFloor - 1) * FLOOR_HEIGHT;
        System.out.printf("initial position : %f\n", initialPos);

        motorAPI.setPosition(initialPos); // TODO: Decide if we always want the elevator to start at 0.0 ?? Then we could just set it during init

        motorAPI.setMotion(Devices.Motor.Motion.START);


        // GUI
        model = new ElevatorModel();
        model.setPosition(initialPos);

        int shaftWidth = 60;
        int screenHeight = 600;
        int screenWidth = 200;
        shaftHeight = screenHeight;
        floorHeightPixels = shaftHeight / NUM_FLOORS;
        double elevatorPixelHeight = (CABIN_HEIGHT / (NUM_FLOORS * FLOOR_HEIGHT)) * shaftHeight;

        Pane root = new Pane();

        Rectangle shaft = new Rectangle(screenWidth / 2.0 - shaftWidth / 2.0, 0, shaftWidth, screenHeight + elevatorPixelHeight/2);
        shaft.setFill(Color.GREY);
        root.getChildren().add(shaft);

        elevator = new Rectangle(screenWidth / 2.0 - shaftWidth / 2.0, 0, shaftWidth, elevatorPixelHeight);
        elevator.setFill(Color.BLUE);
        root.getChildren().add(elevator);

        Text elevatorLabel = new Text();
        elevatorLabel.setFill(Color.WHITE);
        elevatorLabel.setFont(Font.font(12));
        root.getChildren().add(elevatorLabel);

        sensorRects = new Rectangle[NUM_FLOORS * 2];

        for (int i = 0; i < NUM_FLOORS; i++) {
            double floorCenterMeters = i * FLOOR_HEIGHT;
            double bottomSensorMeters = floorCenterMeters - (CABIN_HEIGHT / 2);
            double topSensorMeters = floorCenterMeters + (CABIN_HEIGHT / 2);

            double bottomSensorY = shaftHeight - (bottomSensorMeters / (NUM_FLOORS * FLOOR_HEIGHT)) * shaftHeight;
            double topSensorY = shaftHeight - (topSensorMeters / (NUM_FLOORS * FLOOR_HEIGHT)) * shaftHeight;

            double floorBaseY = (bottomSensorY + topSensorY) / 2;

            Rectangle bottomSensor = new Rectangle(
                    screenWidth / 2.0 - shaftWidth / 2.0,
                    bottomSensorY,
                    shaftWidth, 2);
            bottomSensor.setFill(Color.YELLOW);
            sensorRects[i * 2] = bottomSensor;
            root.getChildren().add(bottomSensor);

            Text bottomLabel = new Text("B" + (i + 1));
            bottomLabel.setFont(Font.font(10));
            bottomLabel.setX(screenWidth / 2.0 + shaftWidth / 2.0 + 5); // Right side of shaft
            bottomLabel.setY(bottomSensorY + 4);  // Slight vertical offset
            root.getChildren().add(bottomLabel);

            Rectangle topSensor = new Rectangle(
                    screenWidth / 2.0 - shaftWidth / 2.0,
                    topSensorY,
                    shaftWidth, 2);
            topSensor.setFill(Color.YELLOW);
            sensorRects[i * 2 + 1] = topSensor;
            root.getChildren().add(topSensor);

            Text topLabel = new Text("T" + (i + 1));
            topLabel.setFont(Font.font(10));
            topLabel.setX(screenWidth / 2.0 + shaftWidth / 2.0 + 5);
            topLabel.setY(topSensorY + 4);
            root.getChildren().add(topLabel);

            double floorLineY = shaftHeight - floorCenterMeters / (NUM_FLOORS * FLOOR_HEIGHT) * shaftHeight;
            Line floorLine = new Line(0, floorLineY, screenWidth, floorLineY);
            floorLine.setStroke(Color.LIGHTGRAY);
            floorLine.getStrokeDashArray().addAll(5d, 5d);
            root.getChildren().add(floorLine);

            Text floorHeightLabel = new Text((FLOOR_HEIGHT * i) + " m");
            floorHeightLabel.setFont(Font.font(10));
            floorHeightLabel.setX(10);
            floorHeightLabel.setY(floorLineY + 4);
            root.getChildren().add(floorHeightLabel);

            Text floorLabel = new Text(String.valueOf(i + 1));
            floorLabel.setFont(Font.font(14));
            floorLabel.setX(10);
            floorLabel.setY(floorBaseY - 5);

            root.getChildren().add(floorLabel);



        }

        Text floorText = new Text();
        floorText.setFont(Font.font(14));
        floorText.setX(10);
        floorText.setY(20);
        root.getChildren().add(floorText);

        model.positionProperty().addListener((obs, oldV, newV) -> {
            double cabinCenter = newV.doubleValue();
            double y = shaftHeight - (cabinCenter / (NUM_FLOORS * FLOOR_HEIGHT)) * shaftHeight - (elevator.getHeight() / 2.0);
            elevator.setY(y);

            // Update text label
            elevatorLabel.setText(String.format("%.2f m", cabinCenter));
            elevatorLabel.setX(elevator.getX() + 5);
            elevatorLabel.setY(y + elevator.getHeight() / 2.0);

            updateSensorColors();

            // Update floor label text (don’t recreate it)
            int floor = motorAPI.getFloor(cabinCenter);
            floorText.setText("Floor " + floor);
        });

        Scene scene = new Scene(root, screenWidth, screenHeight+floorHeightPixels);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Elevator Simulator");
        primaryStage.show();

        Thread motorThread = new Thread(this::runMotorLoop);
        motorThread.setDaemon(true);
        motorThread.start();
    }

    private void runMotorLoop() {
        while (true) {
            double pos = motorAPI.getPosition();
            Platform.runLater(() -> model.setPosition(pos));
            sensorAPI.updateSensors(pos);

//            System.out.printf("Cabin pos:  %.2f\n", pos);
            if (sensorAPI.isFloorAligned(targetFloor)) {
                System.out.printf("Aligned:  %b\n", sensorAPI.isFloorAligned(targetFloor));
                break;
            }


//            Sensor sensor = sensorAPI.getActiveSensor();
//            if (sensor.getFloor() == this.targetFloor) {
//                // we need the sensor position?
//                // so do we return the sensor or the position?
//                // Probably sensor? but position is least amount of parsing
//                //
//
//                double distance = pos - sensor.getPosition();
//
//                if (Math.abs(distance) <= 0.001) {
//                    motorAPI.setMotion(Motor.Motion.STOP);
//                }
//            }
            // TODO : Move this. Maybe split it up so that we check if the floor we want has an active sensor, and if so we start decelerating
            List<Sensor> activeSensors = sensorAPI.pollActiveSensors();
            for (Sensor sensor : activeSensors) {
                if (sensor.getFloor() == this.targetFloor) {
                    double sensorPos = sensor.getPosition();
                    double distance = pos - sensorPos;

                    if (Math.abs(distance) <= 0.001) {
                        System.out.printf("sending stop at position %.2f\n", pos);
                        motorAPI.setMotion(Devices.Motor.Motion.STOP);
                        break;
                    }
                }
            }

            try {
                Thread.sleep((long) (Devices.Motor.DT * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateSensorColors() {
        List<Sensor> activeSensors = sensorAPI.pollActiveSensors();
        List<Sensor> allSensors = sensorAPI.getAllSensors();
        for (int i = 0; i < sensorRects.length; i++) {
            sensorRects[i].setFill(activeSensors.contains(allSensors.get(i)) ? Color.GREEN : Color.YELLOW);
        }
    }

    public static class ElevatorModel {
        private final DoubleProperty position = new SimpleDoubleProperty(0);
        public DoubleProperty positionProperty() { return position; }
        public void setPosition(double p) { position.set(p); }
    }
}
