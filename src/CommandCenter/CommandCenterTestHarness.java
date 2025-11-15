package CommandCenter;

import CommandCenter.states.ElevatorMode;
import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.*;

public class CommandCenterTestHarness {
    private final Bus bus;
    private final Scanner scanner; // To read test cases
    private final int[] elevatorFloors = new int[5]; // Track current floor for each elevator (1-4)
    private ElevatorMode currentMode = ElevatorMode.INDEPENDENT;
    private static CommandCenterTestHarness instance;

    public CommandCenterTestHarness() {
        this.bus = new Bus();
        this.scanner = new Scanner(System.in);
        // Initialize all elevators at floor 1
        for (int i = 1; i <= 4; i++) {
            elevatorFloors[i] = 1;
        }
        subscribeAll();
        startListener();
    }

    public static void main(String[] args) {
        instance = new CommandCenterTestHarness();

        // Launch GUI in separate thread
        new Thread(() -> {
            javafx.application.Application.launch(GUILauncher.class);
        }).start();

        // Give GUI time to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start the command line interface
        instance.startHarness();
    }

    // Inner class to launch JavaFX GUI
    public static class GUILauncher extends javafx.application.Application {
        @Override
        public void start(Stage primaryStage) {
            new ElevatorCommandCenterDisplay(primaryStage);
        }
    }

    private void subscribeAll() {
        for (int i = 1; i <= 4; i++) {
            bus.subscribe(new Topic(1, i));  // elevator position
            bus.subscribe(new Topic(2, i));  // elevator direction
            bus.subscribe(new Topic(3, i));  // elevator door state
            bus.subscribe(new Topic(6, i));  // fire key
            bus.subscribe(new Topic(8, i));  // request buttons (floor select)
        }

        for (int i = 1; i <= 10; i++) {
            bus.subscribe(new Topic(0, i));  // call buttons (car request)
            bus.subscribe(new Topic(13, i));  // elevator disabled
        }

        bus.subscribe(new Topic(5, 0)); // fire alarm
        bus.subscribe(new Topic(7, 0)); // elevator mode (system-wide)
    }

