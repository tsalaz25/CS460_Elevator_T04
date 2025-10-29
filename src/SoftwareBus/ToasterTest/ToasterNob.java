package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;

public class ToasterNob {
    static Bus toasterNob = new Bus();
    static boolean nobDown = true;

    //The toaster nob is pushed up when the timer concludes, and publishes
    //messages when it is pushed up or down
    public static void run() {
        new Thread(() -> {
            toasterNob.subscribe(Topics.TIMER_STATUS);
            toasterNob.publish(new Message(Topics.NOB_STATUS, new int[]{1, 0, 0, 0}));
            System.out.println("nob pushed down");
            while (true) {
                Message nobUpdate = toasterNob.getMessage(Topics.TIMER_STATUS);
                if (nobUpdate != null) {
                    if ((nobUpdate.bodyOne() == 0) && nobDown) {
                        System.out.println("nob pushed up");
                        nobDown = false;
                        toasterNob.publish(new Message(Topics.NOB_STATUS, new int[]{0, 0, 0, 0}));
                    }
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
