package CommandCenter;

import SoftwareBus.Bus.Topic;

import java.util.Arrays;

public class Topics {
//    General Topics
    public static final Topic CAR_REQUEST = new Topic(0, 0);
    public static final Topic ELEVATOR_POSITION = new Topic(1, 0);
    public static final Topic ELEVATOR_DIRECTION = new Topic(2, 0);
    public static final Topic ELEVATOR_DOOR_STATE = new Topic(3, 0);
    public static final Topic ELEVATOR_MODE = new Topic(7, 0);
    public static final Topic FIRE_KEY = new Topic(6, 0);
    public static final Topic FLOOR_SELECT = new Topic(8, 0);
    public static final Topic ELEVATOR_DISABLED = new Topic(13,0);

//    Topics with specific subtopics
    public static final Topic[] CAR_REQUESTS = {
        new Topic(CAR_REQUEST.topic(), 1),
        new Topic(CAR_REQUEST.topic(), 2),
        new Topic(CAR_REQUEST.topic(), 3),
        new Topic(CAR_REQUEST.topic(), 4),
        new Topic(CAR_REQUEST.topic(), 5),
        new Topic(CAR_REQUEST.topic(), 6),
        new Topic(CAR_REQUEST.topic(), 7),
        new Topic(CAR_REQUEST.topic(), 8),
        new Topic(CAR_REQUEST.topic(), 9),
        new Topic(CAR_REQUEST.topic(), 10)
    };
    public static final Topic[] ELEVATOR_POSITIONS = {
            new Topic(ELEVATOR_POSITION.topic(), 1),
            new Topic(ELEVATOR_POSITION.topic(), 2),
            new Topic(ELEVATOR_POSITION.topic(), 3),
            new Topic(ELEVATOR_POSITION.topic(), 4)
    };
    public static final Topic[] ELEVATOR_DIRECTIONS = {
            new Topic(ELEVATOR_DIRECTION.topic(), 1),
            new Topic(ELEVATOR_DIRECTION.topic(), 2),
            new Topic(ELEVATOR_DIRECTION.topic(), 3),
            new Topic(ELEVATOR_DIRECTION.topic(), 4)
    };
    public static final Topic[] ELEVATOR_DOOR_STATES = {
            new Topic(ELEVATOR_DOOR_STATE.topic(), 1),
            new Topic(ELEVATOR_DOOR_STATE.topic(), 2),
            new Topic(ELEVATOR_DOOR_STATE.topic(), 3),
            new Topic(ELEVATOR_DOOR_STATE.topic(), 4)
    };
    public static final Topic[] ELEVATOR_MODES = {
            new Topic(ELEVATOR_MODE.topic(), 1),
            new Topic(ELEVATOR_MODE.topic(), 2),
            new Topic(ELEVATOR_MODE.topic(), 3),
            new Topic(ELEVATOR_MODE.topic(), 4)
    };
    public static final Topic[] FLOOR_SELECTS = {
            new Topic(FLOOR_SELECT.topic(), 1),
            new Topic(FLOOR_SELECT.topic(), 2),
            new Topic(FLOOR_SELECT.topic(), 3),
            new Topic(FLOOR_SELECT.topic(), 4)
    };
    public static final Topic[] FIRE_KEYS = {
            new Topic(FIRE_KEY.topic(),1),
            new Topic(FIRE_KEY.topic(),2),
            new Topic(FIRE_KEY.topic(),3),
            new Topic(FIRE_KEY.topic(),4)
    };
    public static final Topic[] ELEVATORS_DISABLED = {
            new Topic(ELEVATOR_DISABLED.topic(),1),
            new Topic(ELEVATOR_DISABLED.topic(),2),
            new Topic(ELEVATOR_DISABLED.topic(),3),
            new Topic(ELEVATOR_DISABLED.topic(),4)
    };
}
