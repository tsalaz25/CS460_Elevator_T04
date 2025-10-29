package SoftwareBus.Bus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class Bus {
    /**
     * The queue of messages in your inbox
     *
     */
    private BlockingQueue<Message> queue;

    /**
     * The list of topics that you are subscribed to
     */
    private CopyOnWriteArrayList<Topic> subscriptions;
    /**
     * You connect to the message distributor
     */
    private MessageDistributor distributor;

    /**
     * Method to make a new bus. Each bus will have a queue and
     * a list of subscriptions. It will also have a connection to other software bus
     * through the use of message distributor
     */
    public Bus() {
        this.queue = new LinkedBlockingQueue<>();
        this.subscriptions = new CopyOnWriteArrayList<>();
        distributor = new MessageDistributor(1234);
        updateQueue();
    }

    /**
     * Subscribe to a topic
     *
     * @param t the topic you are subscribing to.
     *          topic contains both topic and subtopic
     */
    public void subscribe(Topic t) {
        this.subscriptions.add(t);
    }

    /**
     * Get the most recent message you have from your inbox of a
     * given topic. If you do not have any messages of that topic you will receive null
     *
     * @param t the topic you are get a message for.
     *          topic contains both topic and subtopic
     * @return the most recent message if there is one of a provided topic
     */
    public Message getMessage(Topic t) {
        for (Message m : this.queue) {
            if (m.getTopics().equals(t)) {
                queue.remove(m);
                return m;
            }
        }
        return null;
    }

    /**
     * Publish a message
     *
     * @param m the message that will be published
     */
    public void publish(Message m) {
        distributor.send(m);
    }

    /**
     * A helper method that will read all incoming messages and
     * filter messages that you are subscribed to. If you are subscribed then
     * it will be added to your queue. If not then it will be ignored
     */
    private void updateQueue() {
        new Thread(() -> {
            while (true) {
                Message m = distributor.nextMessage();
                if (subscriptions.contains(m.getTopics())) {
                    queue.add(m);
                }
            }

        }).start();
    }

}
