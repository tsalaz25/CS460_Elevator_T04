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
import java.util.function.IntConsumer;

/**
 * Cabin panel with:
 * - Red-on-black floor display
 * - Direction indicator
 * - Door state indicator
 * - Door image (closed / half / open)
 * - Grid of floor buttons that emit a selection callback
 *
 * This panel is "dumb": the controller sets floor/direction/door state;
 * when the user selects a floor, we invoke onFloorSelected.
 */
public class CabinPanel extends BorderPane implements CabinPanelAPI {

    // ----- Integration hook -----
    private IntConsumer onFloorSelected;

    // ----- UI state -----
    private int currentFloor = 0;
    private String direction = "IDLE";
    private DoorState doorState = DoorState.CLOSED;

    // ----- Controls -----
    private final Label title    = new Label("Cabin Panel");
    private final Label display  = new Label("0");
    private final Label dirBadge = new Label("IDLE");
    private final Label doorBadge = new Label("CLOSED");
    private final GridPane floorGrid = new GridPane();

    // ----- Door images -----
    private Image cabinClosedImg;
    private Image cabinHalfImg;
    private Image cabinOpenImg;
    private ImageView cabinView;

    public CabinPanel() {
        loadDoorImages();
        buildUI();
        buildFloorButtons(0, 10);
        refreshBadges();
    }

    // ============================================================
    // Public integration helper
    // ============================================================
    public void setOnFloorSelected(IntConsumer c) {
        this.onFloorSelected = c;
    }

    // ============================================================
    // CabinPanelAPI
    // ============================================================
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
        // This panel just forwards clicks immediately; no "pending selection"
        return false;
    }

    @Override
    public int selectedFloor() {
        // Not used in current design
        return 0;
    }

    @Override
    public void resetSelection() {
        // No internal selection state to reset
    }

    @Override
    public boolean emergency() {
        // No emergency button implemented here
        return false;
    }

    @Override
    public void resetEmergency() {
        // No emergency state to reset
    }

    @Override
    public DoorState doorState() {
        return doorState;
    }

    @Override
    public void setDoorState(DoorState d) {
        if (d == null) d = DoorState.CLOSED;
        doorState = d;

        // Update door image based on state
        if (cabinView != null) {
            switch (doorState) {
                case OPEN -> {
                    if (cabinOpenImg != null) {
                        cabinView.setImage(cabinOpenImg);
                    }
                }
                case CLOSED -> {
                    if (cabinClosedImg != null) {
                        cabinView.setImage(cabinClosedImg);
                    }
                }
                case OPENING, CLOSING, OBSTRUCTED -> {
                    // Use intermediate image when moving / obstructed
                    if (cabinHalfImg != null) {
                        cabinView.setImage(cabinHalfImg);
                    } else if (cabinClosedImg != null) {
                        cabinView.setImage(cabinClosedImg);
                    }
                }
            }
        }

        refreshBadges();
    }

    @Override
    public boolean overloaded() {
        return false;
    }

    @Override
    public void setOverloaded(boolean b) {
        // optional: style badge or add another badge if you track overload
        // no-op for now
    }

    @Override
    public boolean obstructed() {
        return doorState == DoorState.OBSTRUCTED;
    }

    @Override
    public void setObstructed(boolean b) {
        if (b) {
            setDoorState(DoorState.OBSTRUCTED);
        }
    }

    // ============================================================
    // UI setup
    // ============================================================
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

        // Door image view
        if (cabinClosedImg != null) {
            cabinView = new ImageView(cabinClosedImg);
        } else {
            cabinView = new ImageView();
        }
        cabinView.setPreserveRatio(true);
        cabinView.setFitWidth(260); // tweak as needed

        display.setMinWidth(120);
        display.setAlignment(Pos.CENTER);
        display.setStyle(
                "-fx-background-color:black;" +
                        "-fx-text-fill:#ff4545;" +
                        "-fx-font-family:'Consolas','Courier New',monospace;" +
                        "-fx-font-size:40;" +
                        "-fx-padding:8 14;" +
                        "-fx-background-radius:8;" +
                        "-fx-border-color:#111827; -fx-border-radius:8; -fx-border-width:1;"
        );

        HBox badgeRow = new HBox(10, dirBadge, doorBadge);
        badgeRow.setAlignment(Pos.CENTER);

        floorGrid.setHgap(8);
        floorGrid.setVgap(8);
        floorGrid.setAlignment(Pos.CENTER);

        // Compose: title → door image → display → badges → buttons
        card.getChildren().addAll(title, cabinView, display, badgeRow, floorGrid);
        setCenter(card);
    }

    private void loadDoorImages() {
        cabinClosedImg = loadImage("/cabin/cabin_closed.png");
        cabinHalfImg   = loadImage("/cabin/cabin_half.png");
        cabinOpenImg   = loadImage("/cabin/cabin_open.png");
    }

    private Image loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {
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
                "UP".equals(direction)   ? "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;"
                        : "DOWN".equals(direction) ? "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;"
                        : "-fx-background-color:#e5e7eb; -fx-text-fill:#111827;";
        styleBadge(dirBadge, dirStyle);

        doorBadge.setText(doorState.name());
        String doorStyle =
                switch (doorState) {
                    case OPEN      -> "-fx-background-color:#e0f2fe; -fx-text-fill:#075985;";
                    case OPENING   -> "-fx-background-color:#cffafe; -fx-text-fill:#155e75;";
                    case CLOSING   -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
                    case OBSTRUCTED-> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
                    case CLOSED    -> "-fx-background-color:#e5e7eb; -fx-text-fill:#111827;";
                };
        styleBadge(doorBadge, doorStyle);
    }

    private void styleBadge(Label badge, String extra) {
        badge.setAlignment(Pos.CENTER);
        badge.setStyle(
                "-fx-padding:4 10; -fx-background-radius:9999; " +
                        "-fx-font-weight:700; -fx-border-radius:9999; " +
                        "-fx-border-color:#e5e7eb; -fx-border-width:1;" +
                        extra
        );
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}




