package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

public class CCRequestButtonComponent extends AnchorPane {
    private final Circle circle = new Circle(5);

    public CCRequestButtonComponent()
    {
        circle.setFill(CommandCenterColors.TERMINAL_ORANGE);
    }

    public void setActivated()
    {
        circle.setFill(CommandCenterColors.TERMINAL_ORANGE);
    }

    public void setDeactivated()
    {
        circle.setFill(CommandCenterColors.TERMINAL_ORANGE_DARK);
    }
}
