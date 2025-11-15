package CommandCenter.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class CCCallButtonList extends VBox {
    private CCCallButtonComponent[] callButtons = new CCCallButtonComponent[10];

    public CCCallButtonList() {
        this.setSpacing(60);
        // Optional: Keep alignment/padding
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(30, 5, 30, 5));

        for (int i = 0; i < 10; i++) {
            this.callButtons[i] = new CCCallButtonComponent();
            this.getChildren().addLast(this.callButtons[i]);
        }
    }

    // Floor number 1-10
    public void setFloorCallUp(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for setFloorCallUp: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Setting UP for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            this.callButtons[index].setGoingUp();
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }

    // Floor number 1-10
    public void setFloorCallDown(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for setFloorCallDown: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Setting DOWN for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            this.callButtons[index].setGoingDown();
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }

    // Floor number 1-10
    public void setFloorCallBoth(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for setFloorCallDown: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Setting DOWN for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            this.callButtons[index].setGoingBoth();
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }

    // --- Added Clear Methods ---
    public void clearFloorCallUp(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for clearFloorCallUp: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Clearing UP for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            // Assuming setStayingStill turns off both lights
            this.callButtons[index].setStayingStill();
            // Or, if you want to only turn off the UP light:
            // this.callButtons[index].up.setFill(CommandCenterColors.ARROW_BLUE_DARK); // Needs up to be accessible or have specific method
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }

    public void clearFloorCallDown(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for clearFloorCallDown: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Clearing DOWN for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            // Assuming setStayingStill turns off both lights
            this.callButtons[index].setStayingStill();
            // Or, if you want to only turn off the DOWN light:
            // this.callButtons[index].down.setFill(CommandCenterColors.ARROW_BLUE_DARK); // Needs down to be accessible or have specific method
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }
    public void clearFloorCall(int floor) {
        if (floor < 1 || floor > 10) {
            System.out.println("CallList: Invalid floor for clearFloorCall: " + floor);
            return;
        }
        int index = 10 - floor;  // Invert for visual layout (floor 10 at top, floor 1 at bottom)
//        System.out.println("CallList: Clearing for Floor " + floor + " (using index " + index + ")");
        if (index >= 0 && index < callButtons.length && callButtons[index] != null) {
            // Assuming setStayingStill turns off both lights
            this.callButtons[index].setStayingStill();
            // Or, if you want to only turn off the DOWN light:
            // this.callButtons[index].down.setFill(CommandCenterColors.ARROW_BLUE_DARK); // Needs down to be accessible or have specific method
        } else {
            System.out.println("CallList: Error accessing callButtons[" + index + "]");
        }
    }
    // --- End Added Clear Methods ---


    public void clearAllCalls() {
//        System.out.println("CallList: Clearing all calls");
        for (int i = 0; i < callButtons.length; i++) {
            if (callButtons[i] != null) {
                callButtons[i].setStayingStill();
            }
        }
    }
}

