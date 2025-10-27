package SoftwareBus.Bus;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    /**
     * As specified in class, the contents of a message are stored as an array of integers
     */
    private int[] contents;

    /**
     * To create a message, provide an integer array of the following format
     * {t,s,b1,b2,b3,b4}
     * where t is the topic of the message, s is the subtopic of the message
     * b1...b4 are the contents of the message
     *
     * @param contents the integer array specifying the message contents
     */
    public Message(int[] contents) {
        this.contents = contents;
    }

    /**
     * Get the topic contained in this message
     *
     * @return the integer topic number of this message
     */
    public int topic() {
        return contents[0];
    }

    /**
     * Get the subtopic contained in this message
     *
     * @return the integer subtopic number of this message
     */
    public int subtopic() {
        return contents[1];
    }

    /**
     * Get the message body of this message
     *
     * @return an integer array containing the b1...b4 fields of the message
     */
    public int[] fullBody() {
        return new int[]{contents[2], contents[3], contents[4], contents[5]};
    }

    /**
     * Get the b1 field of this message
     *
     * @return the integer encoding the b1 field
     */
    public int bodyOne() {
        return contents[2];
    }

    /**
     * Get the b2 field of this message
     *
     * @return the integer encoding the b2 field
     */
    public int bodyTwo() {
        return contents[3];
    }

    /**
     * Get the b3 field of this message
     *
     * @return the integer encoding the b3 field
     */
    public int bodyThree() {
        return contents[4];
    }

    /**
     * Get the b4 field of this message
     *
     * @return the integer encoding the b4 field
     */
    public int bodyFour() {
        return contents[5];
    }

    /**
     * Get a string representation of the contents of this message in the following format:
     * topic: t, subtopic: s, BODY: b1, b2, b3, b4
     * where t is the topic, s is the subtopic and b1..b4 are the body fields
     *
     * @return the string representation of this message
     */
    public String toString() {
        return "topic: " + topic() + ", subtopic: " + subtopic() + ", BODY: " + Arrays.toString(contents);
    }


}
