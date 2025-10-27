package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;


public class ToasterHeater {
    static Bus toasterHeater = new Bus(7154);
    static boolean heater = false;

    //The toaster heater is turned on when the nob is down, and off when the nob is up
    public static void run() {
        new Thread(() -> {
            toasterHeater.subscribe(new Topic(1, 1));
            while (true) {
                Message heaterUpdate = toasterHeater.getMessage(new Topic(1, 1));
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
