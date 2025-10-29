package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;

public class ToasterNob {

    //The toaster nob processor has an instance of Bus
    private static Bus toasterBus = new Bus();

    //The toaster nob processor needs this internal state variable to function
    private static boolean nobDown = true;

    //Just some constants instead of re-declaring a new message everytime we publish
    private static final Message NOB_DOWN_MESSAGE = new Message(Topics.NOB_STATUS, new int[]{1});
    private static final Message NOB_UP_MESSAGE = new Message(Topics.NOB_STATUS, new int[]{0});

    //The toaster nob processor operates with this process:
    public static void run() {
        //Subscribe to updates on the toaster timer status
        toasterBus.subscribe(Topics.TIMER_STATUS);
        //Start this test with the nob being pushed down
        nobDown = true;
        toasterBus.publish(NOB_DOWN_MESSAGE);
        System.out.println("Nob Pushed Down");
        //This thread allows the processor to run independently of the main method that called it
        new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //Get the oldest unread message on the subject of the timer status
                //  May be null if there are no unread messages
                Message timerUpdate = toasterBus.getMessage(Topics.TIMER_STATUS);
                if (timerUpdate != null) {
                    if (timerUpdate.bodyOne() == 0) {
                        //If the message was not null, and the timer reached 0
                        nobDown = false;
                        toasterBus.publish(NOB_UP_MESSAGE);
                        System.out.println("Nob Released Up");
                    }
                }
                //Lets only look for updates and make changes ten times a second
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
