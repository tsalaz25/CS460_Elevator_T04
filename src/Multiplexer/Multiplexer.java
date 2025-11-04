package Multiplexer;
import SoftwareBus.Bus.*;


public class    Multiplexer {
    private final int ID;
    //Probably should be an enum but i don't know what modes we're using still
    private int mode = 0;

    //Public test values
    public int testFloor = 2;
    public int testDirection = 1;
    public int testDoor = 1;

    //Constant topics
    //Outputs
//    private final Topic ELEVATOR_FLOOR;
//    private final Topic ELEVATOR_DIRECTION;
//    private final Topic ELEVATOR_DOOR;
//    private final Topic ELEVATOR_OVERWEIGHT;
//    private final Topic FLOOR_COMMAND;
//
//    //Inputs
//    private final Topic ELEVATOR_MODE;
    private final Topic FIRE = new Topic(5, 0);

    //TODO: we need to save the devices here im not sure what they're called yet
    //Possible objects
    // private MotorAssembly = motor;
    // private ElevatorControl = elevator;
    private Bus bus;
    /*
     * Constructs a Multiplexer and then begins polling for input from devices and from
     *
     *
     */
    public Multiplexer(int ID, Bus bus) {
        this.ID = ID;
        this.bus = bus;

//        ELEVATOR_FLOOR = new Topic(1,ID);
//        ELEVATOR_DIRECTION = new Topic(2,ID);
//        ELEVATOR_DOOR = new Topic(3,ID);
//        ELEVATOR_OVERWEIGHT = new Topic(4,ID);
//        ELEVATOR_MODE = new Topic(7,ID);
//        //TODO: No idea what this Topic will be or if we actually need it
//        FLOOR_COMMAND = new Topic(50,ID);


        new Thread(() -> {
            Topics.subscribeAll(bus);

            //TODO: Subscribe to command center info? I'm not fully sure what topic we're subscribing to here
            //Also maybe fire alarm?
            //bus.subscribe();
            while(true){

                //Output block
                pollRequest();
                pollFloor();
                pollDirection();
                pollDoors();
                pollFireKey();
                pollOverweight();
                pollState();
                //Input block
//                Message modeUpdate = bus.getMessage(Topics.ELEVATOR_MODE);
//                // temp invalid
//                if(modeUpdate != null){
//                    mode = modeUpdate.bodyOne();
//                }
//
//                Message fireStatus = bus.getMessage(FIRE);
//                    if(modeUpdate != null){
//                    mode = modeUpdate.bodyOne();
//                }
//
//                //TODO: put actual mode argument here
//                if(mode == 1){
//                    Message command = bus.getMessage(FLOOR_COMMAND);
//                    if(command != null){
//                        //TODO: Move to desired floor
//                        testFloor = command.bodyOne();
//                    }
//                }

            }
        }).start();
    }

    private void pollFloor(){
        int floor = -1;
        //TODO: Get floor info from sensors
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.ELEVATOR_FLOOR);
        }
        floor = message.bodyOne();
        if(floor != -1){
            bus.publish(new Message(Topics.ELEVATOR_FLOOR,new int[]{floor}));
        }
    }

    private void pollDirection(){
        int direction = -1;
        //TODO: Get direction info from elevator
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.ELEVATOR_DIRECTION);
        }
        direction = message.bodyOne();
        if(direction != -1){
            bus.publish(new Message(Topics.ELEVATOR_DIRECTION,new int[]{direction}));
        }
    }

    private void pollDoors(){
        int doorStatus = -1;
        //TODO: Get door info from elevator
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.ELEVATOR_DOOR);
        }
        doorStatus = message.bodyOne();
        if(doorStatus != -1){
            bus.publish(new Message(Topics.ELEVATOR_DOOR,new int[]{doorStatus}));
        }
    }
    private void pollRequest(){
        int floorRequest = -1;
        Message requestMessage = null;
        while(requestMessage == null){
            requestMessage = bus.getMessage(Topics.CAR_REQUEST);
        }
        floorRequest = requestMessage.bodyOne();
        if(floorRequest != -1){
            bus.publish(new Message(Topics.CAR_REQUEST,new int[]{floorRequest}));
        }
    }
    private void pollFireKey(){
        int fireKey = -1;
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.FIRE_KEY);
        }
        fireKey = message.bodyOne();
        if(fireKey != -1){
            bus.publish(new Message(Topics.FIRE_KEY,new int[]{fireKey}));
        }
    }
    private void pollOverweight(){
        int overweight = -1;
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.ELEVATOR_OVERWEIGHT);
        }
        overweight = message.bodyOne();
        if(overweight != -1){
            bus.publish(new Message(Topics.ELEVATOR_OVERWEIGHT,new int[]{overweight}));
        }
    }
    private void pollState(){
        int state = -1;
        Message message = null;
        while(message == null){
            message = bus.getMessage(Topics.ELEVATOR_STATE);
        }
        state = message.bodyOne();
        if(state != -1){
            bus.publish(new Message(Topics.ELEVATOR_STATE,new int[]{state}));
        }
    }


}
