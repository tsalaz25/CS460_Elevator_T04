package LobbyGUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import CabinGUI.DoorState;

import java.net.URL;

/**
 * Lobby panel with:
 *  - Viewing-floor dropdown (0..10)
 *  - "Floor X" label showing which floor this lobby represents
 *  - Up/Down buttons that light when pressed
 *  - Fire button (UI only; controller implements behavior)
 *
 * Integration mode (systemMode = true):
 *   - Does NOT self-move; just fires callbacks on button presses.
 *   - Controller should call setMoving(...) when elevator is moving.
 *
 * Standalone demo mode (systemMode = false):
 *   - Timeline simulates movement floor-by-floor.
 */
public class LobbyPanel extends BorderPane implements LobbyPanelAPI {
    private Runnable onUpPressed;
    private Runnable onDownPressed;
    private Runnable onFireToggled;

    private boolean systemMode = true;

    private boolean upLamp = false;
    private boolean downLamp = false;

    private int currentFloor = 0;
    private int targetFloor = 0;
    private boolean moving = false;

    private boolean fireActive = false;

    private DoorState doorState = DoorState.CLOSED;
    private final Label doorBadge = new Label("CLOSED");

    private final Button upBtn   = new Button("▲");
    private final Button downBtn = new Button("▼");
    private final Button fireBtn = new Button("FIRE");

    private final ComboBox<Integer> floorDropdown = new ComboBox<>();
    private final Label display = new Label("FLOOR 0");
    private final Label title   = new Label("Lobby Panel");

    private Image lobbyClosedImg;
    private Image lobbyHalfImg;
    private Image lobbyOpenImg;
    private ImageView lobbyDoorView;

    private final Timeline travel =
            new Timeline(new KeyFrame(Duration.millis(700), e -> stepTowardTarget()));

    private Runnable onViewingFloorChanged;

    public LobbyPanel() {
        loadDoorImages();
        buildUI();
        wireHandlers();
        travel.setCycleCount(Timeline.INDEFINITE);
        refreshInteractivity();
        applyStyles();
        applyDoorStyles();
        applyFireStyles();
    }

    public void setOnUpPressed(Runnable r) {
        this.onUpPressed = r;
    }

    public void setOnDownPressed(Runnable r) {
        this.onDownPressed = r;
    }

    public void setSystemMode(boolean v) {
        this.systemMode = v;
    }

    private void loadDoorImages() {
        lobbyClosedImg = loadImage("/resources/lobby/lobby_closed.png");
        lobbyHalfImg   = loadImage("/resources/lobby/lobby_half.png");
        lobbyOpenImg   = loadImage("/resources/lobby/lobby_open.png");
    }

    public void setOnViewingFloorChanged(Runnable r) {
        this.onViewingFloorChanged = r;
    }

    private Image loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void setDoorState(DoorState state) {
        this.doorState = state;
        applyDoorStyles();
    }

    @Override
    public void setOnFireToggled(Runnable r) {
        this.onFireToggled = r;
    }

    @Override
    public void setFireActive(boolean active) {
        this.fireActive = active;
        applyFireStyles();
        refreshInteractivity();
    }

    @Override
    public boolean isFireActive() {
        return fireActive;
    }

    @Override
    public boolean upRequested() {
        return upLamp;
    }

    @Override
    public boolean downRequested() {
        return downLamp;
    }

    @Override
    public void resetUpRequest() {
        upLamp = false;
        applyStyles();
        refreshInteractivity();
    }

    @Override
    public void resetDownRequest() {
        downLamp = false;
        applyStyles();
        refreshInteractivity();
    }

    @Override
    public int getCurrentFloor() {
        return currentFloor;
    }

