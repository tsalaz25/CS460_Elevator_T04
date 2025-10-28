package SoftwareBus.ElevatorTest;

import SoftwareBus.Bus.*;

public class HallButtons {
    private final Bus hall = new Bus(7154);
    private boolean buttonPressed = true;

    public void run() {
        new Thread(() -> {
            try {
                while (true) {
                    if (!buttonPressed) continue;
                    Thread.sleep(1000);
                    System.out.println("HB: Up pressed");

                    hall.publish(new Message(new int[]{1, 0, 1, 3, 0, 0}));
                    buttonPressed = false;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
