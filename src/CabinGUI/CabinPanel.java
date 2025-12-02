package CabinGUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Cabin panel with:
 * - Floor display overlaid on cabin image
 * - Direction + door badges
 * - Door image (closed / half / open)
 * - Grid of floor buttons
 * - NEW: Overload + Obstruction toggle buttons (image buttons)
 *
 * This panel is "dumb": the controller sets floor/direction/door state;
 * when the user selects a floor, we invoke callbacks.
 */
public class CabinPanel extends BorderPane implements CabinPanelAPI {
    private IntConsumer onFloorSelected;
    private Consumer<Boolean> onOverloadToggled;
    private Consumer<Boolean> onObstructToggled;

    private int currentFloor = 0;
    private String direction = "IDLE";
    private DoorState doorState = DoorState.CLOSED;

    private boolean fireActive = false;
    private boolean overloaded = false;
    private boolean obstructed = false;

    private final Label title = new Label("Cabin Panel");
    private final Label display = new Label("0");
    private final Label dirBadge = new Label("IDLE");
    private final Label doorBadge = new Label("CLOSED");
    private final Label safetyBadge = new Label("");

    private final GridPane floorGrid = new GridPane();

    private Image cabinClosedImg;
    private Image cabinHalfImg;
    private Image cabinOpenImg;
    private ImageView cabinView;

    private Image obstructImg;
    private Image weightImg;

    private final Button obstructBtn = new Button();
    private final Button weightBtn = new Button();

    private ImageView obstructOverlay;
    private ImageView weightOverlay;

    public CabinPanel() {
        loadDoorImages();
        loadSafetyImages();
        buildUI();
        buildFloorButtons(0, 10);
        refreshBadges();
        refreshSafetyUi();
    }

    public void setOnFloorSelected(IntConsumer c) {
        this.onFloorSelected = c;
    }

    public void setOnOverloadToggled(Consumer<Boolean> c) {
        this.onOverloadToggled = c;
    }

    public void setOnObstructToggled(Consumer<Boolean> c) {
        this.onObstructToggled = c;
    }

    @Override
    public void setFireActive(boolean active) {
        this.fireActive = active;
    }

    @Override
    public void setCurrentFloor(int f) {
        currentFloor = clamp(f, 0, 10);
        display.setText(Integer.toString(currentFloor));
    }

    @Override
    public void setDirection(String s) {
        if (s == null) s = "IDLE";
        direction = s.toUpperCase();
        refreshBadges();
    }

    @Override
    public boolean hasSelection() {
        return false;
    }

    @Override
    public int selectedFloor() {
        return 0;
    }

    @Override
    public void resetSelection() {
    }

    @Override
    public boolean emergency() {
        return false;
    }

    @Override
    public void resetEmergency() {
    }

    @Override
    public DoorState doorState() {
        return doorState;
    }

    @Override
    public void setDoorState(DoorState d) {
        if (d == null) d = DoorState.CLOSED;
        doorState = d;

        if (cabinView != null) {
            switch (doorState) {
                case OPEN -> {
                    if (cabinOpenImg != null) cabinView.setImage(cabinOpenImg);
                }
                case CLOSED -> {
                    if (cabinClosedImg != null) cabinView.setImage(cabinClosedImg);
                }
                case OPENING, CLOSING, OBSTRUCTED -> {
                    if (cabinHalfImg != null) cabinView.setImage(cabinHalfImg);
                    else if (cabinClosedImg != null) cabinView.setImage(cabinClosedImg);
                }
            }
        }

        refreshBadges();
    }

    @Override
    public boolean overloaded() {
        return overloaded;
    }

    @Override
    public void setOverloaded(boolean b) {
        if (b) obstructed = false;
        overloaded = b;
        refreshSafetyUi();
        refreshBadges();
    }

    @Override
    public boolean obstructed() {
        return obstructed;
    }

    @Override
    public void setObstructed(boolean b) {
        if (b) overloaded = false;
        obstructed = b;
        refreshSafetyUi();
        refreshBadges();
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
        card.setStyle(card.getStyle()
                + "; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.25, 0, 6);");

        title.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#16233f;");

        cabinView = (cabinClosedImg != null) ? new ImageView(cabinClosedImg) : new ImageView();
        cabinView.setPreserveRatio(true);
        cabinView.setFitWidth(260);

        display.setAlignment(Pos.CENTER);
        display.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill:#ffffff;" +
                        "-fx-font-family:'Consolas','Courier New', monospace;" +
                        "-fx-font-size:20;" +
                        "-fx-padding:0;");
        display.setMouseTransparent(true);

        obstructOverlay = (obstructImg != null) ? new ImageView(obstructImg) : new ImageView();
        obstructOverlay.setPreserveRatio(true);
        obstructOverlay.setFitWidth(260);
        obstructOverlay.setMouseTransparent(true);
        obstructOverlay.setVisible(false);

        weightOverlay = (weightImg != null) ? new ImageView(weightImg) : new ImageView();
        weightOverlay.setPreserveRatio(true);
        weightOverlay.setFitWidth(260);
        weightOverlay.setMouseTransparent(true);
        weightOverlay.setVisible(false);

        setupIconButton(obstructBtn, obstructImg);
        setupIconButton(weightBtn, weightImg);

        StackPane doorStack = new StackPane();
        doorStack.getChildren().addAll(cabinView, display, obstructOverlay, weightOverlay, obstructBtn, weightBtn);
        doorStack.setAlignment(Pos.TOP_CENTER);

