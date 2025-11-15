package CommandCenter.communicators;

import CommandCenter.ElevatorCommandCenterDisplay;
import CommandCenter.Topics;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Topic;
import javafx.application.Platform;

import java.util.Arrays;


/**
 * Handles communication between the data bus and the lower panel of the ElevatorCommandCenter
 * Displays Fire Key status
 * Publishes and displays elevator disabled/enabled state
 * **/
public class LowerPanelCommunicator {

    // instance of software bus
    private Bus lowerBus;

    // instance of display for direct use of API
    private ElevatorCommandCenterDisplay display;

    private int[] elevatorStates;

    public LowerPanelCommunicator(ElevatorCommandCenterDisplay CCdisplay)
    {
        // display to update
        this.display = CCdisplay;

        // bus to listen to
        this.lowerBus = new Bus();

        // array to hold enabled states
        this.elevatorStates = new int[ElevatorCommandCenterDisplay.NUM_ELEVATORS];
        Arrays.fill(elevatorStates,1);

        // get messages related to fire key status (0/1)
        this.lowerBus.subscribe(Topics.FIRE_KEY);

        // get messages related to elevator disability (0/1)
        this.lowerBus.subscribe(Topics.ELEVATOR_DISABLED);


        // set the event when an enabled button is pressed
        for(int i = 0; i <ElevatorCommandCenterDisplay.NUM_ELEVATORS; i++)
        {
            int elevatorNum = i;
            this.display.lowerPanel.enabledButtonsImageViews[i].setOnMouseClicked(e ->{
                elevatorStates[elevatorNum] = 1 - elevatorStates[elevatorNum];
                Platform.runLater( () -> this.display.toggleElevatorEnabledLights(elevatorNum));
                System.out.println("Toggled Disable on Elevator: " + (elevatorNum + 1));
                this.lowerBus.publish(new Message(new Topic(13,elevatorNum + 1), new int[]{elevatorStates[elevatorNum],0,0,0}));
            });
        }

        // start the listening thread
        listeningThread();
    }

    /**
     * Listens for updates from the data bus and updates the GUI accordingly
     * **/
    private void listeningThread()
    {
        // new thread for message checking
       Thread listenThread = new Thread( ()-> {

            // continuously check message stream
            while(true)
            {
                // get the oldest unread fire key message
                // null if none are unread
                for(Topic topic : Topics.FIRE_KEYS)
                {
                    Message fireKeyUpdate = this.lowerBus.getMessage(topic);
                    if(fireKeyUpdate != null)
                    {
                        // subtopic determines elevator shaft
                        int elevatorShaft = fireKeyUpdate.subtopicInt();

                        // update FireKeyLight
                        System.out.println("Fire Key Light At: " + elevatorShaft);
                        System.out.println("    - New State: " + ((fireKeyUpdate.bodyOne() == 1) ? "in" : "out"));
                        Platform.runLater(() -> this.display.toggleFireKeyLight(elevatorShaft,fireKeyUpdate.bodyOne()));
                    }
                }

                /* get the oldest unread elevator disabled message  // shouldn't be able to send disabled to the GUI?
                // null if none are unread
                for(Topic disTopic : Topics.ELEVATORS_DISABLED)
                {
                    Message disabledUpdate = this.lowerBus.getMessage(disTopic);
                    if(disabledUpdate != null)
                    {
                        // subtopic determines elevator shaft
                        int elevatorShaft = disabledUpdate.subtopicInt();

                        // update disabled visibility
                        elevatorStates[elevatorShaft-1] = 1 - elevatorStates[elevatorShaft-1];
                        System.out.println("DISABLING: " + (elevatorShaft-1));
                        Platform.runLater(() -> this.display.toggleElevatorEnabledLights(elevatorShaft));
                    }
                }

                 */

                // only check once a second
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }

        });
       listenThread.setDaemon(true);
       listenThread.start();
    }

}
