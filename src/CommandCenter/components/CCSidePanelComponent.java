package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CCSidePanelComponent extends VBox {

    // --- Images ---
    private Image independentModeSelectedImage;
    private Image independentModeDeselectedImage;
    private Image centralizedModeSelectedImage;
    private Image centralizedModeDeselectedImage;
    private Image fireModeSelectedImage;
    private Image fireModeDeselectedImage;

    public ImageView independentModeButton;
    public ImageView centralizedModeButton;
    public ImageView fireModeButton;

    public CCSidePanelComponent()
    {
        this.loadImages();

        this.setAlignment(Pos.CENTER);
        Label modeSelectTitle = new Label("Mode Select");
        try {
            Font bauhausFont = Font.loadFont(getClass().getResourceAsStream("/Bauhaus 93 Regular.ttf"), 18);
            if (bauhausFont != null) {
                modeSelectTitle.setFont(bauhausFont);
            } else {
                modeSelectTitle.setFont(Font.font("System", 18));
                System.out.println("Warning: Bauhaus font not loaded.");
            }
        } catch (Exception e) {
            modeSelectTitle.setFont(Font.font("System", 18));
            System.out.println("Error loading Bauhaus font: " + e.getMessage());
        }
        modeSelectTitle.setTextFill(Color.WHITE);
        VBox modeSelectButtonContainer = new VBox(5);


        independentModeButton = new ImageView(independentModeSelectedImage);
        centralizedModeButton = new ImageView(centralizedModeDeselectedImage);
        fireModeButton = new ImageView(fireModeDeselectedImage);


        double modeButtonWidth = 150;
        independentModeButton.setFitWidth(modeButtonWidth);
        independentModeButton.setPreserveRatio(true);
        centralizedModeButton.setFitWidth(modeButtonWidth);
        centralizedModeButton.setPreserveRatio(true);
        fireModeButton.setFitWidth(modeButtonWidth);
        fireModeButton.setPreserveRatio(true);

        modeSelectButtonContainer.getChildren().addAll(independentModeButton, centralizedModeButton, fireModeButton);
        this.getChildren().addAll(modeSelectTitle, modeSelectButtonContainer);
        this.setBackground(Background.fill(CommandCenterColors.PANEL_DARK));
    }

    private void loadImages() {
        try {
            independentModeSelectedImage = new Image(getClass().getResource("/resources/Independant_Button_On.png").toExternalForm());
            independentModeDeselectedImage = new Image(getClass().getResource("/resources/Independant_Button_Off.png").toExternalForm());
            centralizedModeSelectedImage = new Image(getClass().getResource("/resources/Centralized_Button_On.png").toExternalForm());
            centralizedModeDeselectedImage = new Image(getClass().getResource("/resources/Centralized_Button_Off.png").toExternalForm());
            fireModeSelectedImage = new Image(getClass().getResource("/resources/Fire_Button_On.png").toExternalForm());
            fireModeDeselectedImage = new Image(getClass().getResource("/resources/Fire_Button_Off.png").toExternalForm());
        } catch (Exception e) {
            System.err.println("FATAL Error loading one or more images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleIndependantLight()
    {
        independentModeButton.setImage(independentModeSelectedImage);
        centralizedModeButton.setImage(centralizedModeDeselectedImage);
        fireModeButton.setImage(fireModeDeselectedImage);
    }

    public void toggleCentralizedLight()
    {
        independentModeButton.setImage(independentModeDeselectedImage);
        centralizedModeButton.setImage(centralizedModeSelectedImage);
        fireModeButton.setImage(fireModeDeselectedImage);
    }

    public void toggleFireLight()
    {
        independentModeButton.setImage(independentModeDeselectedImage);
        centralizedModeButton.setImage(centralizedModeDeselectedImage);
        fireModeButton.setImage(fireModeSelectedImage);
    }
}
