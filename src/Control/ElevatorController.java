package Control;

import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;
import CommandCenterGUI.CommandCenterPanelAPI;
import CabinGUI.DoorState;

import javafx.application.Platform;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * ElevatorController
 *
 * Responsibilities (current behavior):
 *  - Subscribe to UI events:
 *      UI_HALL_CALL_UP(floor)
 *      UI_HALL_CALL_DOWN(floor)
 *      UI_CABIN_SELECT(floor)
 *  - Track pending requests in three sets.
 *  - Maintain currentFloor, targetFloor, moving flag.
 *  - On each schedule(), pick the nearest pending request and issue CTRL_CMD_MOVE_TO(target).
 *  - On SIM_FLOOR_TICK(floor), update currentFloor, detect arrival when floor == target.
 *  - On arrival, clear requests for that floor, reset lobby lamps, and reschedule if needed.
 *  - Push basic UI state into Cabin/Lobby via their APIs.
 *
 * Fire mode behavior:
 *  - fireMode flag is toggled by UI_FIRE_TOGGLED.
 *  - When fireMode is active:
 *      * all pending requests are cleared
 *      * elevator recalls to floor 0 and stays there with doors open
 *      * normal scheduling is suspended
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
    private DoorState doorState = DoorState.CLOSED;

    // Fire recall state
    private boolean fireMode = false;

    // Pending requests
    private final Set<Integer> hallUp   = new ConcurrentSkipListSet<>();
    private final Set<Integer> hallDown = new ConcurrentSkipListSet<>();
    private final Set<Integer> cabinSel = new ConcurrentSkipListSet<>();

    // ====== CONSTRUCTION ======
    public ElevatorController(EventBus bus,
                              LobbyPanelAPI lobby,
                              CabinPanelAPI cabin) {
        this.bus = bus;
        this.lobby = lobby;
        this.cabin = cabin;
        this.commandCenter = null;

        wireSubscriptions();
        pushUi(); // initial state
        log("Controller booted (no command center)");
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
        log("Controller booted (with command center)");
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
                // 1) Clear normal requests
                hallUp.clear();
                hallDown.clear();
                cabinSel.clear();

                // 2) If not at floor 0, recall to 0
                if (!moving && currentFloor != 0) {
                    targetFloor = 0;
                    moving = true;
                    closeDoors();
                    bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
                    pushUi();
                } else if (!moving && currentFloor == 0) {
                    // Already at recall floor: just sit here with doors open in fire mode.
                    openDoors();
                    pushUi();
                }
            } else {
                // Leaving fire mode:
                // Close doors and resume normal scheduling
                closeDoors();
                schedule();
            }
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

                // Fire recall arrival at floor 0
                if (fireMode && currentFloor == 0) {
                    // In fire mode we want to sit at 0 with doors open and ignore normal scheduling.
                    openDoors();
                    pushUi();
                    return;
                }

                // Normal stop: clear served requests
                clearServed(currentFloor);
                openDoors();

                pushUi();
                // Only normal mode should reschedule
                schedule();
            } else {
                // Just a passing floor
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
                closeDoors();
                bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
                pushUi();
            } else if (!moving && currentFloor == 0) {
                // Already at recall floor; just keep doors open and do nothing.
                openDoors();
                pushUi();
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
            openDoors();
            pushUi();
            // There might still be other requests; check again
            next = pickNearest();
            if (next == null || next == currentFloor) {
                targetFloor = currentFloor;
                pushUi();
                return;
            }
        }

        // Normal case: move to the selected next floor
        targetFloor = next;
        moving = true;
        closeDoors();

        log("schedule(): dispatching to " + targetFloor);
        bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
        pushUi();
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

    // ====== DOOR HELPERS ======
    private void openDoors() {
        if (doorState == DoorState.OPEN) { return; }
        doorState = DoorState.OPEN;
        Platform.runLater(() -> cabin.setDoorState(DoorState.OPEN));
    }

    private void closeDoors() {
        if (doorState == DoorState.CLOSED) { return; }
        doorState = DoorState.CLOSED;
        Platform.runLater(() -> cabin.setDoorState(DoorState.CLOSED));
    }

    // ====== UI UPDATES ======
    private void pushUi() {
        Platform.runLater(() -> {
            // Cabin reflects car position
            cabin.setCurrentFloor(currentFloor);

            // Direction for cabin display
            String direction;
            if (moving && targetFloor > currentFloor) {
                direction = "UP";
            } else if (moving && targetFloor < currentFloor) {
                direction = "DOWN";
            } else {
                direction = "IDLE";
            }
            cabin.setDirection(direction);
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
                commandCenter.setDirection(direction);
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


