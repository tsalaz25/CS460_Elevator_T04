package LobbyGUI;

public interface LobbyPanelAPI {

    // Lamp/query API
    boolean upRequested();
    boolean downRequested();
    void resetUpRequest();
    void resetDownRequest();

    // For wiring & diagnostics
    int getCurrentFloor();
    int getTargetFloor();

    boolean isMoving();
    void setMoving(boolean moving);
}
