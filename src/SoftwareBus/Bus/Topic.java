package SoftwareBus.Bus;

public class Topic {

    /**
     * The topic portion of this topic object
     */
    private int topic;
    /**
     * The subtopic portion of this topic object
     */
    private int subtopic;

    /**
     * To instantiate a topic, provide the corresponding topic and subtopic ints
     *
     * @param topic    the topic to assign this topic instance
     * @param subtopic the subtopic to assign this topic instnace
     */
    public Topic(int topic, int subtopic) {
        this.topic = topic;
        this.subtopic = subtopic;
    }

    /**
     * Get the topic portion of this topic instance
     *
     * @return the integer associated with the topic
     */
    public int topic() {
        return topic;
    }

    /**
     * Get the subtopic portion of this topic instance
     *
     * @return the integer associated with the subtopic
     */
    public int subtopic() {
        return subtopic;
    }

    /**
     * Get a string representation of this topic instance in the following format
     * topic: t, subtopic, s
     * where t is the topic int and s is the subtopic int
     *
     * @return the string representation of this topic instance
     */
    public String toString() {
        return "topic: " + topic + ", subtopic: " + subtopic;
    }

    /**
     * Create a hash of this topic according to the following equation
     * 32 * t + s
     * where t is the topic number and s is the subtopic number
     *
     * @return the integer resulting from the above hash formula
     */
    public int hashCode() {
        return 31 * topic + subtopic;
    }

    /**
     * Determine if two topics are equal to each other
     *
     * @param o the reference object with which to compare.
     * @return true if the given object is a topic and their topic and subtopic
     * portions match each other, false otherwise.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Topic t = (Topic) o;
        return topic == t.topic && subtopic == t.subtopic;
    }
}

