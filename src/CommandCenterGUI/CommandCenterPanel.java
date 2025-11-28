package CommandCenterGUI;

import CabinGUI.DoorState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandCenterPanel extends VBox implements CommandCenterPanelAPI {

    // STATUS LABELS
    private final Label currentFloorLbl  = new Label("Current: 0");
    private final Label targetFloorLbl   = new Label("Target: —");
    private final Label directionLbl     = new Label("Direction: IDLE");
    private final Label movingLbl        = new Label("Moving: false");
    private final Label doorStateLbl     = new Label("Door: CLOSED");
    private final Label fireModeLbl      = new Label("Fire Mode: OFF");

    // Pending requests
    private final Label hallUpLbl   = new Label("Hall Up: []");
    private final Label hallDownLbl = new Label("Hall Down: []");
    private final Label cabinLbl    = new Label("Cabin Sel: []");

    // Controls
    private final ToggleButton fireToggleBtn = new ToggleButton("Fire Mode");
    private final Button clearAllBtn         = new Button("Clear Requests");

    // Callbacks to outside world (wired in DemoSystemApp)
    private Consumer<Boolean> onFireToggled = active -> {};
    private Runnable onClearRequests        = () -> {};

    public CommandCenterPanel() {
        buildLayout();
        attachHandlers();
    }

    private void buildLayout() {
        setSpacing(12);
        setPadding(new Insets(16));
        setAlignment(Pos.TOP_LEFT);

        setStyle(
                "-fx-background-color: linear-gradient(to bottom,#0f172a,#1e293b);" +
                        "-fx-border-color: #64748b; -fx-border-radius: 8; -fx-background-radius: 8;"
        );

        currentFloorLbl.setTextFill(Color.WHITE);
        targetFloorLbl.setTextFill(Color.WHITE);
        directionLbl.setTextFill(Color.WHITE);
        movingLbl.setTextFill(Color.WHITE);
        doorStateLbl.setTextFill(Color.WHITE);
        fireModeLbl.setTextFill(Color.ORANGE);

        hallUpLbl.setTextFill(Color.LIGHTGRAY);
        hallDownLbl.setTextFill(Color.LIGHTGRAY);
        cabinLbl.setTextFill(Color.LIGHTGRAY);

        fireToggleBtn.setMaxWidth(Double.MAX_VALUE);
        clearAllBtn.setMaxWidth(Double.MAX_VALUE);

        fireToggleBtn.setStyle("-fx-background-radius: 6;");
        clearAllBtn.setStyle("-fx-background-radius: 6;");

        VBox statusBox = new VBox(6,
                currentFloorLbl,
                targetFloorLbl,
                directionLbl,
                movingLbl,
                doorStateLbl,
                fireModeLbl
        );
        statusBox.setPadding(new Insets(8));
        statusBox.setStyle("-fx-background-color: rgba(15,23,42,0.8); -fx-background-radius: 8;");

        Label pendingTitle = new Label("Pending Requests:");
        pendingTitle.setTextFill(Color.web("#e5e7eb"));
        VBox pendingBox = new VBox(6,
                pendingTitle,
                hallUpLbl,
                hallDownLbl,
                cabinLbl
        );
        pendingBox.setPadding(new Insets(8));
        pendingBox.setStyle("-fx-background-color: rgba(15,23,42,0.8); -fx-background-radius: 8;");

        Label controlsTitle = new Label("Controls:");
        controlsTitle.setTextFill(Color.web("#e5e7eb"));
        VBox controlsBox = new VBox(8,
                controlsTitle,
                fireToggleBtn,
                clearAllBtn
        );
        controlsBox.setPadding(new Insets(8));
        controlsBox.setStyle("-fx-background-color: rgba(15,23,42,0.8); -fx-background-radius: 8;");

        getChildren().addAll(statusBox, pendingBox, controlsBox);
    }

    private void attachHandlers() {
        fireToggleBtn.selectedProperty().addListener((obs, oldV, newV) -> {
            fireModeLbl.setText("Fire Mode: " + (newV ? "ON" : "OFF"));
            onFireToggled.accept(newV);
        });

        clearAllBtn.setOnAction(e -> onClearRequests.run());
    }

    // === Wiring hooks for DemoSystemApp ===

    public void setOnFireToggled(Consumer<Boolean> handler) {
        this.onFireToggled = (handler != null) ? handler : active -> {};
    }

    public void setOnClearRequests(Runnable handler) {
        this.onClearRequests = (handler != null) ? handler : () -> {};
    }

    // === CommandCenterPanelAPI implementation ===

    @Override
    public void setCurrentFloor(int floor) {
        currentFloorLbl.setText("Current: " + floor);
    }

    @Override
    public void setTargetFloor(int targetFloor, boolean hasTarget) {
        if (hasTarget) {
            targetFloorLbl.setText("Target: " + targetFloor);
        } else {
            targetFloorLbl.setText("Target: —");
        }
    }

    @Override
    public void setMoving(boolean moving) {
        movingLbl.setText("Moving: " + moving);
    }

    @Override
    public void setDirection(String direction) {
        directionLbl.setText("Direction: " + direction);
    }

    @Override
    public void setDoorState(DoorState doorState) {
        doorStateLbl.setText("Door: " + (doorState == null ? "?" : doorState.name()));
    }

    @Override
    public void setFireMode(boolean fireActive) {
        fireModeLbl.setText("Fire Mode: " + (fireActive ? "ON" : "OFF"));
        fireToggleBtn.setSelected(fireActive);
    }

    @Override
    public void setPendingHallUp(Set<Integer> floors) {
        hallUpLbl.setText("Hall Up: " + summarize(floors));
    }

    @Override
    public void setPendingHallDown(Set<Integer> floors) {
        hallDownLbl.setText("Hall Down: " + summarize(floors));
    }

    @Override
    public void setPendingCabin(Set<Integer> floors) {
        cabinLbl.setText("Cabin Sel: " + summarize(floors));
    }

    private String summarize(Set<Integer> set) {
        if (set == null || set.isEmpty()) return "[]";
        return set.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}


