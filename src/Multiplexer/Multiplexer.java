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
            Topics.subscribeAll(bus,ID);

            //TODO: Subscribe to command center info? I'm not fully sure what topic we're subscribing to here
            //Also maybe fire alarm?
            //bus.subscribe();
            while(true){

                //Output block
                System.out.println((pollDevices(Topics.CAR_REQUEST).bodyOne()));
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



    private Message pollDevices(Topic topic){
        Message message = null;
        while (message == null){
            message = bus.getMessage(topic);
        }
        return message;
    }
    private void handleMotorCommand(Message message){
        int body = message.bodyOne();
        int topic = message.topicInt();
        //TODO add motor control logic
        switch (body){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;

        }
    }
    private void handleDoorCommand(Message message){
        int[] body = new int[]{message.bodyOne(), message.bodyTwo(), message.bodyThree(), message.bodyFour()};
        int topic = message.topicInt();
        //TODO add door control logic
        switch (body[0]){
            case 0:
                break;
            case 1:
                break;

        }
    }



}
