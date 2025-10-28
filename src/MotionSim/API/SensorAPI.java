package API;
import Devices.Sensor;

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
        for(int i = 0; i < sensorCount;i++){
            sensors[i].setActive(upper,lower,MARGIN);
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