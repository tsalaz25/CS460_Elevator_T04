package SoftwareBus.ToasterTest;

public class Testing {

    public static void main(String[] args) {
        //Call run methods of the processors timer, heater, LED, and nob
        ToasterTimer.run();
        ToasterHeater.run();
        ToasterLED.run();
        ToasterNob.run();
    }
}
