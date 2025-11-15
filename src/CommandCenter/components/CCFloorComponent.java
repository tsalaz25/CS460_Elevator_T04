package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import CommandCenter.states.ElevatorDoorState;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color; // Keep Color import
import javafx.scene.paint.Paint; // Import Paint
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CCFloorComponent extends StackPane {
    private SVGPath elevatorIconPath = null;
    private Label floorLabel;

    public CCFloorComponent(int floorNumber) {
        this.setBackground(Background.fill(CommandCenterColors.CHARADE));
        this.setMinSize(40, 60);
        this.setMaxSize(40, 60);
        this.createFloorLabel(floorNumber);
    }

    private void createFloorLabel(int floorNumber) {
        floorLabel = new Label(Integer.toString(floorNumber));
        floorLabel.setTextFill(CommandCenterColors.TERMINAL_ORANGE);
        try {
            Font ocrFont = Font.font("OCR A Extended", FontWeight.EXTRA_BOLD, 20);
            if (ocrFont != null) {
                floorLabel.setFont(ocrFont);
            } else {
                floorLabel.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 20));
            }
        } catch (Exception e) {
            floorLabel.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 20));
            System.err.println("Error loading OCR A Extended font: " + e.getMessage());
        }
        this.getChildren().add(floorLabel);
    }

    public void showElevatorCabin(ElevatorDoorState state) {
        if (this.elevatorIconPath == null) {
            this.elevatorIconPath = new ElevatorIcon();
            double scale = 0.08;
            this.elevatorIconPath.setScaleX(scale);
            this.elevatorIconPath.setScaleY(scale);
            this.getChildren().add(this.elevatorIconPath);
        }

        // --- Updated Color Logic ---
        Paint fillColor; // Use Paint type
        switch (state) {
            case OPEN:
                // CCR: Green when doors are open.
                fillColor = Color.LIMEGREEN; // Use a distinct green
                break;
            case CLOSED:
            default:
                // CCR: Match Floor Color (using TERMINAL_ORANGE as the floor/request color)
                fillColor = CommandCenterColors.TERMINAL_ORANGE;
                break;
        }
        // --- End Updated Color Logic ---

        this.elevatorIconPath.setFill(fillColor);
        this.elevatorIconPath.setVisible(true);
        if(floorLabel != null) {
            floorLabel.setVisible(false);
        }
    }

    public void hideElevatorCabin() {
        if (this.elevatorIconPath != null) {
            this.elevatorIconPath.setVisible(false);
        }
        if(floorLabel != null) {
            floorLabel.setVisible(true);
        }
    }

    /**
     * @deprecated Use showElevatorCabin(ElevatorDoorState) instead.
     */
    @Deprecated
    public void updateElevatorCabin(ElevatorDoorState elevatorState) {
        showElevatorCabin(elevatorState);
    }
}

