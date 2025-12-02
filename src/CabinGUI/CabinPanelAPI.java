package CabinGUI;
public interface CabinPanelAPI {
    boolean hasSelection();
    int selectedFloor();
    void resetSelection();

    boolean emergency();
    void resetEmergency();

    DoorState doorState();
    void setDoorState(DoorState s);

    boolean overloaded();
    void setOverloaded(boolean v);

    boolean obstructed();
    void setObstructed(boolean v);

    void setCurrentFloor(int f);
    void setDirection(String s);
    void setFireActive(boolean active);
}
