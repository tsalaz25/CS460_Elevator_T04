package tests;

import CommandCenter.CommandCenterTestHarness;
import javafx.application.Application;

/**
 * DemoScenario4 — Edge Case & Invalid Command Handling
 * This scenario stress-tests the Command Center by sending invalid or
 * boundary commands while other elevators are active. It checks that
 * the system ignores bad inputs gracefully, and that GUI
 * state remains consistent. This one’s mainly for robustness testing.
 */
public class DemoScenario4 {

    public static void main(String[] args) {
        new Thread(() -> Application.launch(CommandCenter.CommandCenterTestHarness.GUILauncher.class)).start();

        sleep(3000);

        CommandCenterTestHarness harness = new CommandCenterTestHarness();
        System.out.println("=== Demo Scenario 4: Edge Cases and Invalid Commands ===");

        harness.processCommand("door 1 closed");
        harness.processCommand("move 1 up 3");
        sleep(2000);

        // Invalid floor and direction commands
        harness.processCommand("move 1 up 15");       // beyond top floor
        harness.processCommand("move 2 down -1");     // invalid floor
        harness.processCommand("door 99 open");       // non-existent elevator
        sleep(2000);

        // Invalid mode and input
        harness.processCommand("mode flying");        // non-existent mode
        harness.processCommand("door something open");// malformed
        harness.processCommand("move");               // incomplete command
        sleep(2000);

        // Stress test with rapid valid commands
        for (int i = 0; i < 3; i++) {
            harness.processCommand("fire on");
            sleep(500);
            harness.processCommand("fire off");
            sleep(500);
        }

        // End of test
        harness.processCommand("door 1 open");
        System.out.println("=== Scenario complete: Edge-case test finished ===");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}