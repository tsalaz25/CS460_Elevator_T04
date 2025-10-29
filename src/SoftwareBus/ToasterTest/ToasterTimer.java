package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

public class ToasterTimer {

    //The toaster timer processor has an instance of Bus
    private static Bus toasterBus = new Bus();

    //The toaster timer processor needs this internal state variable to function
    private static int timer = -1;

    //The toaster nob processor operates with this process:
    public static void run() {
        //Subscribe to updates on the toaster nob status
        toasterBus.subscribe(Topics.NOB_STATUS);
        //This thread allows the processor to run independently of the main method that called it
        new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //Get the oldest unread message on the subject of the timer status
                //  May be null if there are no unread messages
                Message nobUpdate = toasterBus.getMessage(Topics.NOB_STATUS);
                if (nobUpdate != null) {
                    //If the message was not null and the nob is pushed down
                    if (nobUpdate.bodyOne() == 1) {
                        timer = 5;
                        System.out.println("Timer started");
                    } else {
                        //If the message was not null and the nob is pushed up
                        timer = -1;
                        System.out.println("Timer Stopped");
                    }
                } else {
                    //If the message was null, we can assume the nob has not been moved and
                    //  countdown may continue
                    timer--;
                }
                if (timer >= 0) {
                    //While the timer is ticking, publish updates on the status of the timer
                    System.out.printf("Timer at %d \n", timer);
                    toasterBus.publish(new Message(Topics.TIMER_STATUS, new int[]{timer}));
                }
                //Lets only look for updates and make changes once a second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
