package CommandCenter;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CommandCenterColors {
    public static final Color CHARADE = Color.rgb(36, 36, 47);

    public static final Color ARROWTOWN = Color.rgb(153, 142, 109);
    public static final Color GRAVEL = Color.rgb(68, 58, 69);

    // Colors for the terminal window and modes
    public static final Color TERMINAL_BACKGROUND = Color.rgb(16,14,22);
    public static final Color TERMINAL_ORANGE = Color.rgb(225,145,35);
    public static final Color TERMINAL_ORANGE_DARK = Color.rgb(82,50,25);
    public static final Color TERMINAL_BLUE = Color.rgb(35,39,225);
    public static final Color TERMINAL_BLUE_DARK = Color.rgb(25,29,82);
    public static final Color TERMINAL_RED = Color.rgb(225,35,45);
    public static final Color TERMINAL_RED_DARK = Color.rgb(82,25,25);
    public static final Color ARROW_BLUE = Color.rgb(136, 217, 169);
    public static final Color ARROW_BLUE_DARK = Color.rgb(29,38,35);


    // Colors for the background panel
    public static final Color PANEL_BACKGROUND = Color.rgb(100,92,74);
    public static final Color PANEL_DARK = Color.rgb(32,32,28);

    public final Font font = Font.loadFont(
            getClass().getResourceAsStream("/Bauhaus 93 Regular.ttf"), 24
    );

}
