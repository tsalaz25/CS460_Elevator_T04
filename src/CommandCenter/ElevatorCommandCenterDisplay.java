package CommandCenter;

import CommandCenter.communicators.LowerPanelCommunicator;
import CommandCenter.communicators.SidePanelCommunicator;
import CommandCenter.components.*;
import CommandCenter.states.ElevatorDirectionState;
import CommandCenter.states.ElevatorDoorState;
import CommandCenter.states.ElevatorMode;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;


/**
 * Object for the command center display
 * **/
public class ElevatorCommandCenterDisplay extends GridPane
{

    public static final int NUM_ELEVATORS = 4;
    private static final int NUM_FLOORS = 10;

    // terminal component
    private CCTerminalComponent terminal = new CCTerminalComponent(NUM_ELEVATORS,NUM_FLOORS);

    // lower panel component
    public CCLowerPanelComponent lowerPanel = new CCLowerPanelComponent(NUM_ELEVATORS);

    // side panel component
    private CCSidePanelComponent sidePanel = new CCSidePanelComponent();

    // side panel communicator for bus communication
    private SidePanelCommunicator sidePanelCommunicator = new SidePanelCommunicator();

    // lower panel communicator for bus communication
    // says it's never "accessed" but it listens and updates so
    private LowerPanelCommunicator LowerPanelcommunicator;


    /**
     * Constructor For the Command Center
     * **/
    public ElevatorCommandCenterDisplay(Stage primaryStage)
    {
        // GridPane Settings
        this.setPadding(new Insets(10));
        this.setHgap(10);
        this.setVgap(10);
        this.setPrefSize(400,400);

        // lower panel
        LowerPanelcommunicator = new LowerPanelCommunicator(this);


        // add terminal to the root
        this.add(this.terminal, 1, 0, NUM_ELEVATORS * 2, 1);

        // add lowerPanel to root
        this.add(this.lowerPanel, 1, 1, NUM_ELEVATORS * 2,1);

        // add sidePanel to root
        this.add(this.sidePanel, NUM_ELEVATORS * 2 + 1, 0, 1, 1);

        /**
        // set the event when an enabled button is pressed
        // might need to move this out of display
        for(int i = 0; i < NUM_ELEVATORS; i++)
        {
            int elevatorNum = i;
            this.lowerPanel.enabledButtonsImageViews[i].setOnMouseClicked(e ->{
                this.toggleElevatorEnabledLights(elevatorNum);
            });
        }

         */

        // set the events when the side panel buttons are pressed
        // might need to move these out of display
        this.sidePanel.independentModeButton.setOnMouseClicked(e ->{
            this.setModeButton(ElevatorMode.INDEPENDENT);
            this.sidePanelCommunicator.setMode(ElevatorMode.INDEPENDENT);
        });
        this.sidePanel.centralizedModeButton.setOnMouseClicked(e ->{
            this.setModeButton(ElevatorMode.CENTRALIZED);
            this.sidePanelCommunicator.setMode(ElevatorMode.CENTRALIZED);
        });
        this.sidePanel.fireModeButton.setOnMouseClicked(e ->{
            this.setModeButton(ElevatorMode.FIRE);
            this.sidePanelCommunicator.setMode(ElevatorMode.FIRE);
        });

        // Register callback to update UI when mode changes come from the bus
        this.sidePanelCommunicator.setOnModeChangedFromBus(mode -> {
            System.out.println("UI updating from bus callback: " + mode);
            this.setModeButton(mode);
        });


        this.setBackground(Background.fill(CommandCenterColors.PANEL_BACKGROUND));

        // set the scene
        Scene scene = new Scene(this);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Control Center");
        primaryStage.show();
    }

    /**
     * Shows when an elevator is enabled/disabled
     * toggles between enabled and disabled when called
     *
     * dims the elevator shaft and switches the lower panel light
     *
     * all elevators start enabled
     * **/
    public void toggleElevatorEnabledLights(int elevatorNum)
    {
        this.lowerPanel.toggleElevatorEnabledLight(elevatorNum);
        this.terminal.toggleElevatorOpacity(elevatorNum);
    }

    /**
     * Toggles whether a request button at a given elevator and floor is dimmed/lit up
     *
     * all request buttons start disabled
     * **/
    public void toggleRequestButtonLight(int elevatorNum, int floorNum)
    {
        this.terminal.toggleRequestButtonOpacity(elevatorNum,floorNum);
    }

    /**
     * Toggles whether a fire key light at a given elevator is dimmed/lit up
     *
     * all fire key lights start disabled
     * **/
    public void toggleFireKeyLight(int elevatorNum, int state)
    {
        this.lowerPanel.toggleFireKeyLight(elevatorNum, state);
    }

    /**
     * Moves an elevator from one floor to another
     * Also determines the door state (opened/closed) of that elevator
     *
     * all elevators start open at floor 1
     * **/
    public void elevatorToFloor(int elevatorNum, int floorNum, ElevatorDoorState doorState)
    {
        this.terminal.elevatorComponents.get(elevatorNum-1).updateVisualState(floorNum,doorState);
    }

    /**
     * Sets the motion arrow at the given elevator
     *
     * all motion arrows start stationary
     * **/
    public void setMotionArrows(int elevatorNum, ElevatorDirectionState direction)
    {
        CCElevatorComponent elevator = this.terminal.elevatorComponents.get(elevatorNum-1);
        switch(direction)
        {
            case UP:
                elevator.showUpArrow();
                break;
            case DOWN:
                elevator.showDownArrow();
                break;
            case STATIONARY:
                elevator.hideArrows();
                break;
        }
    }

    /**
     * Lights the call button at the given floor
     * Uses the direction states
     *
     * all call buttons start off stationary (both lights off)
     * **/
    public void setCallButton(int floorNum, ElevatorDirectionState direction)
    {
        // the array has 0 at the top  so needs translated
        floorNum = 10 - (floorNum - 1);

        switch(direction)
        {
            case UP:
                this.terminal.callButtonList.setFloorCallUp(floorNum);
                break;
            case DOWN:
                this.terminal.callButtonList.setFloorCallDown(floorNum);
                break;
            case STATIONARY:
                this.terminal.callButtonList.clearFloorCallUp(floorNum);
                this.terminal.callButtonList.clearFloorCallDown(floorNum);
                break;
        }
    }

    /**
     * Toggles the current mode button
     * Selecting one will deselect the others
     *
     * assumes the elevator starts in independent mode
     * **/
    public void setModeButton(ElevatorMode mode)
    {
        switch (mode)
        {
            case INDEPENDENT -> this.sidePanel.toggleIndependantLight();
            case CENTRALIZED -> this.sidePanel.toggleCentralizedLight();
            case FIRE -> this.sidePanel.toggleFireLight();
        }
    }
}
