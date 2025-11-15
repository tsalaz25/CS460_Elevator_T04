package SoftwareBus.ToasterTest;

import SoftwareBus.Bus.Topic;

/**
 * This file describes all possible topics of the toaster system
 */
public class Topics {

    /*
     * This topic corresponds to the status of the toaster nob
     * The field b1 is 1 when the nob is pushed down, 0 otherwise
     */
    public static final Topic NOB_STATUS = new Topic(1, 1);

    /*
     * This topic corresponds to the status of the toaster timer
     * The field b1 is x when the timer has x seconds remaining
     */
    public static final Topic TIMER_STATUS = new Topic(2, 1);
}
