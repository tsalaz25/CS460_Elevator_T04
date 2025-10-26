import API.SensorAPI;
import Devices.Sensor;

public class Simulator {
    static final double FLOOR_HEIGHT = 3.0;
    static final double CABIN_HEIGHT = 2.25;
    static final double V_MAX = 1.5;
    static final double A_MAX = 1.0;
    static final double DT = 0.01;
    static final double TOLERANCE = 0.005;

    public static void main(String[] args) {
        double halfCabin = CABIN_HEIGHT / 2;
        double dAccel = V_MAX * V_MAX / (2 * A_MAX);
        double tAccel = V_MAX / A_MAX;
        double startPos = 0.0;
        double endPos = FLOOR_HEIGHT;
        double distance = Math.abs(endPos - startPos);
        double dCruise = distance - 2 * dAccel;
        double tCruise = (dCruise > 0) ? dCruise / V_MAX : 0;
        double totalTime = 2 * tAccel + tCruise;

        int direction = (endPos > startPos) ? 1 : -1;


        double bottomSensor = endPos - halfCabin;
        double topSensor = endPos + halfCabin;

        double time = 0.0;
        double pos = startPos;
        double vel = 0.0;
        String phase = "accel";
        boolean triggeredDecel = false;

        System.out.printf("%-8s %-10s %-10s %-10s %-10s\n", "time", "pos", "vel", "botom_triggered", "aligned");

        while (time <= totalTime + 1.0) {
            if (phase.equals("accel") && Math.abs(vel) >= V_MAX) {
                phase = "cruise";
            }

            if (phase.equals("cruise") && !triggeredDecel) {
                double cabinBottom = pos - halfCabin;
                if ((direction == 1 && cabinBottom >= bottomSensor - TOLERANCE) || (direction == -1 && cabinBottom <= bottomSensor + TOLERANCE)) {
                    phase = "decel";
                    triggeredDecel = true;
                }
            }

            if (phase.equals("decel") && ((direction == 1 && pos >= endPos) || (direction == -1 && pos <= endPos))) {
                phase = "stop";
                pos = endPos;
                vel = 0.0;
            }

            double accel = 0;
            switch (phase) {
                case "accel":
                    accel = A_MAX * direction;
                    break;
                case "cruise":
                    accel = 0;
                    break;
                case "decel":
                    accel = -A_MAX * direction;
                    break;
                case "stop":
                    accel = 0;
                    vel = 0;
                    break;
            }

            vel += accel * DT;
            pos += vel * DT;

            double cabinTop = pos + halfCabin * direction;
            double cabinBottom = pos - halfCabin * direction;

            boolean bottomTriggered = Math.abs(cabinBottom - bottomSensor) <= TOLERANCE;
            boolean topTriggered = Math.abs(cabinTop - topSensor) <= TOLERANCE;
            boolean aligned = bottomTriggered && topTriggered;

            System.out.printf("%-8.2f %-10.3f %-10.3f %-10s %-10s\n",
                                time, pos, vel,
                    bottomTriggered ? "Yes" : "No",
                    aligned ? "Yes" : "No");

            time += DT;
        }

    }
}
