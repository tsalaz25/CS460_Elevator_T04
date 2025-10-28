package SoftwareBus.ElevatorTest;

import SoftwareBus.Bus.*;

public class ElevatorDoor {
    private final Bus door = new Bus(7154);
    private boolean doorsClosed = true;

    public ElevatorDoor() {
        door.subscribe(new Topic(2, 3));
    }

    public void run() {
        new Thread(() -> {

            // Await message
            while (true) {
                try {
                    Message m = door.getMessage(new Topic(2, 3));
                    if (!doorsClosed || m == null) continue;
                    doorsClosed = false;
                    int floor = m.subtopic(); // Arbitrarily assigning subtopic to floor number, this may change

                    Thread.sleep(1000);
                    System.out.println("\nED: Doors on floor " + floor + " are closed.");

                    // Simulate doors opening
                    Thread.sleep(400);
                    System.out.println("ED: Doors at floor " + floor + " are now open.");
                    Thread.sleep(400);
                    System.out.println("ED: Doors at floor " + floor + " are now closed.");

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
