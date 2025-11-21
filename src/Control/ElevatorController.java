package Control;

import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;
// TODO TOMAS: you will likely need this for door state when you implement doors.
// import CabinGUI.DoorState;

import javafx.application.Platform;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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
 * TODO DANIEL:
 *  - Implement fire mode:
 *      * track a fireMode flag.
 *      * override normal scheduling when fireMode is true (recall to floor 0, open doors, etc.).
 *      * subscribe to a fire-topic (e.g., UI_FIRE_TOGGLED) and control fireMode from there.
 *
 * TODO TOMAS:
 *  - Implement door behavior:
 *      * doors closed while moving, open on arrival at a served floor.
 *      * coordinate CabinPanel door state (and optionally Lobby door display).
 */
public class ElevatorController {

    // ====== FIELDS ======
    private final EventBus bus;
    private final LobbyPanelAPI lobby;
    private final CabinPanelAPI cabin;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

    // TODO DANIEL: track fire mode once you hook up the fire-alarm topic.
    // private boolean fireMode = false;

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

        wireSubscriptions();
        pushUi(); // initial state
    }

    // ====== BUS SUBSCRIPTIONS ======
    private void wireSubscriptions() {

        // Hall Up call from lobby at a given floor
        bus.subscribe(Topics.UI_HALL_CALL_UP, e -> {
            int f = (int) e.payload();
            System.out.println("[CTRL] Hall UP request at floor " + f);
            hallUp.add(f);
            schedule();
        });

        // Hall Down call from lobby at a given floor
        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> {
            int f = (int) e.payload();
            System.out.println("[CTRL] Hall DOWN request at floor " + f);
            hallDown.add(f);
            schedule();
        });

        // Cabin floor selection
        bus.subscribe(Topics.UI_CABIN_SELECT, e -> {
            int f = (int) e.payload();
            System.out.println("[CTRL] Cabin selected floor " + f);
            cabinSel.add(f);
            schedule();
        });

        // TODO DANIEL:
        // Subscribe to your fire-topic (e.g. Topics.UI_FIRE_TOGGLED) and control fireMode here.
        //
        // bus.subscribe(Topics.UI_FIRE_TOGGLED, e -> {
        //     boolean active = (boolean) e.payload();
        //     fireMode = active;
        //     System.out.println("[CTRL] Fire mode = " + fireMode);
        //
        //     if (fireMode) {
        //         // 1) Clear normal requests (hallUp, hallDown, cabinSel)
        //         // 2) If currentFloor != 0, command a move to 0
        //         // 3) If currentFloor == 0, open doors and stay put
        //     } else {
        //         // Leaving fire mode:
        //         // 1) Close doors
        //         // 2) Resume normal scheduling (schedule())
        //     }
        // });

        // Simulator reports a floor tick (we moved one floor)
        bus.subscribe(Topics.SIM_FLOOR_TICK, e -> {
            int f = (int) e.payload();
            currentFloor = f;
            System.out.println("[CTRL] Tick: currentFloor=" + currentFloor +
                    " target=" + targetFloor + " moving=" + moving);

            // Arrival detection: while moving, if we tick into the target floor
            if (moving && currentFloor == targetFloor) {
                moving = false;

                // TODO DANIEL:
                // If fireMode is true AND currentFloor == 0, handle special fire-arrival behavior.

                // Normal stop: clear served requests
                clearServed(currentFloor);

                // TODO TOMAS:
                // Call openDoors() here once you implement it.
                // openDoors();

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
            System.out.println("[CTRL] SIM_ARRIVED at floor " + e.payload());
            // TODO (optional): if you want, you can cross-check with SIM_FLOOR_TICK logic here.
        });
    }

    // ====== SCHEDULING POLICY ======
    private void schedule() {

        // TODO DANIEL:
        // If fireMode is true, ignore normal scheduling and instead:
        //  - ensure we're recalling to floor 0
        //  - if already at 0, keep doors open, do not dispatch new moves.
        //
        // if (fireMode) {
        //     if (!moving && currentFloor != 0) {
        //         targetFloor = 0;
        //         moving = true;
        //         // TODO TOMAS: closeDoors() before moving, once implemented.
        //         // closeDoors();
        //         bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
        //         pushUi();
        //     }
        //     return;
        // }

        // If the car is already moving, don't change the active command
        if (moving) {
            System.out.println("[CTRL] schedule(): already moving, ignoring.");
            return;
        }

        // Find the closest pending request
        Integer next = pickNearest();
        System.out.println("[CTRL] schedule(): current=" + currentFloor + " next=" + next);

        if (next == null) {
            // No pending work → idle at currentFloor
            targetFloor = currentFloor;
            pushUi();
            return;
        }

        // If next is the current floor, immediately serve it (no motion)
        if (next == currentFloor) {
            clearServed(currentFloor);
            // TODO TOMAS: openDoors() here when serving current floor without moving.
            // openDoors();
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

        // TODO TOMAS: close doors before moving once you implement door logic.
        // closeDoors();

        System.out.println("[CTRL] schedule(): dispatching to " + targetFloor);
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

    // ====== CLEAR SERVED REQUESTS ======
    private void clearServed(int floor) {
        System.out.println("[CTRL] clearServed(" + floor + ")");

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

    // ====== DOOR HELPERS (TODO TOMAS) ======
    // These are placeholders for Tomas to implement. They are NOT called yet
    // (calls above are commented out as TODOs).
    //
    // Once implemented, Tomas can:
    //  - import CabinGUI.DoorState
    //  - call cabin.setDoorState(DoorState.OPEN/CLOSED)
    //  - optionally also add a lobby door indicator and update that too.

    // private void openDoors() {
    //     // TODO TOMAS:
    //     // Example:
    //     // Platform.runLater(() -> {
    //     //     cabin.setDoorState(DoorState.OPEN);
    //     //     // lobby.setDoorState(DoorState.OPEN); // if you add this to LobbyPanel
    //     // });
    // }

    // private void closeDoors() {
    //     // TODO TOMAS:
    //     // Example:
    //     // Platform.runLater(() -> {
    //     //     cabin.setDoorState(DoorState.CLOSED);
    //     //     // lobby.setDoorState(DoorState.CLOSED);
    //     // });
    // }

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

            // Lobby only needs to know if we are moving (for disabling buttons)
            lobby.setMoving(moving);
        });
    }
}


