package SoftwareBus.ElevatorTest;

import SoftwareBus.Bus.*;

public class RandomElevator {
    private final Bus elevator = new Bus(7154);
    private boolean idleInShaft = true;

    public RandomElevator() {
        elevator.subscribe(new Topic(1, 0));
    }

    public void run() {
        new Thread(() -> {

            // Await message
            while (true) {
                try {
                    Message m = elevator.getMessage(new Topic(1, 0));
                    if (!idleInShaft || m == null) continue;
                    idleInShaft = false;

                    Thread.sleep(1000);
                    System.out.println("RB: Moving from " + m.bodyOne() + " to floor " + m.bodyTwo());
                    elevator.publish(new Message(new int[]{2, 3, 0, 0, 0, 0}));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
