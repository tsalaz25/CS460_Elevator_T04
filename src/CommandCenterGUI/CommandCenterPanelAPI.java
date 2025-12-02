package CommandCenterGUI;

import CabinGUI.DoorState;

import java.util.Set;

public interface CommandCenterPanelAPI {

    void setCurrentFloor(int floor);
    void setTargetFloor(int targetFloor, boolean hasTarget);
    void setMoving(boolean moving);
    void setDirection(String direction);
    void setDoorState(DoorState doorState);
    void setFireMode(boolean fireActive);
    void setPendingHallUp(Set<Integer> floors);
    void setPendingHallDown(Set<Integer> floors);
    void setPendingCabin(Set<Integer> floors);
}

