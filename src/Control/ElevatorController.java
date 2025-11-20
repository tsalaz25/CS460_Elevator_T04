package Control;
import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class ElevatorController {

    // ====== CONSTRUCTION ======
    public ElevatorController(Wiring.EventBus bus,
                              LobbyGUI.LobbyPanelAPI lobby,
                              CabinGUI.CabinPanelAPI cabin) {

        this.bus = bus;
        this.lobby = lobby;
        this.cabin = cabin;

        wireSubscriptions();
        pushUi();
    }

    // ====== BUS SUBSCRIPTIONS ======
    // Subscribes the ElevatorController to the event bus, sets
    // actions for Hall Up, Hall Down, Cabin Select, Sim Floor Tick, and Sim Arrive
    private void wireSubscriptions() {

        // Sets Hall Up
        bus.subscribe(Topics.UI_HALL_CALL_UP, e -> {
            int f = (int) e.payload();
            hallUp.add(f);
            schedule();
        });

        // Sets Hall Down
        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> {
            int f = (int) e.payload();
            hallDown.add(f);
            schedule();
        });

        // Sets Cabin Select
        bus.subscribe(Topics.UI_CABIN_SELECT, e -> {
            int f = (int) e.payload();
            cabinSel.add(f);
            schedule();
        });

        // Sets Sim Floor Tick
        bus.subscribe(Topics.SIM_FLOOR_TICK, e -> {
            pushUi();
        });

        // Sets Sim Arrive
        bus.subscribe(Topics.SIM_ARRIVED, e -> {
            int f = (int) e.payload();
            currentFloor = f;
            moving = false;

            clearServed(f);
            pushUi();
            schedule();
        });
    }

    // ====== SCHEDULING POLICY ======
    // This method defines how the elevator should respond to a scheduling request, determines
    // if the elevator is in use, does not need to move, or needs to be dispatched to a floor
    private void schedule() {

        // Elevator is in motion, cannot schedule at this time
        if (moving) return;

        // Find the closest pending request
        Integer next = pickNearest();

        // No pending requests or pending request is this floor
        if (next == null || next == currentFloor) {
            targetFloor = currentFloor;
            pushUi();
            return;
        }

        // Normal case, send elevator to nearest pending request
        targetFloor = next;
        moving = true;
        bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
        pushUi();
    }

    // Determines the closest pending request to the elevator
    private Integer pickNearest() {
        Integer best = null;
        int bestDist = Integer.MAX_VALUE;

        // Loops through pending Hall Up requests
        for (int f : hallUp) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        // Loops through pending Hall Down requests
        for (int f : hallDown) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        // Loops through pending Cabin Selection requests
        for (int f : cabinSel) {
            int d = Math.abs(f - currentFloor);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }

        return best;
    }

    // Removes pending request upon completion
    private void clearServed(int f) {

        // Attempts to remove the request from hallUp and hallDown
        boolean hadUp = hallUp.remove(f);
        boolean hadDown = hallDown.remove(f);

        // Attempts to remove the request from cabinSel
        cabinSel.remove(f);

        // Reset lamps if hallUp or hallDown
        if (hadUp) {
            lobby.resetUpRequest();
        }
        if (hadDown) {
            lobby.resetDownRequest();
        }
    }

    // ====== UI UPDATES ======
    // This method pushes the updated UI after every action and tick
    private void pushUi() {

        // Updates current and taget floors
        lobby.setCurrentFloor(currentFloor);
        lobby.setTargetFloor(targetFloor);

        // Updates current floor
        cabin.setCurrentFloor(currentFloor);

        // Calculates then updates direction
        String direction;
        if (targetFloor > currentFloor) {
            direction = "UP";
        } else if (targetFloor < currentFloor) {
            direction = "DOWN";
        } else {
            direction = "IDLE";
        }
        cabin.setDirection(direction);
    }

    // ====== FIELDS ======
    private final Wiring.EventBus bus;
    private final LobbyGUI.LobbyPanelAPI lobby;
    private final CabinGUI.CabinPanelAPI cabin;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

    private final Set<Integer> hallUp   = new ConcurrentSkipListSet<>();
    private final Set<Integer> hallDown = new ConcurrentSkipListSet<>();
    private final Set<Integer> cabinSel = new ConcurrentSkipListSet<>();
}
