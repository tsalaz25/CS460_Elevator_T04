package CommandCenter.communicators;

import CommandCenter.states.ElevatorMode;
import CommandCenter.Topics;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Bus;
import javafx.application.Platform;
import java.util.function.Consumer;

/**
 * Handles communication between the Side Panel UI and the Software Bus.
 * Manages elevator mode state (INDEPENDENT, CENTRALIZED, FIRE) and
 * publishes/receives mode change messages.
 */
public class SidePanelCommunicator {

    // Software Bus instance for communication
    private Bus bus;

    // Current elevator system mode
    private ElevatorMode currentMode;

    // Callback to notify UI when mode changes from bus
    private Consumer<ElevatorMode> onModeChangedFromBus;

    /**
     * Constructor - initializes the bus connection and sets default mode
     */
    public SidePanelCommunicator() {
        // Create new bus instance
        this.bus = new Bus();

        // Subscribe to mode change messages
        this.bus.subscribe(Topics.ELEVATOR_MODE);

        // Set default mode to INDEPENDENT (as per CommandCenter design)
        this.currentMode = ElevatorMode.INDEPENDENT;

        // Start background thread to listen for incoming mode changes
        startMessagePollingThread();
    }

    /**
     * Start a background thread that continuously polls for incoming mode messages
     */
    private void startMessagePollingThread() {
        Thread pollingThread = new Thread(() -> {
            while (true) {
                // Check for mode change messages from the bus
                Message modeMessage = this.bus.getMessage(Topics.ELEVATOR_MODE);

                if (modeMessage != null) {
                    // Extract mode value from message body
                    int modeValue = modeMessage.bodyOne();

                    // Convert to ElevatorMode enum
                    // 0 = INDEPENDENT, 1 = CENTRALIZED, 2 = FIRE
                    if (modeValue >= 0 && modeValue < ElevatorMode.values().length) {
                        ElevatorMode newMode = ElevatorMode.values()[modeValue];

                        // Only update if mode actually changed
                        if (this.currentMode != newMode) {
                            this.currentMode = newMode;
                            System.out.println("Mode updated from bus: " + newMode);

                            // Notify UI callback if registered (on JavaFX thread)
                            if (onModeChangedFromBus != null) {
                                final ElevatorMode modeToNotify = newMode;
                                Platform.runLater(() -> onModeChangedFromBus.accept(modeToNotify));
                            }
                        }
                    }
                }

                // Poll every 100ms to avoid busy waiting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Polling thread interrupted");
                    break;
                }
            }
        });

        // Set as daemon thread so it doesn't prevent program exit
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    /**
     * Get the current elevator mode
     * @return the current ElevatorMode (INDEPENDENT, CENTRALIZED, or FIRE)
     */
    public ElevatorMode getCurrentMode() {
        return this.currentMode;
    }

    /**
     * Set a callback to be notified when mode changes from the bus
     * This allows the UI to update when external components change the mode
     * @param callback Consumer that will be called with the new mode
     */
    public void setOnModeChangedFromBus(Consumer<ElevatorMode> callback) {
        this.onModeChangedFromBus = callback;
    }

    /**
     * Set the elevator mode and publish the change to the bus
     * @param newMode the new ElevatorMode to set
     */
    public void setMode(ElevatorMode newMode) {
        // Update internal state
        this.currentMode = newMode;

        // Encode mode as integer for message body
        // INDEPENDENT = 0, CENTRALIZED = 1, FIRE = 2
        int modeValue = newMode.ordinal();

        // Create message with mode value in body[0]
        int[] body = new int[]{modeValue, 0, 0, 0};
        Message message = new Message(Topics.ELEVATOR_MODE, body);

        // Publish to bus
        this.bus.publish(message);

        System.out.println("Mode changed to: " + newMode);
    }

}
