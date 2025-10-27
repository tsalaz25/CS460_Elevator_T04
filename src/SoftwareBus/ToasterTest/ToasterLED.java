package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

public class ToasterLED {
    static Bus toasterLED = new Bus(7154);
    static boolean ledStatus = false;

    //The toaster LED is turned on when the nob is down, and turned off when the nob is up
    public static void run() {
        new Thread(() -> {
            toasterLED.subscribe(new Topic(1, 1));
            while (true) {
                Message LEDUpdate = toasterLED.getMessage(new Topic(1, 1));
                if (LEDUpdate != null) {
                    if ((LEDUpdate.bodyOne() == 1) != ledStatus) {
                        System.out.print("toggling LED ");
                        ledStatus = !ledStatus;
                        System.out.println(ledStatus ? "on" : "off");
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
