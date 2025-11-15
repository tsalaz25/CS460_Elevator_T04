package Multiplexer;

import SoftwareBus.Bus.Bus;
import SoftwareBus.Bus.Message;
import SoftwareBus.Bus.Topic;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Bus bus = new Bus();
        int elevatorId = 1;
        Topics.subscribeAll(bus, elevatorId);
        Multiplexer multiplexer = new Multiplexer(elevatorId, bus);
        bus.publish(new Message(Topics.CAR_REQUEST, new int[]{2}));
        bus.publish(new Message(Topics.FIRE_KEY, new int[]{0}));
        bus.publish(new Message(Topics.ELEVATOR_STATE, new int[]{1}));
        bus.publish(new Message(new Topic(Topics.ELEVATOR_DIRECTION, elevatorId), new int[]{2}));
        bus.publish(new Message(new Topic(Topics.ELEVATOR_DOOR, elevatorId), new int[]{1}));
        bus.publish(new Message(new Topic(Topics.ELEVATOR_OVERWEIGHT, elevatorId), new int[]{1}));
        bus.publish(new Message(new Topic(Topics.ELEVATOR_FLOOR, elevatorId), new int[]{2}));


        try {
            Thread.sleep(2000); // Give 200ms for thread startup and subscription
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Test interrupted during startup pause.");
            return;
        }
        Message carRequestMessage = null;

        while(carRequestMessage == null){
            carRequestMessage = bus.getMessage(Topics.CAR_REQUEST);
        }
        if (carRequestMessage != null) {
            int direction = carRequestMessage.bodyOne();
            System.out.print("Elevator requested at floor: " + carRequestMessage.subtopicInt());
            if (direction == 1){
                System.out.println(". Elevator moving down.");
            }
            else if (direction == 2){
                System.out.println(". Elevator moving up.");
            }

        }

        sc.nextLine();
        Message elevatorFloorMessage = null;
        while(elevatorFloorMessage == null){
            elevatorFloorMessage = bus.getMessage(new Topic(Topics.ELEVATOR_FLOOR, elevatorId));
        }
        if (elevatorFloorMessage != null) {
            int floor = elevatorFloorMessage.bodyOne();
            System.out.println("Elevator " + elevatorId + " is at floor: " + floor + ".");
        }

        sc.nextLine();
        Message elevatorDirectionMessage = null;
        while(elevatorDirectionMessage == null){
            elevatorDirectionMessage = bus.getMessage(new Topic(Topics.ELEVATOR_DIRECTION, elevatorId));
        }
        if (elevatorDirectionMessage != null) {
            int direction = elevatorDirectionMessage.bodyOne();
            System.out.print("Elevator " + elevatorId + " is moving ");
            switch (direction){
                case 0:
                    System.out.println("nowhere.");
                    break;
                case 1:
                    System.out.println("down.");
                    break;
                case 2:
                    System.out.println("up.");
                    break;
            }
        }

        sc.nextLine();
        Message elevatorDoorMessage = null;
        while(elevatorDoorMessage == null){
            elevatorDoorMessage = bus.getMessage(new Topic(Topics.ELEVATOR_DOOR, elevatorId));
        }
        if (elevatorDoorMessage != null) {
            int doorStatus = elevatorDoorMessage.bodyOne();
            System.out.print("Elevator " + elevatorId + " doors are ");
            switch (doorStatus){
                case 0:
                    System.out.println("open.");
                    break;
                case 1:
                    System.out.println("closed.");
                    break;
                case 2:
                    System.out.println("opening.");
                    break;
                case 3:
                    System.out.println("closing.");
                    break;
            }
        }

        sc.nextLine();
        Message overweightMessage = null;
        while(overweightMessage == null){
            overweightMessage = bus.getMessage(new Topic(Topics.ELEVATOR_OVERWEIGHT, elevatorId));
        }
        if (overweightMessage != null) {
            int overweightStatus = overweightMessage.bodyOne();
            System.out.print("Elevator " + elevatorId + " is");
            switch (overweightStatus){
                case 0:
                    System.out.println(" not overweight.");
                    break;
                case 1:
                    System.out.println(" overweight.");
                    break;
            }
        }
        sc.nextLine();
        Message fireKeyMessage = null;
        while(fireKeyMessage == null){
            fireKeyMessage = bus.getMessage(Topics.FIRE_KEY);
        }
        if (fireKeyMessage != null) {
            int fireKey = fireKeyMessage.bodyOne();
            System.out.print("Elevator " + elevatorId + " fire key is");
            switch (fireKey){
                case 0:
                    System.out.println(" not in.");
                    break;
                case 1:
                    System.out.println(" in.");
                    break;
            }
        }
        sc.nextLine();
        Message elevatorStateMessage = null;
        while(elevatorStateMessage == null){
            elevatorStateMessage = bus.getMessage(Topics.ELEVATOR_STATE);
        }
        if (elevatorStateMessage != null) {
            int elevatorState = elevatorStateMessage.bodyOne();
            System.out.print("Elevator " + elevatorId + " is ");
            switch (elevatorState){
                case 0:
                    System.out.println("disabled.");
                    break;
                case 1:
                    System.out.println("enabled.");
                    break;
            }
        }











    }



}