    private void startListener() {
        Thread listener = new Thread(() -> {
            while (true) {
                // Check per-elevator topics
                for (int i = 1; i <= 4; i++) {
                    Topic[] listenTopics = {
                            new Topic(1, i), new Topic(2, i), new Topic(3, i),
                            new Topic(6, i), new Topic(8, i),
                            new Topic(1, i), new Topic(2, i), new Topic(3, i), new Topic(6, i), new Topic(13, i)
                    };
                    for (Topic t : listenTopics) {
                        Message message = bus.getMessage(t);
                        if (message != null) {
                            printMessage(t, message);
                        }
                    }
                }

                // Check call button topics (floors 1-10)
                for (int i = 1; i <= 10; i++) {
                    Topic callTopic = new Topic(0, i);
                    Message message = bus.getMessage(callTopic);
                    if (message != null) {
                        printMessage(callTopic, message);
                    }
                }

                // Check system-wide topics
                Topic[] systemTopics = {
                        new Topic(5, 0),  // fire alarm
                        new Topic(7, 0)   // elevator mode
                };
                for (Topic t : systemTopics) {
                    Message message = bus.getMessage(t);
                    if (message != null) {
                        printMessage(t, message);
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private void printMessage(Topic t, Message message) {
        try {
            System.out.println("Topic[" + t.topic() + "," + t.subtopic() + "], Body: [" +
                    message.bodyOne() + ", " + message.bodyTwo() + ", " +
                    message.bodyThree() + ", " + message.bodyFour() + "]");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Handle messages with incomplete body arrays gracefully
            System.out.println("Topic[" + t.topic() + "," + t.subtopic() + "], Body: [" +
                    message.bodyOne() + ", <incomplete array>]");
        }
    }

    public void startHarness() {
        System.out.println("******* Command Center Test Harness *******");
        System.out.println("Type commands:");
        System.out.println("  door <id> <open|closed>");
        System.out.println("  move <id> <up|down> <floor>");
        System.out.println("  fire <on|off>");
        System.out.println("  mode <independent|centralized|fire>");
        System.out.println("  call <floor> <up|down>");
        System.out.println("  request <id> <floor>");
        System.out.println("  firekey <id> <out|in>");
        //System.out.println("  disabled <id> <true|false>"); // shouldn't be able to send disabled to the GUI?
        System.out.println("  exit");

        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            } else {
                handleCommand(command);
            }
        }
    }

    private void handleCommand(String command) {
        String[] segments = command.split("\\s+");
        if (segments.length == 0) {
            return;
        }

        try {
            if (segments[0].equals("door")) {
                handleDoor(segments);
            } else if (segments[0].equals("move")) {
                handleMove(segments);
            } else if (segments[0].equals("fire")) {
                handleFire(segments);
            } else if (segments[0].equals("mode")) {
                handleMode(segments);
            } else if (segments[0].equals("call")) {
                handleCall(segments);
            } else if (segments[0].equals("request")) {
                handleRequest(segments);
            } else if (segments[0].equals("firekey")) {
                handleFireKey(segments);
            } //else if (segments[0].equals("disabled")){  // shouldn't be able to send disabled to the GUI?
            //  handleElevatorDisabled(segments);
            //}
            else {
                System.out.println("Invalid command");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleDoor(String[] segments) {
        if (currentMode == ElevatorMode.FIRE) {
            System.out.println("ERROR: Cannot control doors in FIRE mode. Elevators are locked at floor 1 with doors open.");
            return;
        }

        int val = Integer.parseInt(segments[1]);
        String state = segments[2];
        int doorState = -1;
        if (state.equals("open")) {
            doorState = 0;
        } else if (state.equals("closed") || state.equals("close")) {
            doorState = 1;
        }
        bus.publish(new Message(new Topic(3, val), new int[]{doorState, 0, 0, 0}));
        System.out.println("<door, elevator, " + val + ":" + state + ", -, -, ->");
    }

    private void handleMove(String[] segments) {
        if (currentMode == ElevatorMode.FIRE) {
            System.out.println("ERROR: Cannot move elevators in FIRE mode. Elevators are locked at floor 1.");
            return;
        }

        int val = Integer.parseInt(segments[1]);
        String dir = segments[2];
        int floor = Integer.parseInt(segments[3]);

        // Validate floor range (1-10)
        if (floor < 1 || floor > 10) {
            System.out.println("ERROR: Invalid floor " + floor + ". Floor must be between 1 and 10.");
            return;
        }

        int currentFloor = elevatorFloors[val];
        int direction = -1;

        if (dir.equals("up")) {
            direction = 2;
            if (floor <= currentFloor) {
                System.out.println("ERROR: Cannot go UP to floor " + floor + ". Elevator " + val + " is currently at floor " + currentFloor + ".");
                return;
            }
        } else if (dir.equals("down")) {
            direction = 1;
            if (floor >= currentFloor) {
                System.out.println("ERROR: Cannot go DOWN to floor " + floor + ". Elevator " + val + " is currently at floor " + currentFloor + ".");
                return;
            }
        } else if (dir.equals("stop") || dir.equals("stationary")) {
            direction = 0;
        }

        // Update tracked position
        elevatorFloors[val] = floor;

        bus.publish(new Message(new Topic(2, val), new int[]{direction, 0, 0, 0}));
        bus.publish(new Message(new Topic(1, val), new int[]{floor, 0, 0, 0}));
        System.out.println("<elevator, motion, " + val + ":" + dir + " to " + floor + ", -, -, ->");
    }

    private void handleFire(String[] segments) {
        String state = segments[1];
        int fire = -1;
        if (state.equals("on")) {
            fire = 1;
        } else {
            fire = 0;
        }
        bus.publish(new Message(new Topic(5, 0), new int[]{fire, 0, 0, 0}));
        System.out.println("<fire alarm, 0:" + state + ", -, -, -, ->");
    }

    private void handleMode(String[] segments) {
        String elevatorMode = segments[1];
        ElevatorMode mode;
        if (elevatorMode.equals("independent")) {
            mode = ElevatorMode.INDEPENDENT;
        } else if (elevatorMode.equals("centralized")) {
            mode = ElevatorMode.CENTRALIZED;
        } else if (elevatorMode.equals("fire")) {
            mode = ElevatorMode.FIRE;
        } else {
            throw new IllegalArgumentException("Invalid elevator mode: " + elevatorMode);
        }

        // Update current mode
        currentMode = mode;

        // Mode is system-wide, not per-elevator, so use subtopic 0
        bus.publish(new Message(new Topic(7, 0), new int[]{mode.ordinal(), 0, 0, 0}));
        System.out.println("<mode, system:" + mode + ", -, -, -, ->");

        // If entering fire mode, send all elevators to floor 1 and open doors
        if (mode == ElevatorMode.FIRE) {
            // Activate fire alarm
            bus.publish(new Message(new Topic(5, 0), new int[]{1, 0, 0, 0}));

            // Send all elevators to floor 1 and open doors
            for (int i = 1; i <= 4; i++) {
                elevatorFloors[i] = 1; // Update tracked positions
                bus.publish(new Message(new Topic(2, i), new int[]{1, 0, 0, 0})); // direction: down
                bus.publish(new Message(new Topic(1, i), new int[]{1, 0, 0, 0})); // position: floor 1
                bus.publish(new Message(new Topic(3, i), new int[]{0, 0, 0, 0})); // door: open
            }
        }
    }

    private void handleCall(String[] segments) {
        int floor = Integer.parseInt(segments[1]);
        String dir = segments[2];

        // Validate floor range (1-10)
        if (floor < 1 || floor > 10) {
            System.out.println("ERROR: Invalid floor " + floor + ". Floor must be between 1 and 10.");
            return;
        }

        int direction = -1;
        if (dir.equals("up")) {
            direction = 2;
        } else if (dir.equals("down")) {
            direction = 1;
        } else {
            System.out.println("ERROR: Invalid direction '" + dir + "'. Use 'up' or 'down'.");
            return;
        }

        bus.publish(new Message(new Topic(0, floor), new int[]{direction, 0, 0, 0}));
        System.out.println("<call button, floor:" + floor + ", " + dir + ", -, -, ->");
    }

    private void handleRequest(String[] segments) {
        int elevator = Integer.parseInt(segments[1]);
        int floor = Integer.parseInt(segments[2]);

        // Validate elevator range (1-4)
        if (elevator < 1 || elevator > 4) {
            System.out.println("ERROR: Invalid elevator " + elevator + ". Elevator must be between 1 and 4.");
            return;
        }

        // Validate floor range (1-10)
        if (floor < 1 || floor > 10) {
            System.out.println("ERROR: Invalid floor " + floor + ". Floor must be between 1 and 10.");
            return;
        }

        bus.publish(new Message(new Topic(8, elevator), new int[]{floor, 0, 0, 0}));
        System.out.println("<request button, elevator:" + elevator + ", floor:" + floor + ", -, -, ->");
    }

    public void processCommand(String input) {
        handleCommand(input);
    }


    private void handleFireKey(String[] segments) {
        // which elevator shaft
        int shaft = Integer.parseInt(segments[1]);

        // is the key inserted or not
        int state = (segments[2].equals("in")) ? 1 : 0;

        // publish fire key message
        System.out.println("<fire key, " + shaft + ":" + state + ", -, -, -, ->");
        bus.publish(new Message(new Topic(6, shaft), new int[]{state, 0, 0, 0}));
    }

    /* // shouldn't be able to send disabled to GUI?
    private void handleElevatorDisabled(String[] segments)
    {
        // which elevator shaft
        int shaft = Integer.parseInt(segments[1]);

        // is the elevator disabled or not
        int state = (segments[2].equals("true")) ? 1 : 0;

        // publish elevator disabled message
        System.out.println("<elevator disabled, " + shaft + ":" + state + ", -, -, -, ->");
        bus.publish(new Message(new Topic(13,shaft), new int[]{state,0,0,0}));
    }
     */
}

