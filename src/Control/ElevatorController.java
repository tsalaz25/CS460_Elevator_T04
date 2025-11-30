package Control;

import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;
import CommandCenterGUI.CommandCenterPanelAPI;
import CabinGUI.DoorState;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import javax.sound.sampled.Clip;
import Audio.Sfx;

/**
 * ElevatorController
 *
 * Responsibilities:
 *  - Subscribe to UI events:
 *      UI_HALL_CALL_UP(floor)
 *      UI_HALL_CALL_DOWN(floor)
 *      UI_CABIN_SELECT(floor)
 *      UI_FIRE_TOGGLED(boolean)
 *  - Track pending requests in three sets.
 *  - Maintain currentFloor, targetFloor, moving flag, fireMode.
 *  - On each schedule(), pick the nearest pending request and issue CTRL_CMD_MOVE_TO(target),
 *    with a short door-closing animation delay before motion.
 *  - On SIM_FLOOR_TICK(floor), update currentFloor, detect arrival when floor == target.
 *  - On arrival, clear requests for that floor, animate doors opening, dwell briefly, then reschedule.
 *  - Push UI state into Cabin/Lobby/CommandCenter via their APIs.
 */
public class ElevatorController {

    // ====== FIELDS ======
    private final EventBus bus;
    private final LobbyPanelAPI lobby;
    private final CabinPanelAPI cabin;
    private final CommandCenterPanelAPI commandCenter;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;
    private DoorState doorState = DoorState.OPEN;

    // Fire recall state
    private boolean fireMode = false;

    // Pending requests
    private final Set<Integer> hallUp   = new ConcurrentSkipListSet<>();
    private final Set<Integer> hallDown = new ConcurrentSkipListSet<>();
    private final Set<Integer> cabinSel = new ConcurrentSkipListSet<>();

    private final Clip fireLoop      = Sfx.FIRE_LOOP;
    private final Clip moveLoop      = Sfx.ELEVATOR_LOOP;
    private final Clip moveStartClip = Sfx.ELEVATOR_START;
    private final Clip doorCloseClip = Sfx.DOOR_CLOSE;
    private final Clip arriveBell    = Sfx.ELEV_BELL;

    // ====== CONSTRUCTION ======
    public ElevatorController(EventBus bus,
                              LobbyPanelAPI lobby,
                              CabinPanelAPI cabin) {
        this(bus, lobby, cabin, null);
        System.out.println("[CTRL] Controller booted (no command center) | state " + stateSummary());
    }

    public ElevatorController(EventBus bus,
                              LobbyPanelAPI lobby,
                              CabinPanelAPI cabin,
                              CommandCenterPanelAPI commandCenter) {
        this.bus = bus;
        this.lobby = lobby;
        this.cabin = cabin;
        this.commandCenter = commandCenter;

        wireSubscriptions();
        pushUi(); // initial state
        System.out.println("[CTRL] Controller booted (with command center) | state " + stateSummary());
    }

    // ====== BUS SUBSCRIPTIONS ======
    private void wireSubscriptions() {

        // Hall Up call from lobby at a given floor
        bus.subscribe(Topics.UI_HALL_CALL_UP, e -> {
            int f = (int) e.payload();
            log("Hall UP request received @ floor " + f);
            hallUp.add(f);
            schedule();
        });

        // Hall Down call from lobby at a given floor
        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> {
            int f = (int) e.payload();
            log("Hall DOWN request received @ floor " + f);
            hallDown.add(f);
            schedule();
        });

        // Cabin floor selection
        bus.subscribe(Topics.UI_CABIN_SELECT, e -> {
            int f = (int) e.payload();
            log("Cabin floor selected: " + f);
            cabinSel.add(f);
            schedule();
        });

