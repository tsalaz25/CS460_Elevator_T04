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

public class LobbyPanel extends BorderPane implements LobbyPanelAPI {

    private boolean up = false;
    private boolean down = false;
    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

    private final Button upBtn = new Button("▲");
    private final Button downBtn = new Button("▼");
    private final ComboBox<Integer> floorDropdown = new ComboBox<>();
    private final Label title = new Label("Lobby Panel");
    private final Label display = new Label("0"); // red-on-black counter

    private Timeline travel;

    public LobbyPanel() {
        // background
        setPadding(new Insets(12));
        setBackground(new Background(new BackgroundFill(Color.web("#f5f7fc"), CornerRadii.EMPTY, Insets.EMPTY)));

        // card
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(Color.web("#dde3f1"),
                BorderStrokeStyle.SOLID, new CornerRadii(16), new BorderWidths(1))));
        card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.25, 0, 6);");

        title.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#16233f;");

        // 7-seg style display (red numbers on black)
        display.setMinWidth(120);
        display.setAlignment(Pos.CENTER);
        display.setStyle("""
                + "-fx-background-color:black;"
                + "-fx-text-fill:#ff4545;"
                + "-fx-font-family:'Consolas', 'Courier New', monospace;"
                + "-fx-font-size:40;"
                + "-fx-padding:8 14;"
                + "-fx-background-radius:8;"
                + "-fx-border-color:#111827; -fx-border-radius:8; -fx-border-width:1;"
        + """);

        // dropdown 0..10
        for (int i = 0; i <= 10; i++) floorDropdown.getItems().add(i);
        floorDropdown.getSelectionModel().select(0);
        floorDropdown.setPrefWidth(160);
        floorDropdown.setOnAction(e -> {
            Integer v = floorDropdown.getValue();
            if (v != null) {
                setTargetFloor(v);
            }
            refreshInteractivity();
        });

        HBox selectorRow = new HBox(10, new Label("Select current floor:"), floorDropdown);
        selectorRow.setAlignment(Pos.CENTER);

        // buttons
        upBtn.setPrefWidth(90);
        downBtn.setPrefWidth(90);
        upBtn.setStyle(baseButtonStyle());
        downBtn.setStyle(baseButtonStyle());

        upBtn.setOnAction(e -> onCallPressed(true));
        downBtn.setOnAction(e -> onCallPressed(false));

        HBox btnRow = new HBox(12, upBtn, downBtn);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(6));

        // assemble
        card.getChildren().addAll(title, display, selectorRow, btnRow);
        setCenter(card);

        // timeline setup
        travel = new Timeline(new KeyFrame(Duration.millis(700), e -> stepTowardTarget()));
        travel.setCycleCount(Timeline.INDEFINITE);

        applyStyles();
        refreshInteractivity();
    }

    private String baseButtonStyle() {
        return "-fx-font-size:20; -fx-font-weight:800; -fx-padding:10 12;"
             + "-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#d7deea; -fx-border-width:1;";
    }

    private void applyStyles() {
        // Up style
        if (up) {
            upBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;");
        } else {
            upBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;");
        }

        // Down style
        if (down) {
            downBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#a7f3d0; -fx-text-fill:#1b2a4e;");
        } else {
            downBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;");
        }

        // display text
        display.setText(Integer.toString(currentFloor));
    }

    private void onCallPressed(boolean isUp) {
        // Do nothing if already at target
        if (currentFloor == targetFloor) {
            refreshInteractivity();
            return;
        }

        if (isUp) {
            up = true;
        } else {
            down = true;
        }
        applyStyles();

        // Begin travel if not already moving
        if (!moving) {
            moving = true;
            travel.playFromStart();
        }

        refreshInteractivity();
    }

    private void stepTowardTarget() {
        if (currentFloor == targetFloor) {
            finishTravel();
            return;
        }
        if (currentFloor < targetFloor) {
            currentFloor++;
        } else {
            currentFloor--;
        }
        applyStyles();

        if (currentFloor == targetFloor) {
            finishTravel();
        }
    }

    private void finishTravel() {
        moving = false;
        travel.stop();
        // Dim buttons on arrival
        up = false;
        down = false;
        applyStyles();
        refreshInteractivity();
    }

    private void refreshInteractivity() {
        boolean atTarget = (currentFloor == targetFloor);
        // Buttons are disabled if already at floor, or while moving
        upBtn.setDisable(atTarget || moving);
        downBtn.setDisable(atTarget || moving);
        floorDropdown.setDisable(moving);
    }

    // ---- API ----
    @Override
    public boolean upRequested() { return up; }

    @Override
    public boolean downRequested() { return down; }

    @Override
    public void resetUpRequest() { up = false; applyStyles(); refreshInteractivity(); }

    @Override
    public void resetDownRequest() { down = false; applyStyles(); refreshInteractivity(); }

    @Override
    public void setCurrentFloor(int f) {
        currentFloor = clamp(f, 0, 10);
        applyStyles();
        refreshInteractivity();
    }

    @Override
    public int getCurrentFloor() { return currentFloor; }

    @Override
    public void setTargetFloor(int f) {
        targetFloor = clamp(f, 0, 10);
        if (floorDropdown.getValue() == null || floorDropdown.getValue() != targetFloor) {
            floorDropdown.getSelectionModel().select(targetFloor);
        }
        refreshInteractivity();
    }

    @Override
    public int getTargetFloor() { return targetFloor; }

    @Override
    public boolean isMoving() { return moving; }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
