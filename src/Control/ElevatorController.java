package Control;
import Wiring.EventBus;
import Wiring.Topics;
import LobbyGUI.LobbyPanelAPI;
import CabinGUI.CabinPanelAPI;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class ElevatorController {

    // ====== CONSTRUCTION ======
    /** TODO: keep references to bus + UI APIs; call wireSubscriptions(); push initial UI state */
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
    private void wireSubscriptions() {
        bus.subscribe(Topics.UI_HALL_CALL_UP,   e -> { queue add; schedule(); });
        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> { queue add; schedule(); });
        bus.subscribe(Topics.UI_CABIN_SELECT,   e -> { queue add; schedule(); });
        bus.subscribe(Topics.SIM_FLOOR_TICK,    e -> { currentFloor = ...; pushUi(); });
        bus.subscribe(Topics.SIM_ARRIVED,       e -> { currentFloor = ...; moving=false; clearServed(currentFloor); pushUi(); schedule(); });
    }

    // ====== SCHEDULING POLICY ======
    /** TODO: if not moving, choose next target (nearest is fine); publish CTRL_CMD_MOVE_TO(target) */
    private void schedule() {
        // TODO: if (moving) return;
        // TODO: Integer next = pickNearest();
        // TODO: if (next == null || next == currentFloor) { targetFloor = currentFloor; pushUi(); return; }
        // TODO: targetFloor = next; moving = true; bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor); pushUi();
    }

    /** TODO: scan hallUp, hallDown, cabinSel; return the closest to currentFloor (tie-breaker arbitrary) */
    private Integer pickNearest() { /* TODO */ return null; }

    /** TODO: remove any requests served at floor f; reset lobby lamps if appropriate */
    private void clearServed(int f) {
        // TODO: hallUp.remove(f); hallDown.remove(f); cabinSel.remove(f);
        // TODO: lobby.resetUpRequest(); lobby.resetDownRequest();
    }

    // ====== UI UPDATES ======
    /** TODO: push state to panels (floor/target/direction) */
    private void pushUi() {
        // TODO: lobby.setCurrentFloor(currentFloor);
        // TODO: lobby.setTargetFloor(targetFloor);
        // TODO: cabin.setCurrentFloor(currentFloor);
        // TODO: cabin.setDirection(targetFloor > currentFloor ? "UP" : targetFloor < currentFloor ? "DOWN" : "IDLE");
    }

    // ====== FIELDS ======
    private final Wiring.EventBus bus; private final LobbyGUI.LobbyPanelAPI lobby; private final CabinGUI.CabinPanelAPI cabin;
    private int currentFloor = 0, targetFloor = 0; private boolean moving = false;
    private final java.util.Set<Integer> hallUp = new java.util.concurrent.ConcurrentSkipListSet<>();
    private final java.util.Set<Integer> hallDown = new java.util.concurrent.ConcurrentSkipListSet<>();
    private final java.util.Set<Integer> cabinSel = new java.util.concurrent.ConcurrentSkipListSet<>();
}