        StackPane.setMargin(display, new Insets(28, 95, 0, 0));

        StackPane.setAlignment(obstructBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(obstructBtn, new Insets(0, 10, 10, 0));

        StackPane.setAlignment(weightBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(weightBtn, new Insets(0, 0, 10, 10));

        obstructBtn.setOnAction(e -> {
            boolean next = !obstructed;
            setObstructed(next);
            if (onObstructToggled != null) onObstructToggled.accept(next);
            if (next && onOverloadToggled != null) onOverloadToggled.accept(false);
        });

        weightBtn.setOnAction(e -> {
            boolean next = !overloaded;
            setOverloaded(next);
            if (onOverloadToggled != null) onOverloadToggled.accept(next);
            if (next && onObstructToggled != null) onObstructToggled.accept(false);
        });

        HBox badgeRow = new HBox(10, dirBadge, doorBadge, safetyBadge);
        badgeRow.setAlignment(Pos.CENTER);

        floorGrid.setHgap(8);
        floorGrid.setVgap(8);
        floorGrid.setAlignment(Pos.CENTER);

        card.getChildren().setAll(title, doorStack, badgeRow, floorGrid);
        setCenter(card);
    }

    private void setupIconButton(Button b, Image img) {
        b.setFocusTraversable(false);
        b.setPickOnBounds(true);

        ImageView iv = new ImageView();
        if (img != null) iv.setImage(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(26);

        b.setGraphic(iv);
        b.setText("");
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);

        b.setStyle(
                "-fx-background-radius:8;" +
                        "-fx-border-radius:8;" +
                        "-fx-border-color:#111827;" +
                        "-fx-border-width:1;" +
                        "-fx-background-color:rgba(255,255,255,0.85);");
    }

    private void refreshSafetyUi() {
        if (obstructOverlay != null) obstructOverlay.setVisible(obstructed);
        if (weightOverlay != null) weightOverlay.setVisible(overloaded);

        if (obstructed) {
            obstructBtn.setEffect(new javafx.scene.effect.DropShadow(12, Color.rgb(239, 68, 68, 0.9)));
        } else {
            obstructBtn.setEffect(null);
        }

        if (overloaded) {
            weightBtn.setEffect(new javafx.scene.effect.DropShadow(12, Color.rgb(245, 158, 11, 0.9)));
        } else {
            weightBtn.setEffect(null);
        }
    }

    private void loadDoorImages() {
        cabinClosedImg = loadImageAny("/cabin/cabin_closed.png", "/resources/cabin/cabin_closed.png");
        cabinHalfImg = loadImageAny("/cabin/cabin_half.png", "/resources/cabin/cabin_half.png");
        cabinOpenImg = loadImageAny("/cabin/cabin_open.png", "/resources/cabin/cabin_open.png");
    }

    private void loadSafetyImages() {
        obstructImg = loadImageAny("/resources/other/obstruct.png", "/other/obstruct.png");
        weightImg = loadImageAny("/resources/other/weight.png", "/other/weight.png");
    }

    private Image loadImageAny(String... paths) {
        for (String p : paths) {
            try {
                URL url = getClass().getResource(p);
                if (url != null) return new Image(url.toExternalForm());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void buildFloorButtons(int min, int max) {
        int cols = 4;
        int n = 0;
        for (int f = min; f <= max; f++) {
            Button b = new Button(Integer.toString(f));
            b.setPrefWidth(56);
            int finalF = f;
            b.setOnAction(e -> {
                if (onFloorSelected != null) onFloorSelected.accept(finalF);
            });
            b.setStyle("-fx-font-weight:700; -fx-background-radius:10;");
            floorGrid.add(b, n % cols, n / cols);
            n++;
        }
    }

    private void refreshBadges() {
        dirBadge.setText(direction);
        String dirStyle =
                "UP".equals(direction) ? "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;"
                        : "DOWN".equals(direction) ? "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;"
                        : "-fx-background-color:#e5e7eb; -fx-text-fill:#111827;";
        styleBadge(dirBadge, dirStyle);

        doorBadge.setText(doorState.name());
        String doorStyle =
                switch (doorState) {
                    case OPEN -> "-fx-background-color:#e0f2fe; -fx-text-fill:#075985;";
                    case OPENING -> "-fx-background-color:#cffafe; -fx-text-fill:#155e75;";
                    case CLOSING -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
                    case OBSTRUCTED -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
                    case CLOSED -> "-fx-background-color:#e5e7eb; -fx-text-fill:#111827;";
                };
        styleBadge(doorBadge, doorStyle);

        if (overloaded) {
            safetyBadge.setText("OVERLOAD");
            styleBadge(safetyBadge, "-fx-background-color:#ffedd5; -fx-text-fill:#9a3412;");
            safetyBadge.setVisible(true);
            safetyBadge.setManaged(true);
        } else if (obstructed) {
            safetyBadge.setText("OBSTRUCT");
            styleBadge(safetyBadge, "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;");
            safetyBadge.setVisible(true);
            safetyBadge.setManaged(true);
        } else {
            safetyBadge.setText("");
            safetyBadge.setVisible(false);
            safetyBadge.setManaged(false);
        }

        refreshSafetyUi();
    }

    private void styleBadge(Label badge, String extra) {
        badge.setAlignment(Pos.CENTER);
        badge.setStyle(
                "-fx-padding:4 10; -fx-background-radius:9999; " +
                        "-fx-font-weight:700; -fx-border-radius:9999; " +
                        "-fx-border-color:#e5e7eb; -fx-border-width:1;" +
                        extra);
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}






