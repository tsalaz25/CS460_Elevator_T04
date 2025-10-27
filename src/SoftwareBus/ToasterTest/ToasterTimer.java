package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

public class ToasterTimer {

    private static Bus toasterTimer = new Bus(7154);
    static int timer = -1;

    //The toaster timer turns on when the nob is pushed down and counts down from
    //5. Each timer update publishes a message with its new time. Pushing up the
    //nob resets the timer
    public static void run() {
        new Thread(() -> {
            toasterTimer.subscribe(new Topic(1, 1));
            while (true) {
                Message timerUpdate = toasterTimer.getMessage(new Topic(1, 1));
                if (timerUpdate == null || timer > 0) {
                    timer--;
                }
                if (timerUpdate != null) {
                    if (timerUpdate.bodyOne() == 1 && timer < 0) {
                        timer = 5;
                        System.out.println("starting timer");
                    } else if (timerUpdate.bodyOne() == 0 && timer == 0) {
                        timer = -1;
                        System.out.println("timer turned off");
                    }
                }
                toasterTimer.publish(new Message(new int[]{2, 1, timer, 0, 0, 0}));
                if (timer > 0) {
                    System.out.println("timer at " + timer);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
