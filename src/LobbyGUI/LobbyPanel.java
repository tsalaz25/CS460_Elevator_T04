package LobbyGUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import CabinGUI.DoorState;

/**
 * Lobby panel with:
 *  - Viewing-floor dropdown (0..10)
 *  - "Floor X" label showing which floor this lobby represents
 *  - Up/Down buttons that light when pressed
 *  - Fire button (UI only; controller will implement behavior)
 *
 * Integration mode (systemMode = true):
 *   - Does NOT self-move; just fires callbacks on button presses.
 *   - Controller should call setMoving(...) when elevator is moving.
 *
 * Standalone demo mode (systemMode = false):
 *   - Timeline simulates movement floor-by-floor.
 *
 */
public class LobbyPanel extends BorderPane implements LobbyPanelAPI {

    // ------------------------------------------------------------
    // Integration callbacks
    // ------------------------------------------------------------
    private Runnable onUpPressed;
    private Runnable onDownPressed;
    private Runnable onFireToggled;  // TODO DANIEL: used to notify controller

    private boolean systemMode = true;  // true => no internal travel; UI is "dumb"

    // ------------------------------------------------------------
    // UI state
    // ------------------------------------------------------------
    private boolean upLamp = false;
    private boolean downLamp = false;

    private int currentFloor = 0;   // only used by demo mode
    private int targetFloor = 0;    // "viewing floor" in system mode / demo target in demo mode
    private boolean moving = false; // used for disabling controls

    private boolean fireActive = false;  // TODO DANIEL: track fire UI toggle

    private DoorState doorState = DoorState.CLOSED;
    private final Label doorBadge = new Label("CLOSED");

    // ------------------------------------------------------------
    // Controls
    // ------------------------------------------------------------
    private final Button upBtn   = new Button("▲");
    private final Button downBtn = new Button("▼");
    private final Button fireBtn = new Button("FIRE"); // TODO DANIEL: style and label ON/OFF

    private final ComboBox<Integer> floorDropdown = new ComboBox<>();
    private final Label display = new Label("Floor 0");
    private final Label title   = new Label("Lobby Panel");

    // ------------------------------------------------------------
    // Standalone demo travel
    // ------------------------------------------------------------
    private final Timeline travel =
            new Timeline(new KeyFrame(Duration.millis(700), e -> stepTowardTarget()));

    // ============================================================
    // Constructor
    // ============================================================
    public LobbyPanel() {
        buildUI();
        wireHandlers();
        travel.setCycleCount(Timeline.INDEFINITE);
        refreshInteractivity();
        applyStyles();
        applyDoorStyles();  //Show Correct Images
        applyFireStyles();  // ensure fire button starts in the correct visual state
    }

    // ============================================================
    // Public integration helpers
    // ============================================================
    public void setOnUpPressed(Runnable r)   { this.onUpPressed = r; }
    public void setOnDownPressed(Runnable r) { this.onDownPressed = r; }
    public void setSystemMode(boolean v)     { this.systemMode = v; }

    @Override
    public void setDoorState(DoorState state){
        this doorState = state;
        applyDoorStyles();
    }

    // Fire callbacks for controller wiring
    @Override
    public void setOnFireToggled(Runnable r) {
        // Store the callback so the fire button can notify the controller.
        this.onFireToggled = r;
    }

    @Override
    public void setFireActive(boolean active) {
        // Update local fire flag and make the UI match.
        this.fireActive = active;
        applyFireStyles();
        refreshInteractivity();
    }

    @Override
    public boolean isFireActive() {
        // Reflect the current UI fire state.
        return fireActive;
    }

    // ============================================================
    // LobbyPanelAPI core
    // ============================================================
    @Override public boolean upRequested()   { return upLamp; }
    @Override public boolean downRequested() { return downLamp; }

    @Override public void resetUpRequest() {
        upLamp = false;
        applyStyles();
        refreshInteractivity();
    }

    @Override public void resetDownRequest() {
        downLamp = false;
        applyStyles();
        refreshInteractivity();
    }

    @Override public int getCurrentFloor() { return currentFloor; }

    // In system mode, this means: "which floor is this lobby representing?"
    @Override public int getTargetFloor() { return targetFloor; }

    @Override public boolean isMoving() { return moving; }

    @Override public void setMoving(boolean m) {
        this.moving = m;
        refreshInteractivity();
    }

    // ============================================================
    // UI setup
    // ============================================================
    private void buildUI() {
        setPadding(new Insets(12));
        setBackground(new Background(new BackgroundFill(Color.web("#f5f7fc"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(Color.web("#dde3f1"),
                BorderStrokeStyle.SOLID, new CornerRadii(16), new BorderWidths(1))));
        card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.25, 0, 6);");

        title.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#16233f;");

        display.setMinWidth(140);
        display.setAlignment(Pos.CENTER);
        display.setStyle(
                "-fx-background-color:white;" +
                        "-fx-text-fill:#1f2937;" +
                        "-fx-font-family:'Segoe UI', sans-serif;" +
                        "-fx-font-size:24;" +
                        "-fx-padding:10 14;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:#d1d5db; -fx-border-radius:10; -fx-border-width:1;"
        );

        for (int i = 0; i <= 10; i++) floorDropdown.getItems().add(i);
        floorDropdown.getSelectionModel().select(0);
        floorDropdown.setPrefWidth(160);

        HBox selectorRow = new HBox(10, new Label("Viewing floor:"), floorDropdown);
        selectorRow.setAlignment(Pos.CENTER);

        upBtn.setPrefWidth(90);
        downBtn.setPrefWidth(90);

        HBox btnRow = new HBox(12, upBtn, downBtn);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(6));
        // TODO DANIEL:
        // Style fireBtn and decide where it goes visually.
        fireBtn.setPrefWidth(160);

        VBox controls = new VBox(8, selectorRow, btnRow, fireBtn);
        controls.setAlignment(Pos.CENTER);
        HBox doorRow = new HBox(6, new Label("Door:"), doorBadge);
        doorRow.setAlignment(Pos.CENTER);
        card.getChildren().addAll(title, display, controls, doorRow);


        card.getChildren().addAll(title, display, controls);
        setCenter(card);
    }

