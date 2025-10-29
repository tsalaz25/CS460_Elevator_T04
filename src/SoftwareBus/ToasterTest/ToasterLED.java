package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;

public class ToasterLED {
    static Bus toasterLED = new Bus();
    static boolean ledStatus = false;

    //The toaster LED is turned on when the nob is down, and turned off when the nob is up
    public static void run() {
        new Thread(() -> {
            toasterLED.subscribe(Topics.NOB_STATUS);
            while (true) {
                Message LEDUpdate = toasterLED.getMessage(Topics.NOB_STATUS);
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
