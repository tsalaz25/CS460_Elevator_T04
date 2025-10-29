package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;

public class ToasterHeater {

    //The toaster heater processor has an instance of Bus
    private static Bus toasterBus = new Bus();

    //The toaster heater processor needs this internal state variable to function
    private static boolean heaterOn = false;

    //The toaster heater processor operates with this process:
    public static void run() {
        //Subscribe to the status of the nob
        toasterBus.subscribe(Topics.NOB_STATUS);
        //This thread allows the processor to run independently of the main method that called it
        new Thread(() -> {
            //Continuously check for updates and make changes accordingly
            while (true) {
                //Get the oldest unread message on the subject of the nob status
                //  May be null if there are no unread messages
                Message nobUpdate = toasterBus.getMessage(Topics.NOB_STATUS);
                if (nobUpdate != null) {
                    //If the message was not null and the nob is pushed down
                    if (nobUpdate.bodyOne() == 1) {
                        System.out.println("Turning heater on");
                        heaterOn = true;
                    } else {
                        //If the message was not null and the nob is pushed up
                        System.out.println("Turning heater off");
                        heaterOn = false;
                    }
                }
                //If the heater is currently on, print a message (for clarity)
                if (heaterOn) System.out.println("Heating...");
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
