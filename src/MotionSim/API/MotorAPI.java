package MotionSim.API;

import Devices.Motor;
import Devices.Motor.Motion;
import Devices.Motor.Direction;

public class MotorAPI {
    Motor motor;

    public MotorAPI(int numFloors) { motor = new Motor(numFloors); }

    public void setDirection(Direction direction) {
        motor.setDirection(direction);
    }

    public void setMotion(Motion motion) {
        motor.setMotion(motion);
    }

    // TODO : Possibly remove, but we may use this to set it to 0. We may also want to always start at 0
    // So we could remove it if we always start at 0.0
    // However we may keep if want to set it exactly when we reach the floor
    // i.e. turn a 3.91 into 4.0
    public void setPosition(double pos) {
        motor.setPosition(pos);
    }

    public double getPosition() {
        return motor.getPosition();
    }

    public int getFloor(double position) {
        return motor.getFloor(position);
    }

}
