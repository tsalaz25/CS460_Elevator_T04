package Sim;

import Wiring.EventBus;
import Wiring.Topics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Lightweigh Motion SIm that advances one floor per tick in response to Controller Commands
 */
public class MockSim {

    private static final long TICK_MS = 750L;
    private final EventBus bus;
    private final ScheduledExecutorService scheduler;
    private final Object stateLock = new Object();
    private final Deque<Integer> pendingTargets = new  ArrayDeque<>();
    private ScheduledFuture<?> tickerFuture;
    private int currFloor = -1;
    private Integer activeTarget = null;

    // ====== CONSTRUCTION ======
    public MockSim(EventBus bus){
        this.bus = bus;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "MockSimTicker");
            thread.setDaemon(true);
            return thread;
        });

        this.bus.subscribe(Topics.CTRL_CMD_MOVE_TO, event -> {
            Object payload = event.payload();
            if (payload instanceof Integer targetFloor) {
                onMoveTo(targetFloor);
            }
        });

        log("MockSim ready");
    }

    // ====== COMMAND HANDLERS ======
    private void onMoveTo(int targetFloor) {
        List<Integer> immediateArrivals = new ArrayList<>();
        synchronized (stateLock) {
            pendingTargets.add(targetFloor);
            logLocked("enqueue move->" + targetFloor);
            assignNextTargetsLocked(immediateArrivals);
        }

        for (Integer floor : immediateArrivals) {
            log("immediate arrival at floor " + floor + " (already here)");
            bus.publish(Topics.SIM_ARRIVED, floor);
        }
    }

    // ====== TICK LOOP ======
    private void tick() {
        Integer floorTick = null;
        Integer arrivalFloor = null;
        List<Integer> chainedArrivals = new ArrayList<>();

        synchronized (stateLock) {
            if (activeTarget == null) {
                stopTickerLocked();
                return;
            }


            int direction = Integer.compare(activeTarget, currFloor);
            if (direction == 0) {
                arrivalFloor = currFloor;
                activeTarget = null;
                logLocked("arrival at " + currFloor);
                chainedArrivals = new ArrayList<>();
                assignNextTargetsLocked(chainedArrivals);
            } else {
                currFloor += direction;
                floorTick = currFloor;
                logLocked("tick -> " + currFloor + " toward " + activeTarget);

                if (currFloor == activeTarget) {
                    arrivalFloor = currFloor;
                    activeTarget = null;
                    logLocked("arrival at " + currFloor);
                    chainedArrivals = new ArrayList<>();
                    assignNextTargetsLocked(chainedArrivals);
                }
            }
        }

        if (floorTick != null) {
            bus.publish(Topics.SIM_FLOOR_TICK, floorTick);
        }

        if (arrivalFloor != null) {
            bus.publish(Topics.SIM_FLOOR_TICK, arrivalFloor);
        }

        if (chainedArrivals != null) {
            for (Integer floor : chainedArrivals) {
                bus.publish(Topics.SIM_ARRIVED, floor);
            }
        }
    }

    // ====== INTERNAL HELPERS ======
    private void assignNextTargetsLocked(List<Integer> immediateArrivals) {
        while (activeTarget == null && !pendingTargets.isEmpty()) {
            int next = pendingTargets.poll();
            if (next == currFloor) {
                immediateArrivals.add(currFloor);
            } else {
                activeTarget = next;
                logLocked("activate target " + next);
                ensureTickerRunningLocked();
            }
        }

        if (activeTarget == null && pendingTargets.isEmpty()) {
            stopTickerLocked();
        }
    }

    // ======  HELPERS ======
    private void ensureTickerRunningLocked() {
        if (tickerFuture == null || tickerFuture.isCancelled() || tickerFuture.isDone()) {
            tickerFuture = scheduler.scheduleAtFixedRate(this::tick, 0,  TICK_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopTickerLocked() {
        if (tickerFuture != null) {
            tickerFuture.cancel(false);
            tickerFuture = null;
            logLocked("ticker stopped (no work)");
        }
    }

    // ====== LOGGING ======
    private void log(String message) {
        System.out.println("[SIM ] " + message + " | state " + stateSummary());
    }

    private void logLocked(String message) {
        // Caller must hold stateLock
        System.out.println("[SIM ] " + message + " | state " + stateSummaryLocked());
    }

    private String stateSummary() {
        synchronized (stateLock) {
            return stateSummaryLocked();
        }
    }

    private String stateSummaryLocked() {
        String queue = pendingTargets.isEmpty() ? "[]" : pendingTargets.toString();
        return "curr=" + currFloor +
                " active=" + activeTarget +
                " queued=" + queue;
    }
}


/*
* @Tomas MockSim — Create a lightweight simulator that subscribes to CTRL_CMD_MOVE_TO(int) and “moves” one floor
* per tick (e.g., every ~500–700 ms) from the current floor toward the target. On each tick,
* publish SIM_FLOOR_TICK(current); when current == target, publish SIM_ARRIVED(current) and stop the tick loop.
* Decide how to handle mid-travel commands (ignore, queue, or retarget—pick one and keep it consistent),
* ensure ticks are monotonic (no skipping floors), and make the tick scheduler deterministic.
* No UI calls from here—only bus publishes. Keep shared state (current, target, moving) safe for the scheduler
* you choose (Timeline or ScheduledExecutorService).
 */
