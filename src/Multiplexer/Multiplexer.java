package Multiplexer;
import API.MotorAPI;
import Devices.Motor.Motion;
import Devices.Motor.Direction;
import API.SensorAPI;
import CabinGUI.CabinPanel;
import CabinGUI.DoorState;
import LobbyGUI.LobbyPanel;
import SoftwareBus.Bus.*;


public class    Multiplexer {
    private final int NUM_FLOORS = 10;
    private final int ID;

    //Memeory varibles to insure we don't spam the bus
    private int lastFloor = 1;
    private boolean lastAligned = false;
    private DoorState lastDoor = DoorState.CLOSED;
    private int[] activeButtons = new int[NUM_FLOORS];
    private int[] activeCalls = new int[2*NUM_FLOORS];
    private boolean lastOverloaded = false;

    //Public test values
    public int testFloor = 2;
    public int testDirection = 1;
    public int testDoor = 1;
    
    /*
     * Constructs a Multiplexer and then begins polling for input from devices and from
     *
     *
     */
    public Multiplexer(int ID, Bus bus,MotorAPI motor,SensorAPI sensor,LobbyPanel lobby,CabinPanel cabin) {
        this.ID = ID;
        Topics.subscribeAll(bus,ID);
        int floor = lastFloor;
        boolean aligned = lastAligned;
        DoorState door = lastDoor;
        boolean overloaded = lastOverloaded;
        for (int i = 0; i < NUM_FLOORS; i++) {
            activeButtons[i] = 0;
            activeCalls[2*i] = 0;
            activeCalls[(2*i)+1] = 0;
        }

        while(true){

            //Device block 
            //Check floor state
            floor = motor.getFloor(motor.getPosition());
            aligned = sensor.isFloorAligned(floor);
            if (floor != lastFloor || aligned != lastAligned){
                lastFloor = floor;
                lastAligned = aligned;
                int alignedInt;
                if(aligned){
                    alignedInt = 1;
                } else {
                    alignedInt = 0;
                }
                Topics.publish(bus, Topics.ELEVATOR_FLOOR, ID, new int[]{floor,alignedInt});
            }

            //Check door state
            door = cabin.doorState();
            if (door != lastDoor){
                lastDoor = door;
                int doorInt = DoorStateToInt(door);
                Topics.publish(bus, Topics.ELEVATOR_DOOR, ID, new int[]{doorInt});
            }
            //check cabin button state

            if(cabin.hasSelection()){
                int selection = cabin.selectedFloor();
                if(activeButtons[selection-1] == 0){
                    activeButtons[selection-1] = 1;
                    Topics.publish(bus, Topics.FLOOR_SELECT, ID, new int[]{selection});
                }
            }
 
            //Check call button state
            int lobbyFloor = lobby.getCurrentFloor();
            if(lobby.upRequested() && activeCalls[(lobbyFloor-1)*2] != 1){
                activeCalls[(lobbyFloor-1)*2] = 1;
                Topics.publish(bus, Topics.CAR_REQUEST, lobbyFloor, new int[]{2});
            } 
            if(lobby.downRequested() && activeCalls[((lobbyFloor-1)*2)+1] != 1){
                activeCalls[((lobbyFloor-1)*2)+1] = 1;
                Topics.publish(bus, Topics.CAR_REQUEST, lobbyFloor, new int[]{1});
            } 

            //check overload sensor
            overloaded = cabin.overloaded();
            if(overloaded != lastOverloaded){
                lastOverloaded = overloaded;
                int overloadedInt;
                if(overloaded){
                    overloadedInt = 1;
                } else {
                    overloadedInt = 0;
                }
                Topics.publish(bus, 4, ID, new int[]{overloadedInt});
            }

            //Bus block

            //Read motor commands
            Message command = null;
            command = bus.getMessage(new Topic(Topics.MOTOR_COMMAND,ID));
            if(command != null){
                handleMotorCommand(command);
            }

            command = null;
            command = bus.getMessage(new Topic(Topics.DOOR_COMMAND,ID));
            if(command != null){
                handleDoorCommand(command);
            }

            command = null;
            command = bus.getMessage(new Topic(Topics.CABIN_BUTTON_RESET,ID));
            if(command != null){
                handleCabinReset(command);
            }

            command = null;
            command = bus.getMessage(new Topic(Topics.CALL_BUTTON_RESET,ID));
            if(command != null){
                handleLobbyReset(command);
            }


        }
    }


    /* 
    //we're not using this currently since it seemed pointless without the while
    private Message pollDevices(Topic topic){
        Message message = null;
        while (message == null){
            message = bus.getMessage(topic);
        }
        return message;
    }
    */

    private void handleMotorCommand(Message message){
        int body = message.bodyOne();
        int topic = message.topicInt();
        switch (body){
            case 0:
                motor.setMotion(Motion.STOP);
                break;
            case 1:
                motor.setDirection(Direction.DOWN);
                motor.setMotion(Motion.START);
                break;
            case 2:
                motor.setDirection(Direction.UP);
                motor.setMotion(Motion.START);
                break;

        }
    }

    private void handleDoorCommand(Message message){
        int[] body = new int[]{message.bodyOne(), message.bodyTwo(), message.bodyThree(), message.bodyFour()};
        int topic = message.topicInt();
        //This works but it assumes the door just opens instantly which might be okay.
        switch (body[0]){
            case 0:
                cabin.setDoorState(DoorState.OPEN);
                break;
            case 1:
                if(cabin.obstructed()){
                    cabin.setDoorState(DoorState.OBSTRUCTED);
                } else {
                    cabin.setDoorState(DoorState.CLOSED);
                }
                break;

        }
    }

    private void handleCabinReset(Message message){
        int[] body = new int[]{message.bodyOne(), message.bodyTwo(), message.bodyThree(), message.bodyFour()};
        activeButtons[body[0]-1] = 0;
        cabin.resetSelection();
    }

    private void handleLobbyReset(Message message){
        int[] body = new int[]{message.bodyOne(), message.bodyTwo(), message.bodyThree(), message.bodyFour()};
        activeCalls[(body[0]-1)*2] = 0;
        activeCalls[((body[0]-1)*2)+1] = 0;
        cabin.resetSelection();
    }

    /*
     * Converts a DoorState enum into an int that we can send over the bus
     */
    private int DoorStateToInt(DoorState door){
        switch (door){
            case OPEN:
            return 0;
            case CLOSED:
            return 1;
            case OPENING:
            return 2;
            case CLOSING:
            return 3;
            case OBSTRUCTED:
            return 4;
            default:
            return -1;
            
        }
    }



}
