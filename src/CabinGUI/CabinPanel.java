package CabinGUI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;

public class CabinPanel extends BorderPane implements CabinPanelAPI {

    // --- UI widgets ---
    private ImageView previewView;          // open/closed image
    private final Label doorBadge = new Label("DOORS CLOSED");
    private final Rectangle doorLamp = new Rectangle(12, 12);
    private final ComboBox<Integer> floorDropdown = new ComboBox<>();
    private final Button clearSel = new Button("Reset");
    private final ToggleButton emergency = new ToggleButton("EMERGENCY");
    private final Label floorLbl = new Label("1");
    private final Label dirLbl   = new Label("—");
    private final Label banner   = new Label(""); // small status line

    // --- API state ---
    private boolean hasSel = false;
    private int sel = 1;
    private DoorState door = DoorState.CLOSED;
    private boolean overload = false;
    private boolean obstacle = false;

    // --- images ---
    private Image imgOpen;
    private Image imgClosed;

    public CabinPanel() {
        imgOpen   = tryLoad("src/CabinGUI/img/elevator-open.png",   "src/ui/img/elevator-open.png");
        imgClosed = tryLoad("src/CabinGUI/img/elevator-closed.png", "src/ui/img/elevator-closed.png");

        // page bg
        setPadding(new Insets(12));
        setBackground(new Background(new BackgroundFill(Color.web("#f5f7fc"), CornerRadii.EMPTY, Insets.EMPTY)));

        // ----- single "card" -----
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(Color.web("#dde3f1"),
                BorderStrokeStyle.SOLID, new CornerRadii(16), new BorderWidths(1))));
        card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.14), 18, 0.25, 0, 6);");

        Label title = new Label("Cabin Panel");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#16233f;");

        // framed preview
        StackPane preview = new StackPane();
        preview.setMaxSize(330, 390);
        Region frame = new Region();
        frame.setPrefSize(310, 370);
        frame.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-border-color:#d7deea; -fx-border-radius:14;");
        Node inside;
        if (imgOpen != null && imgClosed != null) {
            previewView = new ImageView();
            previewView.setFitWidth(300); previewView.setFitHeight(360);
            previewView.setPreserveRatio(true); previewView.setSmooth(true);
            updatePreviewImage();
            inside = previewView;
        } else {
            Label ph = new Label("Add images:\nsrc/CabinGUI/img/\n  elevator-open.png\n  elevator-closed.png");
            ph.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12; -fx-alignment:center;");
            inside = ph;
        }
        preview.getChildren().addAll(frame, inside);
        preview.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:14; -fx-padding:8; "
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 12, 0.2, 0, 4);");

        // door status row
        doorLamp.setArcWidth(999); doorLamp.setArcHeight(999);
        doorLamp.setStroke(Color.web("#0f172a"));
        HBox doorRow = new HBox(8, doorLamp, doorBadge);
        doorRow.setAlignment(Pos.CENTER_LEFT);

        // small chips row (floor/direction)
        Label cap1 = new Label("FLOOR");      cap1.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11; -fx-font-weight:700;");
        floorLbl.setStyle("-fx-background-color:#eef2ff; -fx-background-radius:10; -fx-padding:4 8; -fx-font-weight:700; -fx-text-fill:#1b2a4e;");
        Label cap2 = new Label("DIRECTION");  cap2.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11; -fx-font-weight:700;");
        dirLbl.setStyle("-fx-background-color:#eef2ff; -fx-background-radius:10; -fx-padding:4 8; -fx-font-weight:700; -fx-text-fill:#1b2a4e;");
        HBox chips = new HBox(10, cap1, floorLbl, cap2, dirLbl);
        chips.setAlignment(Pos.CENTER_LEFT);

        // destination row
        for (int i = 1; i <= 10; i++) floorDropdown.getItems().add(i);
        floorDropdown.setPrefWidth(140);
        floorDropdown.setPromptText("Select floor");
        floorDropdown.setOnAction(e -> {
            Integer f = floorDropdown.getValue();
            if (f != null) { sel = f; hasSel = true; banner.setText("Selected: " + f); }
        });
        clearSel.setOnAction(e -> resetSelection());
        clearSel.setStyle("-fx-background-color:#f3f4f6; -fx-text-fill:#1f2937; -fx-font-weight:700;"
                + "-fx-background-radius:10; -fx-padding:6 12; -fx-border-color:#e5e7eb; -fx-border-radius:10;");
        HBox dest = new HBox(10, new Label("Destination:"), floorDropdown, clearSel);
        ((Label)dest.getChildren().get(0)).setStyle("-fx-font-weight:800; -fx-text-fill:#16233f;");
        dest.setAlignment(Pos.CENTER_LEFT);

        // emergency button
        emergency.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#9f1239; -fx-font-weight:800;"
                + "-fx-background-radius:999; -fx-padding:6 14; -fx-border-color:#fecaca; -fx-border-radius:999;");
        emergency.setOnAction(e -> banner.setText(emergency.isSelected() ? "HELP REQUESTED" : ""));

        // hint/status
        banner.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11;");

        // assemble
        card.getChildren().addAll(title, preview, doorRow, chips, dest, emergency, banner);
        setCenter(card);

        applyDoorVisuals(); // set lamp + badge + image
    }

    // ---- helpers ----
    private Image tryLoad(String... paths) {
        for (String p : paths) {
            try {
                File f = new File(p);
                if (f.exists()) return new Image(f.toURI().toString(), 300, 360, true, true, true);
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void updatePreviewImage() {
        if (previewView == null) return;
        if (door == DoorState.OPEN && imgOpen != null) previewView.setImage(imgOpen);
        else if (imgClosed != null) previewView.setImage(imgClosed);
    }

    private void applyDoorVisuals() {
        switch (door) {
            case OPEN -> doorLamp.setFill(Color.LIMEGREEN);
            case CLOSED -> doorLamp.setFill(Color.RED);
            default -> doorLamp.setFill(Color.GOLD);
        }
        doorBadge.setText(door == DoorState.OPEN ? "DOORS OPEN" : "DOORS CLOSED");
        String bg = (door == DoorState.OPEN) ? "#40a652" : "#d33f3f";
        doorBadge.setStyle("-fx-background-radius:999; -fx-padding:4 12; -fx-text-fill:white; -fx-font-weight:800; -fx-background-color:"+bg+";");
        updatePreviewImage();
    }

    // ---- API ----
    @Override public boolean hasSelection() { return hasSel; }
    @Override public int selectedFloor()   { return sel; }
    @Override public void resetSelection() { hasSel = false; floorDropdown.getSelectionModel().clearSelection(); banner.setText(""); }
    @Override public boolean emergency()   { return emergency.isSelected(); }
    @Override public void resetEmergency() { emergency.setSelected(false); }
    @Override public DoorState doorState() { return door; }
    @Override public void setDoorState(DoorState s) { door = s; applyDoorVisuals(); }
    @Override public boolean overloaded()  { return overload; }
    @Override public void setOverloaded(boolean v) { overload = v; banner.setText(v ? "OVERLOAD" : ""); }
    @Override public boolean obstructed()  { return obstacle; }
    @Override public void setObstructed(boolean v) { obstacle = v; banner.setText(v ? "OBSTRUCTED" : ""); }
    @Override public void setCurrentFloor(int f) { floorLbl.setText(String.valueOf(f)); }
    @Override public void setDirection(String s) { dirLbl.setText(s); }
}