package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class CCLowerPanelComponent extends GridPane {

    private static int NUM_ELEVATORS;

    public ImageView[] enabledButtonsImageViews;
    public ImageView[] fireKeyImageViews;

    private Image circleButtonOnImage;
    private Image circleButtonOffImage;
    private Image squareButtonOnImage;
    private Image squareButtonOffImage;

    public CCLowerPanelComponent(int elevatorNum)
    {

        this.loadImages();

        this.NUM_ELEVATORS = elevatorNum;

        enabledButtonsImageViews = new ImageView[NUM_ELEVATORS];
        fireKeyImageViews = new ImageView[NUM_ELEVATORS];

        // create lower panel
        this.setHgap(10);
        this.setVgap(5);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(5, 0, 5, 5));

        Label enabledLabel = new Label("Enabled");
        enabledLabel.setTextFill(Color.WHITE);
        this.add(enabledLabel, 0, 0);

        for (int i = 0; i < NUM_ELEVATORS; i++) {
            ImageView enabledButton = new ImageView(this.circleButtonOnImage);
            enabledButton.setScaleX(4);
            enabledButton.setScaleY(4);
            enabledButton.setPreserveRatio(true);
            enabledButton.setFitWidth(40);

            this.enabledButtonsImageViews[i] = enabledButton;
            this.add(enabledButton, i + 1, 0);
        }

        Label fireKeyLabel = new Label("Fire Key");
        fireKeyLabel.setTextFill(Color.WHITE);
        this.add(fireKeyLabel, 0, 1);

        for (int i = 0; i < NUM_ELEVATORS; i++) {
            ImageView fireKeyIndicator = new ImageView(squareButtonOffImage);
            fireKeyIndicator.setScaleX(4);
            fireKeyIndicator.setScaleY(4);
            fireKeyIndicator.setPreserveRatio(true);
            fireKeyIndicator.setFitWidth(40);
            fireKeyIndicator.setTranslateY(10);
            this.fireKeyImageViews[i] = fireKeyIndicator;
            this.add(fireKeyIndicator, i + 1, 1);
        }
        this.setMinHeight(90);
        this.setBackground(Background.fill(CommandCenterColors.PANEL_DARK));
    }

    private void loadImages() {
        try {
            circleButtonOnImage = new Image(getClass().getResource("/resources/Circle_Button_On.png").toExternalForm());
            circleButtonOffImage = new Image(getClass().getResource("/resources/Circle_Button_Off.png").toExternalForm());
            squareButtonOnImage = new Image(getClass().getResource("/resources/Square_Button_On.png").toExternalForm());
            squareButtonOffImage = new Image(getClass().getResource("/resources/Square_Button_Off.png").toExternalForm());
        } catch (Exception e) {
            System.err.println("FATAL Error loading one or more images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleElevatorEnabledLight(int elevatorIndex)
    {

        Image enabledButton = enabledButtonsImageViews[elevatorIndex].getImage();
        enabledButton = (enabledButton == circleButtonOffImage) ? circleButtonOnImage : circleButtonOffImage;
        enabledButtonsImageViews[elevatorIndex].setImage(enabledButton);

    }

    public void toggleFireKeyLight(int elevatorIndex, int state)
    {
        Image fireButton = (state == 1) ? squareButtonOnImage : squareButtonOffImage;
        fireKeyImageViews[elevatorIndex-1].setImage(fireButton);
    }

}
