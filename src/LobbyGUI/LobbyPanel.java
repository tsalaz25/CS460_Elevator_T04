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

/**
 * Lobby panel with:
 * - Target floor dropdown (0..10)
 * - Red-on-black floor display
 * - Up/Down buttons that light when pressed
 *
 * Integration mode (systemMode=true):
 *   - Does NOT self-move; just fires callbacks on button presses
 *   - Controller should call setCurrentFloor/setTargetFloor/resetUp/Down
 *
 * Standalone demo mode (systemMode=false):
 *   - A small Timeline simulates travel one floor per tick
 */
public class LobbyPanel extends BorderPane implements LobbyPanelAPI {

    // ----- Integration hooks -----
    private Runnable onUpPressed;
    private Runnable onDownPressed;
    private boolean systemMode = true;  // true => no internal travel; UI is "dumb"

    // ----- UI state -----
    private boolean upLamp = false;
    private boolean downLamp = false;
    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

    // ----- Controls -----
    private final Button upBtn = new Button("▲");
    private final Button downBtn = new Button("▼");
    private final ComboBox<Integer> floorDropdown = new ComboBox<>();
    private final Label display = new Label("Floor 0");
    private final Label title = new Label("Lobby Panel");

    // ----- Standalone demo travel -----
    private final Timeline travel = new Timeline(new KeyFrame(Duration.millis(700), e -> stepTowardTarget()));

    public LobbyPanel() {
        buildUI();
        wireHandlers();
        travel.setCycleCount(Timeline.INDEFINITE);
        refreshInteractivity();
        applyStyles();
    }

    // ============================================================
    // Public integration helpers
    // ============================================================
    public void setOnUpPressed(Runnable r)   { this.onUpPressed = r; }
    public void setOnDownPressed(Runnable r) { this.onDownPressed = r; }
    public void setSystemMode(boolean v)     { this.systemMode = v; }

    // ============================================================
    // LobbyPanelAPI
    // ============================================================
    @Override public boolean upRequested()   { return upLamp; }
    @Override public boolean downRequested() { return downLamp; }

    @Override public void resetUpRequest()   { upLamp = false; applyStyles(); refreshInteractivity(); }
    @Override public void resetDownRequest() { downLamp = false; applyStyles(); refreshInteractivity(); }
    @Override public int getCurrentFloor() { return currentFloor; }
    @Override public int getTargetFloor() { return targetFloor; }

    @Override public boolean isMoving() { return moving; }

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

        card.getChildren().addAll(title, display, selectorRow, btnRow);
        setCenter(card);
    }

    private void wireHandlers() {
        floorDropdown.setOnAction(e -> {
            Integer v = floorDropdown.getValue();
            if (v != null) {
                targetFloor = clamp(v, 0, 10); // this now means “floor I am on”
                display.setText("Floor " + v);
            }
            refreshInteractivity();
        });

        upBtn.setOnAction(e -> {
            // light the lamp
            upLamp = true; downLamp = false;
            applyStyles();

            if (systemMode) {
                if (onUpPressed != null) onUpPressed.run();
            } else {
                // standalone demo travel
                triggerDemoTravel();
            }
            refreshInteractivity();
        });

        downBtn.setOnAction(e -> {
            downLamp = true; upLamp = false;
            applyStyles();

            if (systemMode) {
                if (onDownPressed != null) onDownPressed.run();
            } else {
                triggerDemoTravel();
            }
            refreshInteractivity();
        });
    }

    // ============================================================
    // Standalone demo travel (disabled in system mode)
    // ============================================================
    private void triggerDemoTravel() {
        if (currentFloor == targetFloor) {
            // no-op arrival behavior
            upLamp = false; downLamp = false;
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
        display.setText(Integer.toString(currentFloor));
        if (currentFloor == targetFloor) finishTravel();
    }

    private void finishTravel() {
        moving = false;
        travel.stop();
        upLamp = false; downLamp = false; // dim both on arrival
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
                (downLamp ? "; -fx-background-color:#a7f3d0; -fx-text-fill:#1b2a4e;"
                        : "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;"));
        display.setText(Integer.toString(currentFloor));
    }

    private void refreshInteractivity() {
        boolean atTarget = (currentFloor == targetFloor);
        upBtn.setDisable(moving || atTarget);
        downBtn.setDisable(moving || atTarget);
        floorDropdown.setDisable(moving);
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

