package API;
import Devices.Sensor;

public class SensorAPI{
    private Sensor[] sensors;
    private int sensorCount;
    private final float MARGIN = 0.00001f;

    SensorAPI(Sensor[] sensors,int sensorCount){
        this.sensors = sensors;
        this.sensorCount = sensorCount;

    }

    public void updateSensors(float position){
        for(int i = 0; i < sensorCount;i++){
            sensors[i].setActive(position,MARGIN);
        }
    }

    //Still just the first active sensor
    public Sensor pollSensors(){
        for(int i = 0; i < sensorCount;i++){
            if(sensors[i].getActive()){
                return sensors[i];
            }
        }

        return null;
    }

    public boolean isAligned(int floor){
        int upper = (floor-1)*2;
        int lower = upper + 1;
        return sensors[upper].getActive() && sensors[lower].getActive();
        
    }
}