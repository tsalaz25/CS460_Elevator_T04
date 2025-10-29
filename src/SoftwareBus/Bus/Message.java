package SoftwareBus.Bus;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    /**
     * As specified in class, the contents of a message are stored as an array of integers
     */
    private int[] bodyContents;
    /**
     * The topic of this message instance
     */
    private Topic messageTopic;

    /**
     * To create a message, provide an integer array of the following format
     * {t,s,b1,b2,b3,b4}
     * where t is the topic of the message, s is the subtopic of the message
     * b1...b4 are the contents of the message
     *
     * @param contents the integer array specifying the message contents
     */
    public Message(int[] contents) {
        this.messageTopic = new Topic(contents[0],contents[1]);
        this.bodyContents = new int[4];
        for(int i = 2; i < contents.length; i++) {
            bodyContents[i-2] = contents[i];
        }
    }

    /**
     * Given the topic of a message and the body of contents of the message, create a new
     * instance of message
     * @param t the topic of the message to be created
     * @param body the body contents of the message, should have at most 4 indexes
     */
    public Message(Topic t, int[] body) {
        this.messageTopic=t;
        this.bodyContents = body;
    }

    /**
     * Get the topic contained in this message
     *
     * @return the integer topic number of this message
     */
    public int topicInt() {
        return messageTopic.topic();
    }

    /**
     * Get the subtopic contained in this message
     *
     * @return the integer subtopic number of this message
     */
    public int subtopicInt() {
        return messageTopic.subtopic();
    }

    /**
     * Get the Topic instance containing topic and subtopic attributes of this message instance
     * @return the Topic instance of this message
     */
    public Topic getTopics(){
        return messageTopic;
    }

    /**
     * Get the message body of this message
     *
     * @return an integer array containing the b1...b4 fields of the message
     */
    public int[] fullBody() {
        return new int[]{bodyContents[0], bodyContents[1], bodyContents[2], bodyContents[3]};
    }

    /**
     * Get the b1 field of this message
     *
     * @return the integer encoding the b1 field
     */
    public int bodyOne() {
        return bodyContents[0];
    }

    /**
     * Get the b2 field of this message
     *
     * @return the integer encoding the b2 field
     */
    public int bodyTwo() {
        return bodyContents[1];
    }

    /**
     * Get the b3 field of this message
     *
     * @return the integer encoding the b3 field
     */
    public int bodyThree() {
        return bodyContents[2];
    }

    /**
     * Get the b4 field of this message
     *
     * @return the integer encoding the b4 field
     */
    public int bodyFour() {
        return bodyContents[3];
    }

    /**
     * Get a string representation of the contents of this message in the following format:
     * topic: t, subtopic: s, BODY: b1, b2, b3, b4
     * where t is the topic, s is the subtopic and b1..b4 are the body fields
     *
     * @return the string representation of this message
     */
    public String toString() {
        return "topic: " + topicInt() + ", subtopic: " + subtopicInt() + ", BODY: " + Arrays.toString(bodyContents);
    }


}
