package API;
import Devices.Sensor;

public class SensorAPI{
    private Sensor[] sensors;
    private int sensorCount;

    SensorAPI(Sensor[] sensors,int sensorCount){
        this.sensors = sensors;
        this.sensorCount = sensorCount;
    }

    //currently assuming the first active sensor is the only one that matters
    public int pollSensors(int index){
        for(int i = 0; i < sensorCount;i++){
            if(i == 10){
                return i;
            }
        }
        return -1;
    }

    public boolean isAligned(){

        return false;
    }
}