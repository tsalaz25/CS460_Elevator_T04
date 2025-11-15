package Multiplexer;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

public class Topics {
    //Device outputs (Publish)
    //ints are per elevator and need the ID argument
    public static final int ELEVATOR_FLOOR = 1;
    public static final int ELEVATOR_DIRECTION = 2;
    public static final int ELEVATOR_DOOR = 3;
    public static final int ELEVATOR_OVERWEIGHT  = 4;
    public static final int FLOOR_SELECT = 8;

    //Bus Inputs (Subscribe)
    public static final Topic CAR_REQUEST = new Topic(0, 0);
    public static final int DOOR_COMMAND = 9;
    public static final int MOTOR_COMMAND = 10;
    public static final int CABIN_BUTTON_RESET = 11;
    public static final Topic CALL_BUTTON_RESET = new Topic(12, 0);

    // Additional topics for test compatibility
    public static final Topic FIRE_KEY = new Topic(6, 1);
    public static final Topic ELEVATOR_STATE = new Topic(7, 1);

    // Subscribes to all topics that the multiplexer needs to subscribe to
    public static void subscribeAll(Bus bus,int ID) {
        bus.subscribe(CAR_REQUEST);
        bus.subscribe(new Topic(DOOR_COMMAND,ID));
        bus.subscribe(new Topic(MOTOR_COMMAND,ID));
        bus.subscribe(CALL_BUTTON_RESET);
        bus.subscribe(new Topic(CABIN_BUTTON_RESET,ID));
    }

    public static void publish(Bus bus,Topic t,int[] body){
        bus.publish(new Message(t,body));
    }
}
