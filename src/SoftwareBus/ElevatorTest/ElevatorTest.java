package SoftwareBus.ElevatorTest;

import SoftwareBus.Bus.*;

public class ElevatorTest {

    public static void main(String[] args) throws InterruptedException {
        Bus elevatorServer = new Bus(7154);

        HallButtons hb = new HallButtons();
        RandomElevator rb = new RandomElevator();
        ElevatorDoor ed = new ElevatorDoor();

        hb.run();
        rb.run();
        ed.run();
    }
}
