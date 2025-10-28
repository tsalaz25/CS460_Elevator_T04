package Devices;

public class Sensor{
    double position;
    boolean isActive = false;
    int floor;
    boolean isRoof;

    public Sensor(double position, int floor, boolean isRoof){
        this.position = position;
        this.floor = floor;
        this.isRoof = isRoof;
    }

    public void setActive(double upper,double lower , double MARGIN){
        if (Math.abs(upper - position) <= MARGIN || Math.abs(lower - position) <= MARGIN ){
            this.isActive = true;
        } else {
            this.isActive = false;
        }
    }

    public boolean getActive(){
        return isActive;
    }

}