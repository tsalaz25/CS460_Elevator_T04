package LobbyGUI;

public interface LobbyPanelAPI {
    boolean upRequested();
    boolean downRequested();
    void resetUpRequest();
    void resetDownRequest();

    int getCurrentFloor();

    // Optional helpers for integration/testing
    int getTargetFloor();
    boolean isMoving();
}
