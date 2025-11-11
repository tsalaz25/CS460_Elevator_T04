package MotionSim.API;

import MotionSim.Devices.Sensor;
import java.util.ArrayList;
import java.util.List;
import static Devices.Motor.CABIN_HEIGHT;
import static Devices.Motor.FLOOR_HEIGHT;

public class SensorAPI {
    private final List<Sensor> sensors = new ArrayList<>();

    public SensorAPI(int numFloors) {
        for (int i = 0; i < numFloors; i++) {
            int floor = (int) (i * FLOOR_HEIGHT);
            double floorSensorPos = floor - CABIN_HEIGHT / 2;
            double ceilingSensorPos = floor + CABIN_HEIGHT / 2;
//            System.out.printf("creating floor %d sensor at positions %f %f\n", i+1, floorSensorPos, ceilingSensorPos);

            sensors.add(new Sensor(floorSensorPos, i + 1, false));
            sensors.add(new Sensor(ceilingSensorPos, i + 1, true));
        }
    }

    public void updateSensors(double cabinCenter) {
        sensors.forEach(sensor -> sensor.update(cabinCenter));
    }


    // TODO : Find the best way to check if the floor is aligned
    public boolean isFloorAligned(int floor) {
        List<Sensor> floorSensors = new ArrayList<>();
        int activeSensors = 0;
        for (Sensor sensor : sensors) {
            if (floor == sensor.getFloor()) {
                floorSensors.add(sensor);
            }
        }

        for (Sensor sensor : floorSensors) {
            if (sensor.isActive()) {
                activeSensors++;
            }
        }

        return  activeSensors == 2;
    }

    // TODO : We could remove if we add if our floor has an active sensor
    public List<Sensor> pollActiveSensors() {
        return sensors.stream().filter(Sensor::isActive).toList();
    }

    // TODO : Remove when we get rid of simulation. We do not need to show all sensors.
    public List<Sensor> getAllSensors() { return sensors; }

    // TODO : Decide if we want the api to tell us if our target floor has an active sensor
    // public boolean doesOurFloorHaveAnActiveSensor(int targetFloor)
}
