package LobbyGUI;

import CabinGUI.DoorState;

public interface LobbyPanelAPI {

    // Hall-call lamp / query API
    boolean upRequested();
    boolean downRequested();
    void resetUpRequest();
    void resetDownRequest();

    // Status for controller / diagnostics
    int getCurrentFloor();
    int getTargetFloor();

    boolean isMoving();
    void setMoving(boolean moving);

    //Door Indicator
    void setDoorState(DoorState doorState);

    // Fire-alarm support
    void setFireActive(boolean active);
    void setOnFireToggled(Runnable r);
    boolean isFireActive();
}
