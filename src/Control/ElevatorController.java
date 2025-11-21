package Control;

import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;
import javafx.application.Platform;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class ElevatorController {

    // ====== FIELDS ======
    private final EventBus bus;
    private final LobbyPanelAPI lobby;
    private final CabinPanelAPI cabin;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

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
            hallUp.add(f);
            schedule();
        });

        // Hall Down call from lobby at a given floor
        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> {
            int f = (int) e.payload();
            hallDown.add(f);
            schedule();
        });

        // Cabin floor selection
        bus.subscribe(Topics.UI_CABIN_SELECT, e -> {
            int f = (int) e.payload();
            cabinSel.add(f);
            schedule();
        });

        // Simulator reports a floor tick (we moved one floor)
        bus.subscribe(Topics.SIM_FLOOR_TICK, e -> {
            int f = (int) e.payload();
            currentFloor = f;

            // Treat "arrival" as: while moving, tick floor == target
            if (moving && currentFloor == targetFloor) {
                moving = false;
                clearServed(currentFloor);
                pushUi();
                schedule(); // look for next work
            } else {
                pushUi();
            }
        });

        // SIM_ARRIVED is optional for us now; we just log it if it comes.
        bus.subscribe(Topics.SIM_ARRIVED, e -> {
            // You can log if you want:
            // System.out.println("[CTRL] SIM_ARRIVED at floor " + e.payload());
            // But logic is already handled in SIM_FLOOR_TICK.
        });
    }

    // ====== SCHEDULING POLICY ======
    private void schedule() {
        // If the car is already moving, we don't change the active command
        if (moving) return;

        // Find the closest pending request
        Integer next = pickNearest();

        if (next == null) {
            // No pending work → idle at currentFloor
            targetFloor = currentFloor;
            pushUi();
            return;
        }

        // If next is the current floor, immediately serve it (no motion)
        if (next == currentFloor) {
            clearServed(currentFloor);
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
        // Drop from all queues
        hallUp.remove(floor);
        hallDown.remove(floor);
        cabinSel.remove(floor);

        // Always clear lobby lamps when any hall request is served.
        Platform.runLater(() -> {
            lobby.resetUpRequest();
            lobby.resetDownRequest();
            lobby.setMoving(false); // make sure buttons are enabled again
        });
    }

    // ====== UI UPDATES ======
    private void pushUi() {
        Platform.runLater(() -> {
            // Cabin always reflects the car position
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

            // Let Lobby know if the car is moving so it can enable/disable buttons
            lobby.setMoving(moving);
        });
    }
}

