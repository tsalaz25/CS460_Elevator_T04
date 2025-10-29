package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;

public class ToasterLED {

    //The toaster LED processor has an instance of Bus
    private static Bus toasterBus = new Bus();

    //The toaster LED processor needs this internal state variable to function
    private static boolean ledStatus = false;

    //The toaster LED processor operates with this process:
    public static void run() {
        //Subscribe to updates on the toaster nob status
        toasterBus.subscribe(Topics.NOB_STATUS);
        //This thread allows the processor to run independently of the main method that called it
        new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //Get the oldest unread message on the subject of the nob status
                //  May be null if there are no unread messages
                Message nobUpdate = toasterBus.getMessage(Topics.NOB_STATUS);
                if (nobUpdate != null) {
                    if (nobUpdate.bodyOne() == 1) {
                        //If the message was not null and the nob is pushed down
                        ledStatus = true;
                        System.out.println("Turning LED on");
                    } else {
                        //If the message was not null and the nob is pushed up
                        ledStatus = false;
                        System.out.println("Turning LED off");
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
