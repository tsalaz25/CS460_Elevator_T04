package CommandCenter.communicators;

import CommandCenter.Topics;
import CommandCenter.states.ElevatorDirectionState;
import CommandCenter.states.ElevatorDoorState;
import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

/**
 * Updates the CCElevator Component
 */
public class ElevatorCommunicator {

    //    Elevator states used to update ui
    private ElevatorDoorState elevatorDoorState = ElevatorDoorState.CLOSED;
    private ElevatorDirectionState elevatorDirectionState = ElevatorDirectionState.STATIONARY;
    private int currentFloor = 1;

    private final Bus bus = new Bus();
    private final Topic elevatorPositionTopic;
    private final Topic elevatorDirectionTopic;
    private final Topic elevatorDoorStateTopic;

    public ElevatorCommunicator(int elevatorID) {
        this.elevatorPositionTopic = Topics.ELEVATOR_POSITIONS[elevatorID];
        this.elevatorDirectionTopic = Topics.ELEVATOR_DIRECTIONS[elevatorID];
        this.elevatorDoorStateTopic = Topics.ELEVATOR_DOOR_STATES[elevatorID];

        this.subscribeToTopics();

        Thread thread = new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //  Get the oldest unread message
                //  May be null if there are no unread messages
                Message elevatorPosition = this.bus.getMessage(this.elevatorPositionTopic);
                Message elevatorDirection = this.bus.getMessage(this.elevatorDirectionTopic);
                Message elevatorDoorState = this.bus.getMessage(this.elevatorDoorStateTopic);
                //Let's only look for updates and make changes ten times a second
                if (elevatorPosition != null) {
                    this.setCurrentFloor(elevatorPosition.bodyOne());
                }
                if (elevatorDirection != null) {
                    this.setElevatorDirectionState(ElevatorDirectionState.values()[elevatorDirection.bodyOne()]);
                }
                if (elevatorDoorState != null) {
                    this.setElevatorDoorState(ElevatorDoorState.values()[elevatorDoorState.bodyOne()]);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Registers the bus for all the necessary topics:
     *      Elevator Position
     *      Elevator Direction
     *      Elevator Door State
     */
    private void subscribeToTopics() {
        this.bus.subscribe(this.elevatorPositionTopic);
        this.bus.subscribe(this.elevatorDirectionTopic);
        this.bus.subscribe(this.elevatorDoorStateTopic);
    }

    public ElevatorDoorState getElevatorDoorState() {
        return this.elevatorDoorState;
    }

    public void setElevatorDoorState(ElevatorDoorState elevatorDoorState) {
        this.elevatorDoorState = elevatorDoorState;
    }

    public ElevatorDirectionState getElevatorDirectionState() {
        return this.elevatorDirectionState;
    }

    public void setElevatorDirectionState(ElevatorDirectionState elevatorDirectionState) {
        this.elevatorDirectionState = elevatorDirectionState;
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }
}