    private void wireHandlers() {
        floorDropdown.setOnAction(e -> {
            Integer v = floorDropdown.getValue();
            if (v != null) {
                targetFloor = clamp(v, 0, 10); // in system mode: "floor I am on"
                display.setText("Floor " + v);
            }
            refreshInteractivity();
        });

        upBtn.setOnAction(e -> {
            // light the lamp
            upLamp = true;
            downLamp = false;
            applyStyles();

            if (systemMode) {
                if (onUpPressed != null) onUpPressed.run();
            } else {
                triggerDemoTravel();
            }
            refreshInteractivity();
        });

        downBtn.setOnAction(e -> {
            downLamp = true;
            upLamp = false;
            applyStyles();

            if (systemMode) {
                if (onDownPressed != null) onDownPressed.run();
            } else {
                triggerDemoTravel();
            }
            refreshInteractivity();
        });

        // TODO DANIEL:
        // Hook up fire button to toggle fireActive and notify controller via onFireToggled.
        //
        fireBtn.setOnAction(e -> {
            // 1) Toggle local UI state
            fireActive = !fireActive;

            // 2) Update button look + interactivity
            applyFireStyles();
            refreshInteractivity();

            // 3) Notify controller (DemoSystemApp will publish UI_FIRE_TOGGLED)
            if (onFireToggled != null) {
                onFireToggled.run();
            }
        });
    }

    // ============================================================
    // Standalone demo travel (disabled in system mode)
    // ============================================================
    private void triggerDemoTravel() {
        if (currentFloor == targetFloor) {
            // no-op arrival behavior
            upLamp = false;
            downLamp = false;
            applyStyles();
            refreshInteractivity();
            return;
        }
        if (!moving) {
            moving = true;
            travel.playFromStart();
        }
    }

    private void stepTowardTarget() {
        if (currentFloor == targetFloor) {
            finishTravel();
            return;
        }
        currentFloor += (targetFloor > currentFloor) ? 1 : -1;
        display.setText("Floor " + currentFloor);
        if (currentFloor == targetFloor) finishTravel();
    }

    private void finishTravel() {
        moving = false;
        travel.stop();
        upLamp = false;
        downLamp = false; // dim both on arrival
        applyStyles();
        refreshInteractivity();
    }

    // ============================================================
    // Visuals & interactivity
    // ============================================================
    private String baseButtonStyle() {
        return "-fx-font-size:20; -fx-font-weight:800; -fx-padding:10 12;" +
                "-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#d7deea; -fx-border-width:1;";
    }

    private void applyStyles() {
        upBtn.setStyle(baseButtonStyle() +
                (upLamp ? "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;"
                        : "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;"));
        downBtn.setStyle(baseButtonStyle() +
                (downLamp ? "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;"
                        : "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;"));
        // display text is managed when changing floors, not here
    }

    private void applyFireStyles() {
        if (fireActive) {
            fireBtn.setText("FIRE ON");
            fireBtn.setStyle(
                    "-fx-font-weight:800;"
                            + "-fx-background-color:#b91c1c;"
                            + "-fx-text-fill:white;"
                            + "-fx-background-radius:9999;"
                            + "-fx-padding:6 16;"
            );
        } else {
            fireBtn.setText("FIRE OFF");
            fireBtn.setStyle(
                    "-fx-font-weight:600;"
                            + "-fx-background-color:#fee2e2;"
                            + "-fx-text-fill:#7f1d1d;"
                            + "-fx-background-radius:9999;"
                            + "-fx-padding:6 16;"
            );
        }
    }

    private void applyDoorStyles() {
        String badgeStyle = "-fx-padding:4 10; -fx-background-radius:9999; -fx-font-weight:700;";
        if (doorState == DoorState.OPEN) {
            doorBadge.setText("OPEN");
            doorBadge.setStyle(badgeStyle + "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;");
        } else {
            doorBadge.setText("CLOSED");
            doorBadge.setStyle(badgeStyle + "-fx-background-color:#fee2e2; -fx-text-fill:#7f1d1d;");
        }

    }

    private void refreshInteractivity() {
        // Only disable controls while the elevator is moving in system mode
        if (systemMode) {
            boolean disableCalls = moving || fireActive;
            upBtn.setDisable(disableCalls);
            downBtn.setDisable(disableCalls);
            floorDropdown.setDisable(moving);
        } else {
            upBtn.setDisable(false);
            downBtn.setDisable(false);
            floorDropdown.setDisable(false);
        }
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
