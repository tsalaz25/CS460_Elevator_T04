package CommandCenter.components;

import CommandCenter.CommandCenterColors;
import CommandCenter.communicators.ElevatorCommunicator;
import CommandCenter.communicators.TerminalCommunicator;
import CommandCenter.states.CallButtonSetState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/** Object for a GridPane representing the terminal display
 *
 * takes an 2 ints for the amount of elevators and floors
 *
 *  the terminal contains:
 *      - call buttons
 *      - elevator shafts (with request buttons and direction arrows)
 * **/
public class CCTerminalComponent extends GridPane {

    private static int NUM_ELEVATORS;
    private static int NUM_FLOORS;
    public CCCallButtonList callButtonList;
    public List<CCElevatorComponent> elevatorComponents = new ArrayList<>();
    private List<VBox> requestButtonVBoxes = new ArrayList<>();
    private List<Circle[]> requestButtonCirclesList = new ArrayList<>();

    private TerminalCommunicator terminalCommunicator = new TerminalCommunicator();

    /**
     * Constructor for the terminal display object
     * **/
    public CCTerminalComponent(int elevatorNum, int floorNum)
    {

        // set number of elevators and floors
        this.NUM_ELEVATORS = elevatorNum;
        this.NUM_FLOORS = floorNum;

        this.setPadding(new Insets(10));
        this.setHgap(10);
        this.setVgap(10);
        this.setPrefSize(400,400);

        // create call button
        this.callButtonList = new CCCallButtonList();
        this.callButtonList.setSpacing(45);
        this.callButtonList.setTranslateY(-6);
        this.add(this.callButtonList, 0, 0, 1, 3);

        // create elevator and request buttons
        HBox elevatorsAndRequests = new HBox(30);
        elevatorsAndRequests.setAlignment(Pos.CENTER);
        for (int i = 0; i < NUM_ELEVATORS; i++) {
            ElevatorCommunicator elevatorCommunicator = new ElevatorCommunicator(i);
            CCElevatorComponent elevatorComp = new CCElevatorComponent(elevatorCommunicator);
            this.elevatorComponents.add(elevatorComp);

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
            this.requestButtonVBoxes.add(requestButtonVBox);
            requestButtonVBox.setPadding(new Insets(30, 0, 0, 0));

            // Use the correctly declared list field
            this.requestButtonCirclesList.add(currentElevatorRequestButtons);

            elevatorsAndRequests.getChildren().addAll(elevatorComp, requestButtonVBox);
        }
        this.add(elevatorsAndRequests, 1, 0, NUM_ELEVATORS * 2, 1);
        this.setBackground(Background.fill(CommandCenterColors.TERMINAL_BACKGROUND));
        this.setHgap(50);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> updateComponents()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Toggles the opacity of the elevator at the given index between light and dark
     * **/
    public void toggleElevatorOpacity(int elevatorIndex)
    {
        double dimmed = 0.5;
        double light = 1.0;
        double opacityToggle = (elevatorComponents.get(elevatorIndex).getOpacity() == light) ? dimmed : light;

        elevatorComponents.get(elevatorIndex).setOpacity(opacityToggle);
        requestButtonVBoxes.get(elevatorIndex).setOpacity(opacityToggle);

    }

    /**
     * Toggles the opacity of the request button at the given elevator and floor between light and dark
     * **/
    public void toggleRequestButtonOpacity(int elevatorIndex, int floorIndex)
    {
        Color dimmed = CommandCenterColors.TERMINAL_ORANGE_DARK;
        Color light = CommandCenterColors.TERMINAL_ORANGE;
        Circle requestButton = requestButtonCirclesList.get(elevatorIndex-1)[floorIndex-1];
        Color colorToggle = (requestButton.getFill() == light) ? dimmed : light;

        requestButton.setFill(colorToggle);
    }

    public void setRequestButtonOpacity(int elevatorIndex, int floorIndex, boolean isLight) {
        Color dimmed = CommandCenterColors.TERMINAL_ORANGE_DARK;
        Color light = CommandCenterColors.TERMINAL_ORANGE;
        Circle requestButton = requestButtonCirclesList.get(elevatorIndex-1)[floorIndex-1];
        Color colorToggle = isLight ? light : dimmed;

        requestButton.setFill(colorToggle);
    }

    public void updateComponents() {
        boolean[][] requestButtonStates = this.terminalCommunicator.getRequestedFloorStates();
        for (int i = 0; i < NUM_ELEVATORS; i++) {
            for (int j = 0; j < NUM_FLOORS; j++) {
                setRequestButtonOpacity(i+1, j+1, requestButtonStates[i][j]);
            }
        }

        CallButtonSetState[] callButtonStates = this.terminalCommunicator.getCallButtonSetStates();
        for (int i = 0; i < NUM_FLOORS; i++) {
            switch (callButtonStates[i]) {
                case UP -> this.callButtonList.setFloorCallUp(i+1);
                case DOWN -> this.callButtonList.setFloorCallDown(i+1);
                case BOTH -> this.callButtonList.setFloorCallBoth(i+1);
                case OFF -> this.callButtonList.clearFloorCall(i+1);
            }
        }
    }

}
