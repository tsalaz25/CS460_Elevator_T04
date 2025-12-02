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
    private final EventBus bus;
    private final LobbyPanelAPI lobby;
    private final CabinPanelAPI cabin;
    private final CommandCenterPanelAPI commandCenter;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean overloaded = false;
    private boolean obstructed = false;
    private boolean moving = false;
    private DoorState doorState = DoorState.OPEN;

    private boolean fireMode = false;

    private final Set<Integer> hallUp   = new ConcurrentSkipListSet<>();
    private final Set<Integer> hallDown = new ConcurrentSkipListSet<>();
    private final Set<Integer> cabinSel = new ConcurrentSkipListSet<>();

    private final Clip fireLoop      = Sfx.FIRE_LOOP;
    private final Clip moveLoop      = Sfx.ELEVATOR_LOOP;
    private final Clip moveStartClip = Sfx.ELEVATOR_START;
    private final Clip doorCloseClip = Sfx.DOOR_CLOSE;
    private final Clip arriveBell    = Sfx.ELEV_BELL;
    private final Clip deny          = Sfx.DENY;

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
        pushUi();
        System.out.println("[CTRL] Controller booted (with command center) | state " + stateSummary());
    }

    private void wireSubscriptions() {
        bus.subscribe(Topics.UI_OVERLOAD_TOGGLED, e -> {
            overloaded = (boolean) e.payload();
            if (overloaded) obstructed = false;

            log("OVERLOAD toggled -> " + (overloaded ? "ON" : "OFF"));

            if (overloaded && moving) {
                moving = false;
                targetFloor = currentFloor;
                bus.publish(Topics.CTRL_CMD_STOP, null);
            }

            cabin.setOverloaded(overloaded);
            cabin.setObstructed(obstructed);

            schedule();
        });

        bus.subscribe(Topics.UI_OBSTRUCT_TOGGLED, e -> {
            obstructed = (boolean) e.payload();
            if (obstructed) overloaded = false;

            log("OBSTRUCTION toggled -> " + (obstructed ? "ON" : "OFF"));

            if (obstructed && moving) {
                moving = false;
                targetFloor = currentFloor;
                bus.publish(Topics.CTRL_CMD_STOP, null);
            }

            cabin.setOverloaded(overloaded);
            cabin.setObstructed(obstructed);

            if (obstructed) {
                doorState = DoorState.OBSTRUCTED;
                pushUi();
            }

            schedule();
        });

        bus.subscribe(Topics.UI_HALL_CALL_UP, e -> {
            int f = (int) e.payload();

            if (fireMode || overloaded || obstructed) {
                log("Denied request (fire/overload/obstruct active)");
                Sfx.play(deny);
                return;
            }

            log("Hall UP request received @ floor " + f);
            hallUp.add(f);
            schedule();
        });

        bus.subscribe(Topics.UI_HALL_CALL_DOWN, e -> {
            int f = (int) e.payload();

            if (fireMode || overloaded || obstructed) {
                log("Denied request (fire/overload/obstruct active)");
                Sfx.play(deny);
                return;
            }

            log("Hall DOWN request received @ floor " + f);
            hallDown.add(f);
            schedule();
        });

        bus.subscribe(Topics.UI_CABIN_SELECT, e -> {
            int f = (int) e.payload();

            if (fireMode || overloaded || obstructed) {
                log("Denied request (fire/overload/obstruct active)");
                Sfx.play(deny);
                return;
            }

            log("Cabin floor selected: " + f);
            cabinSel.add(f);
            schedule();
        });

        bus.subscribe(Topics.UI_FIRE_TOGGLED, e -> {
            boolean active = (boolean) e.payload();
            fireMode = active;
            log("Fire mode toggled -> " + (fireMode ? "ACTIVE" : "OFF"));

            if (fireMode) {
                Sfx.loop(fireLoop);
                hallUp.clear();
                hallDown.clear();
                cabinSel.clear();
            } else {
                Sfx.stop(fireLoop);
            }

            schedule();
        });

        bus.subscribe(Topics.SIM_FLOOR_TICK, e -> {
            int f = (int) e.payload();
            currentFloor = f;
            log("Tick -> floor " + currentFloor);

            if (moving && currentFloor == targetFloor) {
                moving = false;
                log("Arrived at target " + currentFloor);

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
                pushUi();
            }
        });

        bus.subscribe(Topics.SIM_ARRIVED, e -> {
            log("SIM_ARRIVED at floor " + e.payload());
        });
    }

    private void schedule() {
        if (overloaded || obstructed) {
            moving = false;
            targetFloor = currentFloor;

            doorState = obstructed ? DoorState.OBSTRUCTED : DoorState.OPEN;

            pushUi();
            return;
        }

        if (fireMode) {
            if (!moving && currentFloor != 0) {
                targetFloor = 0;
                moving = true;
                animateClosingThenDispatch();
            } else if (!moving && currentFloor == 0) {
                animateOpeningThen(null);
            }
            return;
        }

        if (moving) {
            log("schedule(): already moving, ignore new work");
            return;
        }

        Integer next = pickNearest();
        log("schedule(): evaluating next stop -> " + next);

        if (next == null) {
            targetFloor = currentFloor;
            log("schedule(): no pending requests, staying idle");
            pushUi();
            return;
        }

        if (next == currentFloor) {
            log("Serving current floor " + currentFloor + " without moving");
            clearServed(currentFloor);

            animateOpeningThen(() -> {
                PauseTransition dwell = new PauseTransition(Duration.seconds(0.5));
                dwell.setOnFinished(ev2 -> schedule());
                dwell.play();
            });
            return;
        }

        targetFloor = next;
        moving = true;
        animateClosingThenDispatch();
    }

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

    private void clearServed(int floor) {
        log("clearServed(" + floor + ")");

        hallUp.remove(floor);
        hallDown.remove(floor);
        cabinSel.remove(floor);

        Platform.runLater(() -> {
            lobby.resetUpRequest();
            lobby.resetDownRequest();
            lobby.setMoving(false);
        });
    }

    public void clearAllRequests() {
        log("clearAllRequests()");
        hallUp.clear();
        hallDown.clear();
        cabinSel.clear();
        pushUi();
    }

    private void animateClosingThenDispatch() {
        Platform.runLater(() -> {
            doorState = DoorState.CLOSING;
            Sfx.play(doorCloseClip);
            pushUi();

            PauseTransition closing = new PauseTransition(Duration.seconds(0.10));
            closing.setOnFinished(ev -> {
                doorState = DoorState.CLOSED;
                pushUi();

                PauseTransition delay = new PauseTransition(Duration.seconds(1.0));
                delay.setOnFinished(ev2 -> {
                    log("dispatching after close delay to " + targetFloor);
                    Sfx.play(moveStartClip);
                    Sfx.loop(moveLoop);
                    bus.publish(Topics.CTRL_CMD_MOVE_TO, targetFloor);
                });
                delay.play();
            });
            closing.play();
        });
    }

    private void animateOpeningThen(Runnable afterOpen) {
        Platform.runLater(() -> {
            doorState = DoorState.OPENING;
            pushUi();

            PauseTransition pt = new PauseTransition(Duration.seconds(0.1));
            pt.setOnFinished(ev -> {
                doorState = DoorState.OPEN;
                pushUi();

                if (afterOpen != null) {
                    afterOpen.run();
                }
            });
            pt.play();
        });
    }

    public void pushUi() {
        Platform.runLater(() -> {
            cabin.setCurrentFloor(currentFloor);

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

            int lobbyFloor = lobby.getTargetFloor();

            DoorState lobbyDoorState =
                    (currentFloor == lobbyFloor) ? doorState : DoorState.CLOSED;

            lobby.setDoorState(lobbyDoorState);
            lobby.setMoving(moving);
            lobby.setFireActive(fireMode);

            if (commandCenter != null) {
                commandCenter.setCurrentFloor(currentFloor);

                boolean hasTarget =
                        moving || !hallUp.isEmpty() || !hallDown.isEmpty() || !cabinSel.isEmpty();

                commandCenter.setTargetFloor(targetFloor, hasTarget);
                commandCenter.setMoving(moving);
                commandCenter.setDirection(directionStr);
                commandCenter.setDoorState(doorState);
                commandCenter.setFireMode(fireMode);

                commandCenter.setPendingHallUp(Set.copyOf(hallUp));
                commandCenter.setPendingHallDown(Set.copyOf(hallDown));
                commandCenter.setPendingCabin(Set.copyOf(cabinSel));
            }
        });
    }

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



