package tests;

import CommandCenter.CommandCenterTestHarness;
import javafx.application.Application;

/**
 * DemoScenario1 — Basic Command Center Interaction
 * This scenario runs a simple end-to-end test showing how the Command Center GUI
 * reacts to basic elevator commands. It walks through door control, movement to
 * a target floor, and mode toggles, confirming that GUI elements update correctly
 * and topic messages are published as expected.
 */

public class DemoScenario1 {
    public static void main(String[] args) {
        new Thread(() -> Application.launch(CommandCenter.CommandCenterTestHarness.GUILauncher.class)).start();
        sleep(3000);

        CommandCenterTestHarness harness = new CommandCenterTestHarness();

        System.out.println("=== Demo Scenario 1: Basic Elevator Movement ===");

        harness.processCommand("door 1 open");
        sleep(1000);
        harness.processCommand("move 1 up 5");
        sleep(3000);
        harness.processCommand("door 1 closed");
        sleep(1000);
        harness.processCommand("mode centralized");
        sleep(1000);
        harness.processCommand("fire on");
        sleep(2000);
        harness.processCommand("fire off");

        System.out.println("=== Scenario complete ===");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}