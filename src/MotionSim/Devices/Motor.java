package src.MotionSim.Devices;

public final class Motor {

    public static final double FLOOR_HEIGHT = 4.0;
    public static final double CABIN_HEIGHT = 2.0;
    private static final double V_MAX = 2.0;
    private static final double A_MAX = 2.0;
    public static final double DT = 0.005;

    public enum Motion { START, STOP }
    public enum Direction { UP, DOWN }

    private volatile double position;
    private volatile double velocity;
    private Motion motion = Motion.STOP;
    private Direction direction = Direction.UP;
    private int startFloor;
    private int targetFloor;
    private int numFloors;

    private Thread simThread;
    private boolean running = false;

    public Motor(int numFloors) {
        this.numFloors = numFloors;
        this.position = 0;
        this.velocity = 0;
    }


    public synchronized void setDirection(Direction direction) {
        if (direction == Direction.UP && targetFloor < startFloor ||
                direction == Direction.DOWN && targetFloor > startFloor) {
            this.direction = (targetFloor > startFloor) ? Direction.UP : Direction.DOWN;
        } else this.direction = direction;
    }

    public synchronized void setMotion(Motion motion) {
        this.motion = motion;
        if (motion == Motion.START) startSimulation();
        if (motion == Motion.STOP) System.out.print("STOP commanded, Initiating deceleration\n");
    }


    public synchronized void setPosition(double pos) {
        this.position = pos;
        this.velocity = 0;
    }
    public synchronized double getPosition() {
        return position;
    }

    private void startSimulation() {
        if (simThread != null && simThread.isAlive()) return;

        running = true;
        simThread = new Thread(this::simulateMotion);
        simThread.setDaemon(true);
        simThread.start();
    }


    private void simulateMotion() {
        double targetPos = (targetFloor - 1) * FLOOR_HEIGHT;
        boolean inAccel = true, inCruise = false, inDecel = false;


        while (running) {
            synchronized (this) {

                // TODO : Maybe add a safety ("mechanical") stop for the bottom floor and top floor

                if (inAccel && Math.abs(velocity) >= V_MAX) {
                    System.out.print("Cruising\n");
                    inAccel = false;
                    inCruise = true;
                    inDecel = false;
                }

//                if ((direction == Direction.UP && position >= targetPos) ||
//                        (direction == Direction.DOWN && position <= targetPos)) {
//                    position = targetPos;
//                    velocity = 0;
//                    motion = Motion.STOP;
//                    running = false;
//                    System.out.printf("Reached target floor, stopping\n");
//                    break;
//                }

                if (motion == Motion.STOP && velocity != 0) {
                    inAccel = false;
                    inCruise = false;
                    inDecel = true;

                }

//                if (!inDecel && (Math.abs(position - targetPos) < (velocity * velocity) / (2 * A_MAX))) {
//                    inAccel = false;
//                    inCruise = false;
//                    inDecel = true;
//                    System.out.printf("Automatically switching to decel\n");
//                }

                double accel = 0;
                if (inAccel) {
                    accel = A_MAX * (direction == Direction.UP ? 1 : -1);
                } else if (inDecel) {
                    accel = -A_MAX * (direction == Direction.UP ? 1 : -1);
                }

                velocity += accel * DT;
//                if (Math.abs(velocity) > V_MAX) velocity = V_MAX * Math.signum(velocity);

                position += velocity * DT;

                if (inDecel && Math.abs(velocity) < 0) {
                    velocity = 0;
                    motion = Motion.STOP;
                    running = false;
                    System.out.print("Soft stop complete\n");
                    break;
                }


            }

            try { Thread.sleep((long)(DT*1000)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

    }

    public int getFloor(double position) {
        int closestFloor = 1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < numFloors; i++) {
            double floorCenter = i * FLOOR_HEIGHT;
            double distance = Math.abs(position - floorCenter);
            if (distance < minDistance) {
                minDistance = distance;
                closestFloor = i + 1; // convert to 1-based
            }
        }

        return closestFloor;
    }
}
