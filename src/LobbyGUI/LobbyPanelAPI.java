package LobbyGUI;

public interface LobbyPanelAPI {
    boolean upRequested();
    boolean downRequested();
    void resetUpRequest();
    void resetDownRequest();

    void setCurrentFloor(int f);
    int getCurrentFloor();

    // Optional helpers for integration/testing
    void setTargetFloor(int f);
    int getTargetFloor();
    boolean isMoving();
}
