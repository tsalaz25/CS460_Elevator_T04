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

    //I don't know if we're doing this in here or in the main function but i putting something here anway
    public void setActive(float position){
        //This likely won't work
        if (position == this.position){
            this.isActive = true;
        } else {
            this.isActive = false;
        }
    }

        public void setActive(boolean isActive){
            this.isActive = isActive;
        }
}