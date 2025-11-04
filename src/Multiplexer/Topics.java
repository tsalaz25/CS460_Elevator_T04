package Multiplexer;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Topic;

public class Topics {
    public static final Topic CAR_REQUEST = new Topic(0, 5);
    public static final Topic ELEVATOR_FLOOR = new Topic(1, 1) ;
    public static final Topic ELEVATOR_DIRECTION = new Topic(2, 1) ;
    public static final Topic ELEVATOR_DOOR = new Topic(3, 1) ;
    public static final Topic ELEVATOR_OVERWEIGHT  = new Topic(4, 1) ;
    public static final Topic FIRE_KEY = new Topic(6,1);
    public static final Topic ELEVATOR_STATE = new Topic(7,1);


    public static void subscribeAll(Bus bus) {
        bus.subscribe(CAR_REQUEST);
        bus.subscribe(ELEVATOR_FLOOR);
        bus.subscribe(ELEVATOR_DIRECTION);
        bus.subscribe(ELEVATOR_DOOR);
        bus.subscribe(ELEVATOR_OVERWEIGHT);
        bus.subscribe(FIRE_KEY);
        bus.subscribe(ELEVATOR_STATE);

    }
}
