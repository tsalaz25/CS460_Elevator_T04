import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import API.SensorAPI;
import Devices.Sensor;

public class Simulator {
    static final double FLOOR_HEIGHT = 3.0;
    static final double CABIN_HEIGHT = 2.25;
    static final double V_MAX = 1.5;
    static final double A_MAX = 1.0;
    static final double DT = 0.01;
    static final double TOLERANCE = 0.005;
    static final int NUMFLOORS = 10;



    static double halfCabin;
    static double dAccel;
    static double tAccel;
    static double startPos;
    static double endPos;
    static double distance;
    static double dCruise;
    static double tCruise;
    static double totalTime;
    static int direction;
    static double topSensor;
    static double bottomSensor;
    static ElevatorModel model;
    static Sensor[] sensors;
    static SensorAPI sensorAPI;

    public static void main(String[] args) {
        halfCabin = CABIN_HEIGHT / 2;
        dAccel = V_MAX * V_MAX / (2 * A_MAX);
        tAccel = V_MAX / A_MAX;
        startPos = 0.0;
        endPos = FLOOR_HEIGHT;
        distance = Math.abs(endPos - startPos);
        dCruise = distance - 2 * dAccel;
        tCruise = (dCruise > 0) ? dCruise / V_MAX : 0;
        totalTime = 2 * tAccel + tCruise;

        direction = (endPos > startPos) ? 1 : -1;

        sensors = new Sensor[NUMFLOORS*2];

        for(int i = 0; i<NUMFLOORS*2; i++){
            double position = (i/2)*FLOOR_HEIGHT + (i%2)*CABIN_HEIGHT;
            boolean isRoof = (i%2 == 1);
            sensors[i] = new Sensor(position, i/2, isRoof);
        }

        sensorAPI = new SensorAPI(sensors,NUMFLOORS*2,TOLERANCE);

        bottomSensor = endPos - halfCabin;
        topSensor = endPos + halfCabin;

        model = new ElevatorModel();
        SimulatorGUI.setModel(model);

        System.out.printf("%-8s %-10s %-10s %-10s %-10s\n", "time", "pos", "vel", "botom_triggered", "aligned");

        SimulatorGUI.launch(SimulatorGUI.class, args);
    }

    public static void runSimulation() {
        double time = 0.0;
        double pos = startPos;
        double vel = 0.0;
        String phase = "accel";
        boolean triggeredDecel = false;

        while (time <= totalTime + 1.0) {
            if (phase.equals("accel") && Math.abs(vel) >= V_MAX) {
                phase = "cruise";
            }

            if (phase.equals("cruise") && !triggeredDecel) {
                double cabinBottom = pos - halfCabin;
                if ((direction == 1 && cabinBottom >= bottomSensor - TOLERANCE) || (direction == -1 && cabinBottom <= bottomSensor + TOLERANCE)) {
                    phase = "decel";
                    triggeredDecel = true;
                }
            }

            if (phase.equals("decel") && ((direction == 1 && pos >= endPos) || (direction == -1 && pos <= endPos))) {
                phase = "stop";
                pos = endPos;
                vel = 0.0;
            }

            double accel = 0;
            switch (phase) {
                case "accel":
                    accel = A_MAX * direction;
                    break;
                case "cruise":
                    accel = 0;
                    break;
                case "decel":
                    accel = -A_MAX * direction;
                    break;
                case "stop":
                    accel = 0;
                    vel = 0;
                    break;
            }

            vel += accel * DT;
            pos += vel * DT;

            double finalPos = pos;
            Platform.runLater(() -> model.setPosition(finalPos));

            double cabinTop = pos + halfCabin;
            double cabinBottom = pos - halfCabin;
            sensorAPI.updateSensors(cabinTop, cabinBottom);

            boolean bottomTriggered = Math.abs(cabinBottom - bottomSensor) <= TOLERANCE;
            boolean aligned = Math.abs(pos - endPos) <= TOLERANCE;

            System.out.printf("%-8.2f %-10.3f %-10.3f %-10s %-10s\n",
                    time, pos, vel,
                    bottomTriggered ? "Yes" : "No",
                    aligned ? "Yes" : "No");

            time += DT;

            try {
                Thread.sleep((long) (DT * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public static class ElevatorModel {
        private final DoubleProperty position = new SimpleDoubleProperty(0);
        public DoubleProperty positionProperty() { return position; }
        public void setPosition(double p) { position.set(p); }
    }

    public static class SimulatorGUI extends Application {
        private static ElevatorModel model;

        public SimulatorGUI() {}
        @Override
        public void start(Stage primaryStage) throws Exception {
            int elevatorWidth = 50;
            int elevatorHeight = 20;
            int gapHeight = 20;
            int numFloors = 10;
            int screenHeight = numFloors * elevatorHeight + (numFloors -1) * gapHeight;
            int screenWidth = elevatorWidth * 10;

            primaryStage.setTitle("MotionSim Simulator");
            primaryStage.setResizable(true);
            primaryStage.setWidth(screenWidth);
            primaryStage.setHeight(screenHeight + 100);
            Pane root = new Pane();


            Rectangle elevatorShaft = new Rectangle(elevatorWidth, screenHeight);
            elevatorShaft.setFill(Color.GREY);
            elevatorShaft.setX(screenWidth / 2 - elevatorWidth / 2);
            elevatorShaft.setY(0);
            root.getChildren().add(elevatorShaft);

            Rectangle elevator = new Rectangle(elevatorWidth, elevatorHeight/2);
            elevator.setFill(Color.BLUE);
            elevator.setX(screenWidth / 2 - elevatorWidth / 2);
            elevator.setY(screenHeight - elevatorHeight/2);
            root.getChildren().add(elevator);

            drawSensors(numFloors, elevatorWidth, screenWidth, screenHeight, gapHeight, elevatorHeight, root);

            model.positionProperty().addListener((obs, oldV, newV) -> {
                double ratio = elevatorHeight / Simulator.FLOOR_HEIGHT;
                double offset = gapHeight;
                elevator.setY(screenHeight - (2 * newV.doubleValue() * ratio) + elevatorHeight/4 - offset );
            });

            Scene scene = new Scene(root, screenWidth, screenHeight);
            primaryStage.setScene(scene);
            primaryStage.show();

            new Thread(Simulator::runSimulation).start();
        }

        private static void drawSensors(int numFloors, int elevatorWidth, int screenWidth, int screenHeight, int gapHeight, int elevatorHeight, Pane root) {
            for(int floor = 0; floor < numFloors; floor++) {
                Rectangle bottomSensor = new Rectangle(elevatorWidth, 1);
                if(sensors[floor*2].getActive()){
                    bottomSensor.setFill(Color.CHARTREUSE);
                } else {
                    bottomSensor.setFill(Color.RED);
                }
                bottomSensor.setX(screenWidth / 2 - elevatorWidth / 2);
                bottomSensor.setY(screenHeight - floor * (gapHeight + elevatorHeight) - elevatorHeight /4);
                Rectangle topSensor = new Rectangle(elevatorWidth, 1);
                if(sensors[(floor*2)+1].getActive()){
                    topSensor.setFill(Color.CHARTREUSE);
                } else {
                    topSensor.setFill(Color.RED);
                }
                topSensor.setX(screenWidth / 2 - elevatorWidth / 2);
                topSensor.setY(screenHeight - floor * (gapHeight + elevatorHeight) - (3*(elevatorHeight /4)));

                root.getChildren().addAll(bottomSensor, topSensor);
            }
        }

        public static void setModel(ElevatorModel m) {
            model = m;
        }
    }
}