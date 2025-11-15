package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint; // Added import

// Reverted to the version provided by the user, keeping debug prints
public class CCCallButtonComponent extends AnchorPane {
    private final Triangle up = new Triangle();
    private final Triangle down = new Triangle(15, 0); // User provided version had this offset

    public CCCallButtonComponent() {
        this.up.setFill(CommandCenterColors.ARROW_BLUE_DARK);
        this.down.setFill(CommandCenterColors.ARROW_BLUE_DARK);
        this.down.setScaleY(-2);
        this.getChildren().addAll(this.up, this.down);
        this.setMinSize(30, 15);
        this.setMaxSize(30, 15);

        // Debugging Background (Optional)
        // this.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
    }

    public void setGoingUp() {
//        System.out.println("  ButtonComponent: setGoingUp() called."); // Kept Debug Print
        Paint upColor = CommandCenterColors.ARROW_BLUE;
        Paint downColor = CommandCenterColors.ARROW_BLUE_DARK;
        // System.out.println("  ButtonComponent: Setting UP color: " + (upColor != null ? upColor.toString() : "NULL")); // Optional debug
        this.down.setFill(downColor);
        this.up.setFill(upColor);
    }

    public void setGoingDown() {
//        System.out.println("  ButtonComponent: setGoingDown() called."); // Kept Debug Print
        Paint upColor = CommandCenterColors.ARROW_BLUE_DARK;
        Paint downColor = CommandCenterColors.ARROW_BLUE;
        // System.out.println("  ButtonComponent: Setting DOWN color: " + (downColor != null ? downColor.toString() : "NULL")); // Optional debug
        this.up.setFill(upColor);
        this.down.setFill(downColor);
    }

    public void setGoingBoth() {
//        System.out.println("  ButtonComponent: setGoingBoth() called."); // Kept Debug Print
        // System.out.println("  ButtonComponent: Setting DOWN color: " + (downColor != null ? downColor.toString() : "NULL")); // Optional debug
        this.up.setFill(CommandCenterColors.ARROW_BLUE);
        this.down.setFill(CommandCenterColors.ARROW_BLUE);
    }

    public void setStayingStill() {
//        System.out.println("  ButtonComponent: setStayingStill() called."); // Kept Debug Print
        Paint color = CommandCenterColors.ARROW_BLUE_DARK;
        this.up.setFill(color);
        this.down.setFill(color);
    }

}