    @Override
    public int getTargetFloor() {
        return targetFloor;
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setMoving(boolean m) {
        this.moving = m;
        refreshInteractivity();
    }

    private void buildUI() {
        setPadding(new Insets(12));
        setBackground(new Background(new BackgroundFill(
                Color.web("#f5f7fc"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                Color.web("#dde3f1"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(16),
                new BorderWidths(1))));
        card.setStyle(
                card.getStyle()
                        + "; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.25, 0, 6);");

        title.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#16233f;");

        display.setMinWidth(50);
        display.setAlignment(Pos.CENTER);
        display.setStyle(
                "-fx-background-color:#ecd29b;" +
                        "-fx-text-fill:#111827;" +
                        "-fx-font-family:'Consolas','Courier New',monospace;" +
                        "-fx-font-size:16;" +
                        "-fx-font-weight:800;" +
                        "-fx-padding:6 10;" +
                        "-fx-background-radius:2;" +
                        "-fx-border-color:#ecd29b; -fx-border-radius:3; -fx-border-width:1;"
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

        if (lobbyClosedImg != null) {
            lobbyDoorView = new ImageView(lobbyClosedImg);
        } else {
            lobbyDoorView = new ImageView();
        }
        lobbyDoorView.setPreserveRatio(true);
        lobbyDoorView.setFitWidth(240);

        StackPane doorStack = new StackPane();
        doorStack.getChildren().add(lobbyDoorView);

        VBox floorOverlay = new VBox(display);
        floorOverlay.setAlignment(Pos.TOP_CENTER);
        floorOverlay.setPadding(new Insets(14, 18, 0, 0));
        floorOverlay.setMouseTransparent(true);
        StackPane.setAlignment(floorOverlay, Pos.TOP_CENTER);
        doorStack.getChildren().add(floorOverlay);

        StackPane.setAlignment(fireBtn, Pos.BOTTOM_RIGHT);
        fireBtn.setTranslateX(-200);
        fireBtn.setTranslateY(-120);
        doorStack.getChildren().add(fireBtn);

        HBox doorRow = new HBox(6, new Label("Door:"), doorBadge);
        doorRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
                title,
                doorStack,
                doorRow,
                selectorRow,
                btnRow
        );

        setCenter(card);
    }

    private void wireHandlers() {
        floorDropdown.setOnAction(e -> {
            Integer v = floorDropdown.getValue();
            if (v != null) {
                targetFloor = clamp(v, 0, 10);
                display.setText("FLOOR " + v);
            }
            refreshInteractivity();

            if (onViewingFloorChanged != null) {
                onViewingFloorChanged.run();
            }
        });

        upBtn.setOnAction(e -> {
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

        fireBtn.setOnAction(e -> {
            fireActive = !fireActive;
            applyFireStyles();
            refreshInteractivity();

            if (onFireToggled != null) {
                onFireToggled.run();
            }
        });
    }

    private void triggerDemoTravel() {
        if (systemMode) return;
        if (currentFloor == targetFloor) {
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
        display.setText("FLOOR " + currentFloor);
        if (currentFloor == targetFloor) finishTravel();
    }

    private void finishTravel() {
        moving = false;
        travel.stop();
        upLamp = false;
        downLamp = false;
        applyStyles();
        refreshInteractivity();
    }

    private String baseButtonStyle() {
        return "-fx-font-size:20; -fx-font-weight:800; -fx-padding:10 12;" +
                "-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#d7deea; -fx-border-width:1;";
    }

    private void applyStyles() {
        upBtn.setStyle(
                baseButtonStyle() +
                        (upLamp
                                ? "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;"
                                : "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;"));
        downBtn.setStyle(
                baseButtonStyle() +
                        (downLamp
                                ? "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;"
                                : "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;"));
    }

    private void applyFireStyles() {
        String base =
                "-fx-font-weight:800;" +
                        "-fx-font-size:9;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:3 3;" +
                        "-fx-min-width:30;" +
                        "-fx-min-height:30;" +
                        "-fx-max-width:30;" +
                        "-fx-max-height:30;" +
                        "-fx-border-color:#030000; -fx-border-radius:3; -fx-border-width:1;";

        if (fireActive) {
            fireBtn.setText("FIRE");
            fireBtn.setStyle(
                    base +
                            "-fx-background-color:#b91c1c;" +
                            "-fx-text-fill:white;");
        } else {
            fireBtn.setText("FIRE");
            fireBtn.setStyle(
                    base +
                            "-fx-background-color:#fee2e2;" +
                            "-fx-text-fill:#7f1d1d;");
        }
    }

    private void applyDoorStyles() {
        String badgeStyle = "-fx-padding:4 10; -fx-background-radius:9999; -fx-font-weight:700;";
        Image img = null;

        switch (doorState) {
            case OPEN -> {
                doorBadge.setText("OPEN");
                doorBadge.setStyle(badgeStyle + "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;");
                img = lobbyOpenImg;
            }
            case CLOSED -> {
                doorBadge.setText("CLOSED");
                doorBadge.setStyle(badgeStyle + "-fx-background-color:#fee2e2; -fx-text-fill:#7f1d1d;");
                img = lobbyClosedImg;
            }
            case OPENING, CLOSING, OBSTRUCTED -> {
                doorBadge.setText(doorState.name());
                doorBadge.setStyle(badgeStyle + "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;");
                img = (lobbyHalfImg != null) ? lobbyHalfImg : lobbyClosedImg;
            }
        }

        if (lobbyDoorView != null && img != null) {
            lobbyDoorView.setImage(img);
        }
    }

    private void refreshInteractivity() {
        if (systemMode) {
            boolean disableCalls = moving;
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


