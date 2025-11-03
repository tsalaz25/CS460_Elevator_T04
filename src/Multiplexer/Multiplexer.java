package Multiplexer;
import SoftwareBus.Bus.*;


public class Multiplexer {
    private final int ID;
    //Probably should be an enum but i don't know what modes we're using still
    private int mode = 0;

    //Public test values
    public int testFloor = 2;
    public int testDirection = 1;
    public int testDoor = 1;

    //Constant topics
    //Outputs
    private final Topic ELEVATOR_FLOOR;
    private final Topic ELEVATOR_DIRECTION;
    private final Topic ELEVATOR_DOOR;
    private final Topic ELEVATOR_OVERWEIGHT;
    private final Topic FLOOR_COMMAND;

    //Inputs
    private final Topic ELEVATOR_MODE;
    private final Topic FIRE = new Topic(5, 0);

    //TODO: we need to save the devices here im not sure what they're called yet
    //Possible objects
    // private MotorAssembly = motor;
    // private ElevatorControl = elevator;
    private Bus bus = new Bus();

    /*
     * Constructs a Multiplexer and then begins polling for input from devices and from 
     * 
     * 
     */
    public Multiplexer(int ID){
        this.ID = ID;
        ELEVATOR_FLOOR = new Topic(1,ID);
        ELEVATOR_DIRECTION = new Topic(2,ID);
        ELEVATOR_DOOR = new Topic(3,ID);
        ELEVATOR_OVERWEIGHT = new Topic(4,ID);
        ELEVATOR_MODE = new Topic(7,ID); 
        //TODO: No idea what this Topic will be or if we actually need it
        FLOOR_COMMAND = new Topic(50,ID); 


        new Thread(() -> {
            bus.subscribe(ELEVATOR_MODE);
            bus.subscribe(FIRE);
            //TODO: Subscribe to command center info? I'm not fully sure what topic we're subscribing to here
            //Also maybe fire alarm?
            //bus.subscribe();
            while(true){

                //Output block
                pollFloor();
                pollDirection();
                pollDoors();

                //Input block
                Message modeUpdate = bus.getMessage(ELEVATOR_MODE);
                // temp invalid 
                if(modeUpdate != null){
                    mode = modeUpdate.bodyOne();
                }

                Message fireStatus = bus.getMessage(FIRE);
                    if(modeUpdate != null){
                    mode = modeUpdate.bodyOne();
                }

                //TODO: put actual mode argument here
                if(mode == 1){
                    Message command = bus.getMessage(FLOOR_COMMAND);
                    if(command != null){
                        //TODO: Move to desired floor
                        testFloor = command.bodyOne();
                    }
                }
                
            }
        });
    }

    private void pollFloor(){
        int floor = -1;
        //TODO: Get floor info from sensors
        if(floor != -1){
            floor = testFloor;
            bus.publish(new Message(ELEVATOR_FLOOR,new int[]{testFloor}));
        }
    }

    private void pollDirection(){
        int direction = -1;
        //TODO: Get direction info from elevator
        if(direction != -1){
            direction = testDirection;
            bus.publish(new Message(ELEVATOR_DIRECTION,new int[]{testDirection}));
        }
    }

    private void pollDoors(){
        int doorStatus = -1;
        //TODO: Get door info from elevator
        if(doorStatus != -1){
            doorStatus = testDoor;
            bus.publish(new Message(ELEVATOR_DOOR,new int[]{testDoor}));
        }
    }

}
