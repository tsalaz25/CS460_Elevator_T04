package LobbyGUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class LobbyPanel extends BorderPane implements LobbyPanelAPI {

    private boolean up = false;
    private boolean down = false;
    private int currentFloor = 1;

    private final Button upBtn = new Button("▲");
    private final Button downBtn = new Button("▼");
    private final Label title = new Label("Lobby Panel");
    private final Label floorLbl = new Label("1");

    public LobbyPanel() {
        // page background
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

        // floor chip
        Label cap1 = new Label("FLOOR");
        cap1.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11; -fx-font-weight:700;");
        floorLbl.setStyle("-fx-background-color:#eef2ff; -fx-background-radius:10; -fx-padding:4 8; -fx-font-weight:700; -fx-text-fill:#1b2a4e;");
        HBox chip = new HBox(8, cap1, floorLbl);
        chip.setAlignment(Pos.CENTER_LEFT);

        // buttons row
        upBtn.setPrefWidth(80);
        downBtn.setPrefWidth(80);
        upBtn.setStyle(baseButtonStyle());
        downBtn.setStyle(baseButtonStyle());

        upBtn.setOnAction(e -> {
            up = !up;
            applyStyles();
        });
        downBtn.setOnAction(e -> {
            down = !down;
            applyStyles();
        });

        HBox row = new HBox(12, upBtn, downBtn);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(6));

        card.getChildren().addAll(title, chip, row);
        setCenter(card);

        applyStyles();
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
            downBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#fde68a; -fx-text-fill:#1b2a4e;");
        } else {
            downBtn.setStyle(baseButtonStyle() + "; -fx-background-color:#f3f4f6; -fx-text-fill:#1f2937;");
        }
    }

    // ---- API ----
    @Override
    public boolean upRequested() { return up; }

    @Override
    public boolean downRequested() { return down; }

    @Override
    public void resetUpRequest() { up = false; applyStyles(); }

    @Override
    public void resetDownRequest() { down = false; applyStyles(); }

    @Override
    public void setCurrentFloor(int f) { currentFloor = f; floorLbl.setText(String.valueOf(f)); }

    @Override
    public int getCurrentFloor() { return currentFloor; }
}
