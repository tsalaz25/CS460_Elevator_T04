package CommandCenter.communicators;

import CommandCenter.Topics;
import CommandCenter.states.CallButtonSetState;
import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

import java.util.Arrays;

public class TerminalCommunicator {
    private final Bus bus = new Bus();
    private final CallButtonSetState[] callButtonSetStates = new CallButtonSetState[10];
    private final boolean[][] requestedFloorStates = new boolean[4][10];

    public TerminalCommunicator() {
        Arrays.fill(this.callButtonSetStates, CallButtonSetState.OFF);
        for (boolean[] requestedFloorState : requestedFloorStates) {
            Arrays.fill(requestedFloorState, false);
        }
        this.subscribe();

        new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //  Get the oldest unread message
                //  May be null if there are no unread messages

                //  Let's only look for updates and make changes ten times a second
                for (Topic topic : Topics.CAR_REQUESTS) {
                    Message carCall = this.bus.getMessage(topic);
                    if (carCall != null) {
                        int floor = carCall.subtopicInt() - 1;  // Convert 1-based floor to 0-based index
                        CallButtonSetState currentState = this.callButtonSetStates[floor];
                        switch (carCall.bodyOne()) {
                            case 1 -> {
                                switch (currentState) {
                                    case UP -> {
                                        this.callButtonSetStates[floor] = CallButtonSetState.BOTH;
                                    }
                                    case OFF -> {
                                        this.callButtonSetStates[floor] = CallButtonSetState.DOWN;
                                    }
                                    case DOWN, BOTH -> {}
                                }
                            }
                            case 2 -> {
                                switch (currentState) {
                                    case DOWN -> {
                                        this.callButtonSetStates[floor] = CallButtonSetState.BOTH;
                                    }
                                    case OFF -> {
                                        this.callButtonSetStates[floor] = CallButtonSetState.UP;
                                    }
                                    case UP, BOTH -> {}
                                }
                            }
                        }
                    }
                }

                for (Topic topic : Topics.FLOOR_SELECTS) {
                    Message requestButtonMessage = this.bus.getMessage(topic);
                    if (requestButtonMessage != null) {
                        int elevator = requestButtonMessage.subtopicInt();
                        int floor = requestButtonMessage.bodyOne();
                        // Convert 1-based elevator and floor to 0-based indices
                        this.requestedFloorStates[elevator - 1][floor - 1] = true;
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void subscribe() {
        this.bus.subscribe(Topics.CAR_REQUEST);
        this.bus.subscribe(Topics.FLOOR_SELECT);
    }

    public CallButtonSetState[] getCallButtonSetStates() {
        return this.callButtonSetStates;
    }

    public boolean[][] getRequestedFloorStates() {
        return this.requestedFloorStates;
    }
}
