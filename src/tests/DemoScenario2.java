package tests;

import CommandCenter.CommandCenterTestHarness;
import javafx.application.Application;

/**
 * DemoScenario2 — Fire Mode Recovery
 * This scenario simulates a fire alarm event interrupting normal elevator movement.
 * It verifies how the system transitions into FIRE mode, returns the elevator to
 * the ground floor, and safely restores operation once the alarm clears. This
 * ensures proper handling of emergency protocols and system recovery.
 */

public class DemoScenario2 {
    public static void main(String[] args) {
        // Launch GUI
        new Thread(() -> Application.launch(CommandCenter.CommandCenterTestHarness.GUILauncher.class)).start();

        sleep(3000); // Give GUI time

        CommandCenterTestHarness harness = new CommandCenterTestHarness();
        System.out.println("=== Demo Scenario 2: Fire Mode Recovery ===");

        harness.processCommand("door 2 closed");
        sleep(1000);
        harness.processCommand("move 2 up 8");
        sleep(2500);
        harness.processCommand("fire on");
        sleep(1000);
        harness.processCommand("move 2 down 1");
        sleep(3000);
        harness.processCommand("fire off");
        sleep(1000);
        harness.processCommand("mode centralized");
        sleep(1000);
        harness.processCommand("door 2 open");

        System.out.println("=== Scenario complete ===");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}