/** Team 5 Motor device for Motion Simulations API */
public final class Motor {

    /** Motion states */
    public enum Motion { STARTED, STOPPED }

    /** Direction states */
    public enum Direction { UP, DOWN }

    private Motion motion;
    private Direction direction;

    /** Constructor: motor starts stopped and facing up */
    public Motor() {
        motion = Motion.STOPPED;
        direction = Direction.UP;
    }

    /** Sets whether the motor is started or stopped */
    public void setMotion(Motion motion) {
        this.motion = motion;
        switch (motion) {
            case STARTED:
                System.out.println("Motor started moving " + direction);
                break;
            case STOPPED:
                System.out.println("Motor stopped");
                break;
        }
    }

    /** Sets the direction of the motor (UP or DOWN) */
    public void setDirection(Direction direction) {
        this.direction = direction;
        switch (direction) {
            case UP:
                System.out.println("Direction set to UP");
                break;
            case DOWN:
                System.out.println("Direction set to DOWN");
                break;
        }
    }
}
