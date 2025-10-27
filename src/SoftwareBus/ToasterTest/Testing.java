package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Testing {

    public static void main(String[] args) {
        toasterExample();
//        otherExample();
    }

    private static void otherExample() {
        ConcurrentHashMap<Integer, ArrayList<Integer>> map = new ConcurrentHashMap<>();

        map.put(1, new ArrayList<>());
        map.put(2, new ArrayList<>());
        map.put(3, new ArrayList<>());

        System.out.println(map.get(1).size());


        Bus b = new Bus(1234);
        System.out.println("here1");


        Bus c1 = new Bus(1234);
        System.out.println(c1.getID());
        c1.subscribe(new Topic(1, 0));
        c1.subscribe(new Topic(2, 1));
        c1.subscribe(new Topic(3, 2));

        Bus c2 = new Bus(1234);
        System.out.println(c2.getID());
        c2.subscribe(new Topic(1, 0));
        c2.subscribe(new Topic(2, 0));

        Bus c3 = new Bus(1234);
        System.out.println(c3.getID());
        c3.subscribe(new Topic(1, 1));
        c3.subscribe(new Topic(3, 0));


        c1.publish(new Message(new int[]{1, 0, 4, 5}));
        c1.publish(new Message(new int[]{2, 0, 2, 6}));
        c1.publish(new Message(new int[]{3, 0, 9, 3}));
        c1.publish(new Message(new int[]{10, 0, 4, 5}));

        System.out.println(c1.getMessage(new Topic(1, 0)));
        System.out.println(c2.getMessage(new Topic(1, 0)));
        System.out.println(c3.getMessage(new Topic(1, 1)));
        System.out.println(c3.getMessage(new Topic(3, 0)));
    }

    private static void toasterExample() {
        /*
         * Topics: 1.1-Nob pushed
         * Topics: 2.1-Time remaining
         */
        Bus toasterServer = new Bus(7154);
        ToasterTimer.run();
        ToasterHeater.run();
        ToasterLED.run();
        ToasterNob.run();
    }
}
