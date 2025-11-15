package CommandCenter;

import CommandCenter.communicators.ElevatorCommunicator;
import CommandCenter.components.CCCallButtonList;
import CommandCenter.components.CCElevatorComponent;
import CommandCenter.states.ElevatorDoorState;
import SoftwareBus.Bus.Bus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OldCommandCenterDemo extends Application {
    private boolean isIndependentModeSelected = true;
    private boolean isCentralizedModeSelected = false;
    private boolean isFireModeSelected = false;

    // --- References to components ---
    private static final int NUM_ELEVATORS = 4;
    private static final int NUM_FLOORS = 10;
    private CCCallButtonList callButtonList;
    private List<CCElevatorComponent> elevatorComponents = new ArrayList<>();
    private List<VBox> requestButtonVBoxes = new ArrayList<>();
    private ImageView[] enabledButtonsImageViews = new ImageView[NUM_ELEVATORS];
    private ImageView[] fireKeyImageViews = new ImageView[NUM_ELEVATORS];
    private ImageView independantModeToggle;
    private ImageView centralizedModeToggle;
    private ImageView fireModeToggle;

    // --- Added Missing Field Declaration ---
    private List<Circle[]> requestButtonCirclesList = new ArrayList<>();


    // --- State Tracking ---
    private boolean[] elevatorsEnabled = new boolean[NUM_ELEVATORS];

    // --- Images ---
    private Image independantModeSelectedImage;
    private Image independantModeDeselectedImage;
    private Image centralizedModeSelectedImage;
    private Image centralizedModeDeselectedImage;
    private Image fireModeSelectedImage;
    private Image fireModeDeselectedImage;
    private Image circleButtonOnImage;
    private Image circleButtonOffImage;
    private Image squareButtonOnImage;
    private Image squareButtonOffImage;

    private ElevatorCommunicator[] elevatorCommunicators = new ElevatorCommunicator[NUM_ELEVATORS];
    private Bus bus = new Bus();


    @Override
    public void start(Stage primaryStage) throws Exception {
        Arrays.fill(elevatorsEnabled, true); // Start all enabled
        for (int i = 0; i < NUM_ELEVATORS; i++) {
            elevatorCommunicators[i] = new ElevatorCommunicator(i);
        }

        loadImages();

        GridPane root = new GridPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);

        // create call button
        callButtonList = new CCCallButtonList();
        callButtonList.setSpacing(45);
        callButtonList.setTranslateY(-45);
        root.add(callButtonList, 0, 0, 1, 3);

        // create elevator and request buttons?
        HBox elevatorsAndRequests = new HBox(30);
        elevatorsAndRequests.setAlignment(Pos.CENTER);
        for (int i = 0; i < NUM_ELEVATORS; i++) {
            CCElevatorComponent elevatorComp = new CCElevatorComponent(this.elevatorCommunicators[i]);
            elevatorComponents.add(elevatorComp);

            VBox requestButtonVBox = new VBox();
            requestButtonVBox.setAlignment(Pos.CENTER);
            requestButtonVBox.setSpacing(50);
            requestButtonVBox.setTranslateX(-25);
            requestButtonVBox.setTranslateY(-15);

            Circle[] currentElevatorRequestButtons = new Circle[NUM_FLOORS];

            for (int j = NUM_FLOORS - 1; j >= 0; j--) {
                Circle reqCircle = new Circle(5, CommandCenterColors.TERMINAL_ORANGE_DARK);
                currentElevatorRequestButtons[j] = reqCircle;
                StackPane circlePane = new StackPane(reqCircle);
                requestButtonVBox.getChildren().add(circlePane);
            }
            requestButtonVBoxes.add(requestButtonVBox);
            requestButtonVBox.setPadding(new Insets(30, 0, 0, 0));

            // Use the correctly declared list field
            requestButtonCirclesList.add(currentElevatorRequestButtons);

            elevatorsAndRequests.getChildren().addAll(elevatorComp, requestButtonVBox);
        }
        elevatorsAndRequests.setBackground(Background.fill(CommandCenterColors.TERMINAL_BACKGROUND));
        root.add(elevatorsAndRequests, 1, 0, NUM_ELEVATORS * 2, 1);


        // create select box
        VBox modeSelect = createModeSelectVBox();
        modeSelect.setBackground(Background.fill(CommandCenterColors.PANEL_DARK));
        root.add(modeSelect, NUM_ELEVATORS * 2 + 1, 0, 1, 1);

        // create lower panel
        GridPane lowerPanel = createLowerPanel();
        lowerPanel.setBackground(Background.fill(CommandCenterColors.PANEL_DARK));
        lowerPanel.setMinHeight(90);
        root.add(lowerPanel, 1, 1, NUM_ELEVATORS * 2,1);


        Button simpleTestButton = new Button("Run Test");
        simpleTestButton.setOnAction(e -> {if(!isFireModeSelected) lightButtonsTest();});
        root.add(simpleTestButton, NUM_ELEVATORS * 2 + 1, 1);


        root.setBackground(Background.fill(CommandCenterColors.PANEL_BACKGROUND));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Control Center");
        primaryStage.show();

        //callButtonList.setFloorCallUp(7);
        //callButtonList.setFloorCallUp(3);
        //callButtonList.setFloorCallDown(2);
        //callButtonList.setFloorCallDown(10);
    }

    private void loadImages() {
        try {
            independantModeSelectedImage = new Image(getClass().getResource("/Independant_Button_On.png").toExternalForm());
            independantModeDeselectedImage = new Image(getClass().getResource("/Independant_Button_Off.png").toExternalForm());
            centralizedModeSelectedImage = new Image(getClass().getResource("/Centralized_Button_On.png").toExternalForm());
            centralizedModeDeselectedImage = new Image(getClass().getResource("/Centralized_Button_Off.png").toExternalForm());
            fireModeSelectedImage = new Image(getClass().getResource("/Fire_Button_On.png").toExternalForm());
            fireModeDeselectedImage = new Image(getClass().getResource("/Fire_Button_Off.png").toExternalForm());
            circleButtonOnImage = new Image(getClass().getResource("/Circle_Button_On.png").toExternalForm());
            circleButtonOffImage = new Image(getClass().getResource("/Circle_Button_Off.png").toExternalForm());
            squareButtonOnImage = new Image(getClass().getResource("/Square_Button_On.png").toExternalForm());
            squareButtonOffImage = new Image(getClass().getResource("/Square_Button_Off.png").toExternalForm());
        } catch (Exception e) {
            System.err.println("FATAL Error loading one or more images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private GridPane createLowerPanel() {
        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(5);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(5, 0, 5, 5));

        Label enabledLabel = new Label("Enabled");
        enabledLabel.setTextFill(Color.WHITE);
        panel.add(enabledLabel, 0, 0);

        for (int i = 0; i < NUM_ELEVATORS; i++) {
            ImageView enabledButton = new ImageView(elevatorsEnabled[i] ? circleButtonOnImage : circleButtonOffImage);
            enabledButton.setScaleX(4);
            enabledButton.setScaleY(4);
            enabledButton.setPreserveRatio(true);
            enabledButton.setFitWidth(40);
            final int elevatorIndex = i;
            enabledButton.setOnMouseClicked(event -> {
                System.out.println("[Click] Enabled button for Elevator " + (elevatorIndex + 1));
                elevatorsEnabled[elevatorIndex] = !elevatorsEnabled[elevatorIndex];
                updateEnabledButtonVisual(elevatorIndex);
                System.out.println("   - Elevator " + (elevatorIndex + 1) + " state is now: " + (elevatorsEnabled[elevatorIndex] ? "Enabled" : "Disabled"));
            });
            enabledButtonsImageViews[i] = enabledButton;
            panel.add(enabledButton, i + 1, 0);
        }

        Label fireKeyLabel = new Label("Fire Key");
        fireKeyLabel.setTextFill(Color.WHITE);
        panel.add(fireKeyLabel, 0, 1);

        for (int i = 0; i < NUM_ELEVATORS; i++) {
            ImageView fireKeyIndicator = new ImageView(squareButtonOffImage);
            fireKeyIndicator.setScaleX(4);
            fireKeyIndicator.setScaleY(4);
            fireKeyIndicator.setPreserveRatio(true);
            fireKeyIndicator.setFitWidth(40);
            fireKeyIndicator.setTranslateY(10);
            final int elevatorIndex = i;
            fireKeyIndicator.setOnMouseClicked(event -> {
                System.out.println("[Click] Fire Key indicator for Elevator " + (elevatorIndex + 1));
                toggleFireKeyStateVisual(elevatorIndex);
            });
            fireKeyImageViews[i] = fireKeyIndicator;
            panel.add(fireKeyIndicator, i + 1, 1);
        }

        return panel;
    }

    // mode select box should be its own object
    private VBox createModeSelectVBox() {
        VBox modeSelect = new VBox(5);
        modeSelect.setAlignment(Pos.CENTER);
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

        if (independantModeDeselectedImage == null /* etc */) {
            modeSelect.getChildren().addAll(modeSelectTitle, new Label("Error: Images missing"));
            return modeSelect;
        }

        independantModeToggle = new ImageView(independantModeSelectedImage);
        setupModeToggle(independantModeToggle, independantModeSelectedImage, independantModeDeselectedImage, 0);

        centralizedModeToggle = new ImageView(centralizedModeDeselectedImage);
        setupModeToggle(centralizedModeToggle, centralizedModeSelectedImage, centralizedModeDeselectedImage, 1);

        fireModeToggle = new ImageView(fireModeDeselectedImage);
        setupModeToggle(fireModeToggle, fireModeSelectedImage, fireModeDeselectedImage, 2);

        double modeButtonWidth = 150;
        independantModeToggle.setFitWidth(modeButtonWidth);
        independantModeToggle.setPreserveRatio(true);
        centralizedModeToggle.setFitWidth(modeButtonWidth);
        centralizedModeToggle.setPreserveRatio(true);
        fireModeToggle.setFitWidth(modeButtonWidth);
        fireModeToggle.setPreserveRatio(true);

        modeSelectButtonContainer.getChildren().addAll(independantModeToggle, centralizedModeToggle, fireModeToggle);
        modeSelect.getChildren().addAll(modeSelectTitle, modeSelectButtonContainer);
        return modeSelect;
    }

    private void setupModeToggle(ImageView toggle, Image selectedImg, Image deselectedImg, int modeIndex) {
        if (toggle == null || selectedImg == null || deselectedImg == null) return;
        toggle.setOnMouseClicked(event -> {
            System.out.println("[Click] Mode toggle: " + modeIndex);
            boolean wasSelected = false;
            switch (modeIndex) {
                case 0: wasSelected = isIndependentModeSelected; break;
                case 1: wasSelected = isCentralizedModeSelected; break;
                case 2: wasSelected = isFireModeSelected; break;
            }

            if (!wasSelected) {
                isIndependentModeSelected = false;
                isCentralizedModeSelected = false;
                isFireModeSelected = false;
                if(independantModeToggle != null) independantModeToggle.setImage(independantModeDeselectedImage);
                if(centralizedModeToggle != null) centralizedModeToggle.setImage(centralizedModeDeselectedImage);
                if(fireModeToggle != null) fireModeToggle.setImage(fireModeDeselectedImage);

                toggle.setImage(selectedImg);
                String modeName = "";
                switch (modeIndex) {
                    case 0:
                        isIndependentModeSelected = true;
                        modeName="Independent";

                        break;
                    case 1:
                        isCentralizedModeSelected = true;
                        modeName="Centralized";

                        break;
                    case 2:
                        isFireModeSelected = true;
                        modeName="Fire";

                        handleFireModeActivation();
                        break;
                }
                System.out.println("Mode changed to: " + modeName);
            } else {
                System.out.println("Mode " + modeIndex + " was already selected.");
            }
        });
    }

    private void lightButtonsTest() {

        System.out.println("--- Running Simplest Test ---");

        // Use Platform.runLater for UI updates even within the handler
        Platform.runLater(() -> {

            Timeline timeline = new Timeline(
            // disable elevator 4
            // it won't be used in this test to show how disabling stops updates
            new KeyFrame(Duration.seconds(0), e -> {if (3 < NUM_ELEVATORS) { elevatorsEnabled[3] = false; updateEnabledButtonVisual(3); }}),

            // set call buttons to 5
            // all elevators will go to floor 5
            new KeyFrame(Duration.seconds(5), e -> {
                System.out.println("Test 1: Setting Call Buttons");
                if (callButtonList != null) {
                // note: number if off
                callButtonList.setFloorCallUp(6);
            }   else { System.out.println(" - callButtonList is null"); }
            }),

            new KeyFrame(Duration.seconds(10), e-> {
                // show motion arrows
                if (elevatorComponents.size() > 1) {
                    elevatorComponents.get(0).showUpArrow();
                }
                if (elevatorComponents.size() > 2) {
                    elevatorComponents.get(1).showUpArrow();
                }
                if (elevatorComponents.size() > 3) {
                    elevatorComponents.get(2).showUpArrow();
                }
            }),

            ////////// begin moving one floor up at a time /////////////////////////////////////////////////////

            new KeyFrame(Duration.seconds(15), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(2, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(1).updateVisualState(2, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(2).updateVisualState(2, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(20), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(3, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(1).updateVisualState(3, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(2).updateVisualState(3, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(25), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(4, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(1).updateVisualState(4, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(2).updateVisualState(4, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(30), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(5, ElevatorDoorState.OPEN);
                    elevatorComponents.get(1).updateVisualState(5, ElevatorDoorState.OPEN);
                    elevatorComponents.get(2).updateVisualState(5, ElevatorDoorState.OPEN);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }

                // destination reached hide motion arrows
                if (elevatorComponents.size() > 1) { elevatorComponents.get(0).hideArrows(); }
                if (elevatorComponents.size() > 2) { elevatorComponents.get(1).hideArrows(); }
                if (elevatorComponents.size() > 3) { elevatorComponents.get(2).hideArrows(); }

                // hide call
                if (callButtonList != null) {
                    // note: number if off
                    callButtonList.clearFloorCallUp(6);
                }   else { System.out.println(" - callButtonList is null"); }
            }),


            new KeyFrame(Duration.seconds(35), e -> {
                // we get new requests
                lightRequestButton(0, 8, true);
                lightRequestButton(1, 8, true);
                lightRequestButton(2, 10, true);
            }),

            new KeyFrame(Duration.seconds(40), e -> {
                //close doors
                elevatorComponents.get(0).updateVisualState(5, ElevatorDoorState.CLOSED);
                elevatorComponents.get(1).updateVisualState(5, ElevatorDoorState.CLOSED);
                elevatorComponents.get(2).updateVisualState(5, ElevatorDoorState.CLOSED);

                //set motion arrows again
                if (elevatorComponents.size() > 1) { elevatorComponents.get(0).showUpArrow(); }
                if (elevatorComponents.size() > 2) { elevatorComponents.get(1).showUpArrow(); }
                if (elevatorComponents.size() > 3) { elevatorComponents.get(2).showUpArrow(); }
            }),

            new KeyFrame(Duration.seconds(45), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(6, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(1).updateVisualState(6, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(2).updateVisualState(6, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(50), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(7, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(1).updateVisualState(7, ElevatorDoorState.CLOSED);
                    elevatorComponents.get(2).updateVisualState(7, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(55), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(0).updateVisualState(8, ElevatorDoorState.OPEN);
                    elevatorComponents.get(1).updateVisualState(8, ElevatorDoorState.OPEN);
                    elevatorComponents.get(2).updateVisualState(8, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }

                // destination reached hide motion arrows
                if (elevatorComponents.size() > 1) { elevatorComponents.get(0).hideArrows(); }
                if (elevatorComponents.size() > 2) { elevatorComponents.get(1).hideArrows(); }

                // hide request button
                lightRequestButton(0, 8, false);
                lightRequestButton(1, 8, false);
            }),

            new KeyFrame(Duration.seconds(60), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(2).updateVisualState(9, ElevatorDoorState.CLOSED);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }
            }),

            new KeyFrame(Duration.seconds(65), e -> {
                if (elevatorComponents.size() >= NUM_ELEVATORS) {
                    elevatorComponents.get(2).updateVisualState(10, ElevatorDoorState.OPEN);
                } else {
                    System.out.println(" - Error: Not enough elevator components");
                }

                // destination reached hide motion arrows
                if (elevatorComponents.size() > 3) { elevatorComponents.get(2).hideArrows(); }

                // hide request button
                lightRequestButton(2, 10, false);
            })); timeline.play();



            // --- Explicitly request layout ---
            if (getRootNode() != null) { // Need a way to get the root pane
                System.out.println("Requesting layout update...");
                getRootNode().requestLayout();
            }


            System.out.println("--- Simplest Test Finished ---");
        }); // End of Platform.runLater
    }

    // Helper method to get the root node (assuming root is GridPane)
    private GridPane getRootNode() {
        // Find the root pane reliably, e.g., via a known child's scene
        if (callButtonList != null && callButtonList.getScene() != null && callButtonList.getScene().getRoot() instanceof GridPane) {
            return (GridPane) callButtonList.getScene().getRoot();
        }
        // Add other ways to find the root if the above fails
        System.err.println("Could not reliably find root GridPane to request layout.");
        return null;
    }

    // Helper method to light up/down request buttons
    private void lightRequestButton(int elevatorIndex, int floorNumber, boolean active) {
        // --- Add detailed logging ---
        System.out.println("--> lightRequestButton called for E" + (elevatorIndex + 1) + " F" + floorNumber + " Active: " + active);

        if (elevatorIndex < 0 || elevatorIndex >= requestButtonCirclesList.size()) {
            System.out.println("  lightRequestButton: Invalid elevator index: " + elevatorIndex);
            return;
        }
        if (floorNumber < 1 || floorNumber > NUM_FLOORS) {
            System.out.println("  lightRequestButton: Invalid floor number: " + floorNumber);
            return;
        }

        int circleIndex = floorNumber - 1;
        Circle[] elevatorCircles = requestButtonCirclesList.get(elevatorIndex);

        if (circleIndex < 0 || circleIndex >= elevatorCircles.length || elevatorCircles[circleIndex] == null) {
            System.out.println("  lightRequestButton: Cannot find circle for E" + (elevatorIndex+1) + " F" + floorNumber + " at index " + circleIndex);
            return;
        }

        Circle targetCircle = elevatorCircles[circleIndex];
        // --- Log target circle info ---
        System.out.println("  Target Circle: " + targetCircle);
        System.out.println("  Current Fill: " + targetCircle.getFill());

        Paint newFill; // Use Paint type
        if (active) newFill = CommandCenterColors.TERMINAL_ORANGE;
        else newFill = CommandCenterColors.TERMINAL_ORANGE_DARK;
        targetCircle.setFill(newFill);

        // --- Log after setting ---
        System.out.println("  Set Fill To: " + newFill);
        System.out.println("  Fill After Set: " + targetCircle.getFill());
        System.out.println(" - Request button for E" + (elevatorIndex+1) + " F" + floorNumber + " set to " + (active ? "ON" : "OFF"));
    }

    private void updateEnabledButtonVisual(int elevatorIndex) {
        // --- Add logging ---
        System.out.println("--> updateEnabledButtonVisual called for index: " + elevatorIndex);
        if (elevatorIndex < 0 || elevatorIndex >= NUM_ELEVATORS || enabledButtonsImageViews[elevatorIndex] == null) {
            System.out.println("  Error updating enabled visual for index " + elevatorIndex + ": Invalid index or ImageView is null.");
            return;
        }
        ImageView button = enabledButtonsImageViews[elevatorIndex];
        boolean isEnabled = elevatorsEnabled[elevatorIndex]; // Read state from array
        Image targetImage = isEnabled ? circleButtonOnImage : circleButtonOffImage;

        // --- Log image details ---
        System.out.println("  State (elevatorsEnabled[" + elevatorIndex + "]): " + isEnabled);
        System.out.println("  Target Image: " + (targetImage == circleButtonOnImage ? "ON" : "OFF"));
        if (targetImage == null) {
            System.out.println("  ERROR: Target image is NULL!");
            return; // Don't try to set a null image
        }

        button.setImage(targetImage);
        System.out.println(" - Elevator " + (elevatorIndex + 1) + " visual set to: " + (isEnabled ? "ENABLED" : "DISABLED"));

        // Call dimming method based on state
        setElevatorDimmed(elevatorIndex, !isEnabled); // Dim if NOT enabled
    }

    // Method to Dim/Undim Elevator + Request Buttons
    private void setElevatorDimmed(int elevatorIndex, boolean dimmed) {
        // --- Add logging ---
        System.out.println("--> setElevatorDimmed called for index: " + elevatorIndex + " Dimmed: " + dimmed);
        if (elevatorIndex < 0 || elevatorIndex >= NUM_ELEVATORS) {
            System.out.println("  setElevatorDimmed: Invalid elevator index.");
            return;
        }

        double opacity = dimmed ? 0.5 : 1.0;
        boolean disableInteraction = dimmed; // Should interaction be disabled?

        // Dim Elevator Component
        if (elevatorIndex < elevatorComponents.size() && elevatorComponents.get(elevatorIndex) != null) {
            elevatorComponents.get(elevatorIndex).setOpacity(opacity);
            System.out.println(" - Elevator " + (elevatorIndex + 1) + " component opacity set to " + opacity);
        } else {
            System.out.println("  setElevatorDimmed: Elevator component not found for index " + elevatorIndex);
        }


        // Dim Corresponding Request Button VBox
        if (elevatorIndex < requestButtonVBoxes.size() && requestButtonVBoxes.get(elevatorIndex) != null) {
            requestButtonVBoxes.get(elevatorIndex).setOpacity(opacity);
            System.out.println(" - Request buttons VBox " + (elevatorIndex + 1) + " opacity set to " + opacity);
        } else {
            System.out.println("  setElevatorDimmed: Request buttons VBox not found for index " + elevatorIndex);
        }
    }

    private void toggleFireKeyStateVisual(int elevatorIndex) {
        if (elevatorIndex < 0 || elevatorIndex >= NUM_ELEVATORS || fireKeyImageViews[elevatorIndex] == null) {
            System.out.println("Error toggling fire key visual for index " + elevatorIndex);
            return;
        }
        ImageView indicator = fireKeyImageViews[elevatorIndex];
        if (indicator.getImage() == squareButtonOnImage) {
            indicator.setImage(squareButtonOffImage);
            System.out.println(" - Fire Key visually REMOVED from Elevator " + (elevatorIndex + 1));
        } else {
            indicator.setImage(squareButtonOnImage);
            System.out.println(" - Fire Key visually INSERTED in Elevator " + (elevatorIndex + 1));
        }
    }

    private void handleFireModeActivation() {
        System.out.println("Fire Mode Activated! Sending enabled elevators to floor 1.");
        for (int i = 0; i < NUM_ELEVATORS; i++) {
            if (elevatorsEnabled[i]) {
                if (i < elevatorComponents.size() && elevatorComponents.get(i) != null) {
                    System.out.println(" - Sending Elevator " + (i + 1) + " to Floor 1 (OPEN)");
                    elevatorComponents.get(i).updateVisualState(1, ElevatorDoorState.OPEN);
                    elevatorComponents.get(i).hideArrows();
                } else {
                    System.out.println(" - Error: Cannot access elevator component for index " + i);
                }
            } else {
                System.out.println(" - Elevator " + (i + 1) + " is disabled, not moving.");
            }
        }
        if(callButtonList != null) {
            callButtonList.clearAllCalls();
        }
    }

    private void updateComponents() {
//        Updates the fire key image
        for (int i = 0; i < NUM_ELEVATORS; i++) {
//            fireKeyImageViews[i].setImage(elevatorCommunicators[i].isFireKeyIn() ? fireModeSelectedImage : fireModeDeselectedImage);
        }
//        Update the request buttons
//        Update the call buttons
    }

    public static void main(String[] args) {
        launch(args);
    }
}