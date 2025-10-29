package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;


public class ToasterHeater {
    static Bus toasterHeater = new Bus();
    static boolean heater = false;

    //The toaster heater is turned on when the nob is down, and off when the nob is up
    public static void run() {
        new Thread(() -> {
            toasterHeater.subscribe(Topics.NOB_STATUS);
            while (true) {
                Message heaterUpdate = toasterHeater.getMessage(Topics.NOB_STATUS);
                if (heaterUpdate != null) {
                    if ((heaterUpdate.bodyOne() == 1) && !heater) {
                        System.out.println("Turning heater on");
                        heater = true;
                    } else if (heater && heaterUpdate.bodyOne() == 0) {
                        System.out.println("Turning heater off");
                        heater = false;
                    }
                }
                if (heater) {
                    System.out.println("heating...");
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
