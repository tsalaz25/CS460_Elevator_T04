package Sim;

import Wiring.EventBus;
import Wiring.Topics;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MockSim {

    // ====== CONSTRUCTION ======
    /** TODO: keep bus reference; subscribe to CTRL_CMD_MOVE_TO; set up timing (ScheduledExecutorService or Timeline) */
    public MockSim(Wiring.EventBus bus /*, Motor motor, Sensor sensor */) {
        // TODO: this.bus = bus; this.motor = motor; this.sensor = sensor;
        // TODO: bus.subscribe(Topics.CTRL_CMD_MOVE_TO, e -> onMoveTo((Integer)e.payload()));
        // TODO: init ticker
    }

    // ====== COMMAND HANDLERS ======
    /** TODO: store target; if current==target publish SIM_ARRIVED; else start ticking toward target */
    private void onMoveTo(int targetFloor) {
        // TODO: set target; if equals current -> bus.publish(SIM_ARRIVED, current)
        // TODO: start ticking if not moving
    }

    // ====== TICK LOOP ======
    /** TODO: every ~500–700ms move one floor toward target; publish SIM_FLOOR_TICK; on arrival publish SIM_ARRIVED and stop */
    private void tick() {
        // TODO: if current==target -> stop; publish SIM_ARRIVED
        // TODO: else current += sign(target - current); publish SIM_FLOOR_TICK(current)
    }

    // ====== FIELDS ======
    // private final Wiring.EventBus bus;
    // private volatile int current = 0;
    // private volatile Integer target = null;
    // private volatile boolean moving = false;
    // private java.util.concurrent.ScheduledExecutorService exec; // or Timeline if you prefer FX
    // private MotionSim.Motor motor; // TODO: integrate later
    // private MotionSim.Sensor sensor;
}