        // Fire mode toggle (from Lobby and/or Command Center)
        bus.subscribe(Topics.UI_FIRE_TOGGLED, e -> {
            boolean active = (boolean) e.payload();
            fireMode = active;
            log("Fire mode toggled -> " + (fireMode ? "ACTIVE" : "OFF"));

            if (fireMode) {
                // Start fire alarm loop
                Sfx.loop(fireLoop);

                // Clear normal requests when entering fire mode
                hallUp.clear();
                hallDown.clear();
                cabinSel.clear();
            } else {
                // Stop fire alarm when leaving fire mode
                Sfx.stop(fireLoop);
            }

            // Let schedule() decide recall / behavior based on fireMode flag
            schedule();
        });

        // Simulator reports a floor tick (we moved one floor)
        bus.subscribe(Topics.SIM_FLOOR_TICK, e -> {
            int f = (int) e.payload();
            currentFloor = f;
            log("Tick -> floor " + currentFloor);

            // Arrival detection: while moving, if we tick into the target floor
            if (moving && currentFloor == targetFloor) {
                moving = false;
                log("Arrived at target " + currentFloor);

                // Stop movement loop sound & ding
                Sfx.stop(moveLoop);
                Sfx.play(arriveBell);

                if (fireMode && currentFloor == 0) {
                    animateOpeningThen(null);
                    return;
                }

                clearServed(currentFloor);
                animateOpeningThen(() -> {
                    PauseTransition dwell = new PauseTransition(Duration.seconds(5.0));
                    dwell.setOnFinished(ev2 -> schedule());
                    dwell.play();
                });
            } else {
                // Just a passing floor: update UI only
                pushUi();
            }
        });

