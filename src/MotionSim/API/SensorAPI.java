package API;
import Devices.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorAPI{
    private Sensor[] sensors;
    private int sensorCount;
    private final double MARGIN;

    public SensorAPI(Sensor[] sensors,int sensorCount,double MARGIN){
        this.sensors = sensors;
        this.sensorCount = sensorCount;
        this.MARGIN = MARGIN;
    }

    public void updateSensors(double upper,double lower){
        for(int i = 0; i < sensorCount; i++){
            sensors[i].setActive(upper,lower,MARGIN);
        }
    }

    public List<Sensor> pollSensors() {
        List<Sensor> activeSensors = new ArrayList<>();

        for (int i = 0; i < sensorCount; i++) {
            if (sensors[i].getActive()) {
                activeSensors.add(sensors[i]);
            }
        }

        return activeSensors;
    }

    public boolean isAligned(int floor){
        int upper = (floor-1)*2;
        int lower = upper + 1;
        return sensors[upper].getActive() && sensors[lower].getActive();
        
    }
}