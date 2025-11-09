package Devices;

import static src.MotionSim.Devices.Motor.CABIN_HEIGHT;

public class Sensor {
    private final double TOLERANCE = 0.01; //0.1

    private final double position;
    private final int floor;
    private final boolean isTop;
    private boolean active;

    public Sensor(double position, int floor, boolean isTop) {
        this.position = position;
        this.floor = floor;
        this.isTop = isTop;
        this.active = false;
    }

    public void update(double cabinCenter) {
        this.active = this.position <= (cabinCenter + CABIN_HEIGHT / 2) + this.TOLERANCE && this.position >= (cabinCenter - CABIN_HEIGHT / 2) - this.TOLERANCE;

    }

    public boolean isActive() {
        return active;
    }

    public int getFloor() {
        return floor;
    }

    public double getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("Sensor[floor=%d, pos=%.2f, type=%s, active=%b]",
                floor, position, isTop ? "Top" : "Floor", active);
    }
}
