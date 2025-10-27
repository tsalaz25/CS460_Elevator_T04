package Devices;

public class Sensor{
    float position;
    boolean isActive = false;
    int floor;
    boolean isRoof;

    public Sensor(float position, int floor, boolean isRoof){
        this.position = position;
        this.floor = floor;
        this.isRoof = isRoof;
    }

    public void setActive(float position, float MARGIN){
        if (position > this.position - MARGIN && position < this.position + MARGIN){
            this.isActive = true;
        } else {
            this.isActive = false;
        }
    }

    public boolean getActive(){
        return isActive;
    }

}