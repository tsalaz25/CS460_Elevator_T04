package LobbyGUI;

public interface LobbyPanelAPI {
    // Whether the up/down hall call is currently lit (requested)
    boolean upRequested();
    boolean downRequested();

    // Reset the hall calls (turn off the light)
    void resetUpRequest();
    void resetDownRequest();

    // Optional status helpers
    void setCurrentFloor(int f);
    int getCurrentFloor();
}
