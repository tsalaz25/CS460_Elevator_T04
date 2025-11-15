package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import CommandCenter.communicators.ElevatorCommunicator;
import CommandCenter.states.ElevatorDirectionState;
import CommandCenter.states.ElevatorDoorState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CCElevatorComponent extends GridPane {
    // Removed unused padding variable
    private static final int NUM_FLOORS = 10; // Use a constant
    private CCFloorComponent[] floors = new CCFloorComponent[NUM_FLOORS]; // Array indexed 0 to NUM_FLOORS-1
    private int currentFloorIndex = 0; // Index of the floor where the elevator is currently shown (0 = Floor 1)
    private ImageView upArrowImageView; // Store reference to arrow ImageViews
    private ImageView downArrowImageView;

    private ElevatorCommunicator elevatorCommunicator;

    public CCElevatorComponent(ElevatorCommunicator elevatorCommunicator) {
        super();
        this.elevatorCommunicator = elevatorCommunicator;
        this.setAlignment(Pos.CENTER);

        upArrowImageView = createDirectionArrow(true); // Assign to field
        VBox elevatorLocation = createElevatorLocation();
        downArrowImageView = createDirectionArrow(false); // Assign to field

        // Initially hide arrows
        upArrowImageView.setVisible(false);
        downArrowImageView.setVisible(false);

        this.add(upArrowImageView, 0, 0);
        this.add(elevatorLocation, 0, 1);
        this.add(downArrowImageView, 0, 2);

        // --- Initialize elevator visual state ---
        // Start at floor 1 (index 0) with doors closed (or open as per requirement)
        // Adjust initial state if needed
        updateVisualState(1, ElevatorDoorState.CLOSED);

        this.setOnMouseClicked(e -> {
            System.out.println("Elevator Component Clicked (Count: " + e.getClickCount() + ")");
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> updateComponent()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private ImageView createDirectionArrow(boolean isUp){
        ImageView arrow = new ImageView(); // Create empty first
        try {
            Image arrowImage = new Image(getClass().getResource("/resources/Arrow.png").toExternalForm());
            arrow.setImage(arrowImage);
            arrow.setFitHeight(30);
            arrow.setFitWidth(40);
            arrow.setPreserveRatio(false); // Allow stretching
            if (!isUp) {
                arrow.setScaleY(-1); // Flip for down arrow
                // Maybe add a slight translate Y to align better after flipping
                // arrow.setTranslateY(5);
            }
        } catch(Exception e) {
            System.err.println("Error loading Arrow.png: " + e.getMessage());
            // Optionally set a placeholder color/shape if image fails
        }
        return arrow;
    }

    private VBox createElevatorLocation() {
        VBox elevatorLocation = new VBox();
        // Removed fixed padding, let component sizes dictate height
        // elevatorLocation.setPadding(new Insets(this.padding));
        elevatorLocation.setAlignment(Pos.CENTER); // Center floors vertically
        elevatorLocation.setSpacing(0); // Floors should touch

        // Loop from 10 down to 1 to add floors visually top-to-bottom
        for (int i = NUM_FLOORS; i > 0; i--) {
            CCFloorComponent floorComponent = new CCFloorComponent(i);
            // Request buttons are handled separately in CommandCenterDisplay now
            // CCRequestButtonComponent requestButton = new CCRequestButtonComponent();

            // Store floor component in array using 0-based index
            // Floor 1 -> index 0, Floor 10 -> index 9
            this.floors[i - 1] = floorComponent;

            // Removed request button adding here
            elevatorLocation.getChildren().add(floorComponent);
        }

        // Removed initial updateElevatorCabin call, handled in constructor now.
        // this.floors[0].updateElevatorCabin(ElevatorDoorState.CLOSED); // Floor 1 is index 0

        elevatorLocation.setBackground(Background.fill(CommandCenterColors.ARROWTOWN));
        return elevatorLocation;
    }

    private void updateComponent() {
        this.updateVisualState(
                this.elevatorCommunicator.getCurrentFloor(),
                this.elevatorCommunicator.getElevatorDoorState()
        );

        ElevatorDirectionState directionState = this.elevatorCommunicator.getElevatorDirectionState();
        switch (directionState) {
            case UP -> showUpArrow();
            case DOWN -> showDownArrow();
            default -> hideArrows();
        }
    }

    /**
     * Updates the visual representation of the elevator.
     * Hides the cabin at the old floor and shows it at the new floor with the specified door state.
     *
     * @param newFloorNumber The floor number (1-10) where the elevator should be shown.
     * @param newState       The state of the elevator doors (OPEN/CLOSED).
     */
    public void updateVisualState(int newFloorNumber, ElevatorDoorState newState) {
        // Validate floor number
        if (newFloorNumber < 1 || newFloorNumber > NUM_FLOORS) {
            System.err.println("Invalid floor number received: " + newFloorNumber);
            return;
        }

        int newFloorIndex = newFloorNumber - 1; // Convert to 0-based index

        // Hide elevator at the current floor if it's valid
        if (currentFloorIndex >= 0 && currentFloorIndex < NUM_FLOORS && floors[currentFloorIndex] != null) {
            floors[currentFloorIndex].hideElevatorCabin();
        } else {
            System.out.println("Note: currentFloorIndex was invalid ("+ currentFloorIndex +") before move, hiding nothing.");
        }


        // Show elevator at the new floor
        if (newFloorIndex >= 0 && newFloorIndex < NUM_FLOORS && floors[newFloorIndex] != null) {
            floors[newFloorIndex].showElevatorCabin(newState);
            currentFloorIndex = newFloorIndex; // Update the current floor index
//            System.out.println("Elevator visual updated to Floor " + newFloorNumber + " (" + newState + ")");
        } else {
            System.err.println("Error showing elevator at invalid index: " + newFloorIndex + " (Floor " + newFloorNumber + ")");
            // Attempt to reset to a known state? Or leave invisible?
            // For now, update currentFloorIndex anyway to prevent repeated errors from old index
            currentFloorIndex = newFloorIndex;
        }

    }

    /**
     * Shows the UP direction arrow and hides the DOWN arrow.
     */
    public void showUpArrow() {
        if (upArrowImageView != null) upArrowImageView.setVisible(true);
        if (downArrowImageView != null) downArrowImageView.setVisible(false);
//        System.out.println("Showing UP arrow");
    }

    /**
     * Shows the DOWN direction arrow and hides the UP arrow.
     */
    public void showDownArrow() {
        if (upArrowImageView != null) upArrowImageView.setVisible(false);
        if (downArrowImageView != null) downArrowImageView.setVisible(true);
//        System.out.println("Showing DOWN arrow");
    }

    /**
     * Hides both direction arrows (e.g., when stationary).
     */
    public void hideArrows() {
        if (upArrowImageView != null) upArrowImageView.setVisible(false);
        if (downArrowImageView != null) downArrowImageView.setVisible(false);
//        System.out.println("Hiding arrows");
    }

    // TODO: Add method for dimming/undimming the entire component if needed for DISABLED state.
     /*
     public void setDimmed(boolean dimmed) {
         this.setOpacity(dimmed ? 0.5 : 1.0); // Example dimming effect
         this.setDisable(dimmed); // Prevent interaction when dimmed
     }
     */
}