        // Optional: SIM_ARRIVED (we don't rely on it, but you can log it)
        bus.subscribe(Topics.SIM_ARRIVED, e -> {
            log("SIM_ARRIVED at floor " + e.payload());
            // Optional cross-check with SIM_FLOOR_TICK.
        });
    }

    // ====== SCHEDULING POLICY ======
    private void schedule() {

        // Fire mode overrides normal scheduling: recall to floor 0 and stay there.
        if (fireMode) {
            if (!moving && currentFloor != 0) {
                targetFloor = 0;
                moving = true;
                animateClosingThenDispatch();
            } else if (!moving && currentFloor == 0) {
                // Already at recall floor; keep doors open.
                animateOpeningThen(null);
            }
            return;
        }

        // If the car is already moving, don't change the active command
        if (moving) {
            log("schedule(): already moving, ignore new work");
            return;
        }

        // Find the closest pending request
        Integer next = pickNearest();
        log("schedule(): evaluating next stop -> " + next);

        if (next == null) {
            // No pending work → idle at currentFloor
            targetFloor = currentFloor;
            log("schedule(): no pending requests, staying idle");
            pushUi();
            return;
        }

        // If next is the current floor, immediately serve it (no motion)
        if (next == currentFloor) {
            log("Serving current floor " + currentFloor + " without moving");
            clearServed(currentFloor);

            // Doors may already be open; ensure they are, then dwell and re-evaluate.
            animateOpeningThen(() -> {
                PauseTransition dwell = new PauseTransition(Duration.seconds(0.5));
                dwell.setOnFinished(ev2 -> schedule());
                dwell.play();
            });
            return;
        }

        // Normal case: move to the selected next floor
        targetFloor = next;
        moving = true;
        animateClosingThenDispatch();
    }

    // Pick the nearest floor with any kind of pending request
    private Integer pickNearest() {
        Integer best = null;
        int bestDist = Integer.MAX_VALUE;

        for (int f : hallUp) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        for (int f : hallDown) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        for (int f : cabinSel) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        return best;
    }

    // ====== CLEAR SERVED / CLEAR ALL ======
    private void clearServed(int floor) {
        log("clearServed(" + floor + ")");

        // Drop from all queues
        hallUp.remove(floor);
        hallDown.remove(floor);
        cabinSel.remove(floor);

        // Clear lobby lamps when any hall request is served.
        Platform.runLater(() -> {
            lobby.resetUpRequest();
            lobby.resetDownRequest();
            lobby.setMoving(false); // make sure buttons are enabled again
        });
    }

    /**
     * Clear all pending requests (used by Command Center "Clear Requests" button).
     */
    public void clearAllRequests() {
        log("clearAllRequests()");
        hallUp.clear();
        hallDown.clear();
        cabinSel.clear();
        pushUi();
    }

    // ====== DOOR ANIMATIONS ======

    /**
     * Animate doors closing (OPEN/OPENING -> CLOSING -> CLOSED) and then
     * dispatch the move command to the simulator after a short delay.
     */
    private void animateClosingThenDispatch() {
        Platform.runLater(() -> {
            // Step 1: start closing (half-open image) + door closing sound
            doorState = DoorState.CLOSING;
            Sfx.play(doorCloseClip);
            pushUi();

            PauseTransition closing = new PauseTransition(Duration.seconds(0.10));  // visible closing
            closing.setOnFinished(ev -> {

                // Step 2: fully closed
                doorState = DoorState.CLOSED;
                pushUi();

                // Step 3: wait extra 1 second before moving
                PauseTransition delay = new PauseTransition(Duration.seconds(1.0));
                delay.setOnFinished(ev2 -> {
                    log("dispatching after close delay to " + targetFloor);

                    // Play one-shot start clip
                    Sfx.play(moveStartClip);
                    // Start continuous movement loop
                    Sfx.loop(moveLoop);

                    // Tell simulator to move
                    bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
                });
                delay.play();
            });
            closing.play();
        });
    }

    /**
     * Animate doors opening (CLOSED/CLOSING -> OPENING -> OPEN).
     * Optionally run a callback after the doors are fully open.
     */
    private void animateOpeningThen(Runnable afterOpen) {
        Platform.runLater(() -> {
            // Step 1: opening / half-open image
            doorState = DoorState.OPENING;
            pushUi();

            PauseTransition pt = new PauseTransition(Duration.seconds(0.1));
            pt.setOnFinished(ev -> {
                // Step 2: fully open
                doorState = DoorState.OPEN;
                pushUi();

                if (afterOpen != null) {
                    afterOpen.run();
                }
            });
            pt.play();
        });
    }

    // ====== UI UPDATES ======
    private void pushUi() {
        Platform.runLater(() -> {
            // Cabin reflects car position
            cabin.setCurrentFloor(currentFloor);

            // Direction for cabin display
            String directionStr;
            if (moving && targetFloor > currentFloor) {
                directionStr = "UP";
            } else if (moving && targetFloor < currentFloor) {
                directionStr = "DOWN";
            } else {
                directionStr = "IDLE";
            }
            cabin.setDirection(directionStr);
            cabin.setDoorState(doorState);

            // Lobby Indicator
            lobby.setDoorState(doorState);
            lobby.setMoving(moving);

            // --- Command Center (if present) ---
            if (commandCenter != null) {
                commandCenter.setCurrentFloor(currentFloor);
                // Treat having any pending work or moving as "has target"
                boolean hasTarget =
                        moving || !hallUp.isEmpty() || !hallDown.isEmpty() || !cabinSel.isEmpty();
                commandCenter.setTargetFloor(targetFloor, hasTarget);
                commandCenter.setMoving(moving);
                commandCenter.setDirection(directionStr);
                commandCenter.setDoorState(doorState);
                commandCenter.setFireMode(fireMode);

                // Send copies so UI can't mutate internal sets
                commandCenter.setPendingHallUp(Set.copyOf(hallUp));
                commandCenter.setPendingHallDown(Set.copyOf(hallDown));
                commandCenter.setPendingCabin(Set.copyOf(cabinSel));
            }
        });
    }

    // ====== LOGGING ======
    private void log(String message) {
        System.out.println("[CTRL] " + message + " | state " + stateSummary());
    }

    private String stateSummary() {
        return "curr=" + currentFloor +
                " target=" + targetFloor +
                " moving=" + moving +
                " fire=" + fireMode +
                " pending=" + pendingSummary();
    }

    private String pendingSummary() {
        String up = summarizeSet(hallUp);
        String down = summarizeSet(hallDown);
        String cab = summarizeSet(cabinSel);
        return "{up=" + up + ",down=" + down + ",cab=" + cab + "}";
    }

    private String summarizeSet(Set<Integer> set) {
        if (set.isEmpty()) return "[]";
        return set.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}


