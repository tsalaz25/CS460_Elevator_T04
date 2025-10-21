package API;

public class SensorAPI{
    private boolean[] sensors;

    SensorAPI(boolean[] sensors){
        this.sensors = sensors;
    }

    public boolean pollSensor(int index){
        boolean sensor = sensors[index];
        return sensor;
    }
}