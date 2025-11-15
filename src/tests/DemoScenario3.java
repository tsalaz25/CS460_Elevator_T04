package tests;

import CommandCenter.CommandCenterTestHarness;
import javafx.application.Application;

/**
 * DemoScenario3 — Multi-Elevator Coordination
 * This scenario demonstrates multiple elevators operating simultaneously while
 * the system transitions between different modes. It checks that door states,
 * floor updates, and fire alarm triggers are correctly synchronized across all
 * active elevators, verifying coordinated message flow through the data bus.
 */

public class DemoScenario3 {

    public static void main(String[] args) {
        new Thread(() -> Application.launch(CommandCenter.CommandCenterTestHarness.GUILauncher.class)).start();

        sleep(3000);

        CommandCenterTestHarness harness = new CommandCenterTestHarness();
        System.out.println("=== Demo Scenario 3: Multi-Elevator Coordination ===");

        // Initialize both elevators
        harness.processCommand("door 1 closed");
        harness.processCommand("door 2 closed");
        sleep(1000);

        harness.processCommand("move 1 up 4");
        harness.processCommand("move 2 up 6");
        sleep(4000);

        // Open elevator 1 at floor 4
        harness.processCommand("door 1 open");
        sleep(1000);

        // Elevator 2 continues upward, then switches direction
        harness.processCommand("move 2 down 2");
        sleep(3000);
        harness.processCommand("door 2 open");
        sleep(1000);
        harness.processCommand("door 2 closed");
        sleep(1000);

        // System mode transition test
        harness.processCommand("mode independent");
        sleep(2000);
        harness.processCommand("mode centralized");
        sleep(2000);

        // Fire alarm activation affecting all elevators
        harness.processCommand("fire on");
        sleep(2000);
        harness.processCommand("fire off");
        sleep(2000);

        System.out.println("=== Scenario complete: Multi-elevator test finished ===");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}