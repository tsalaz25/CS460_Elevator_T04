package SoftwareBus.Bus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class Bus {
    private BlockingQueue<Message> queue;
    private CopyOnWriteArrayList<Topic> subscriptions;
    private MessageDistributor distributor;

    public Bus() {
        this.queue = new LinkedBlockingQueue<>();
        this.subscriptions = new CopyOnWriteArrayList<>();
        distributor = new MessageDistributor(1234);
        updateQueue();
    }

    public void subscribe(Topic t) {
        this.subscriptions.add(t);
    }

    public Message getMessage(Topic t) {
        for(Message m : this.queue) {
            if(m.getTopics().equals(t)) {
                queue.remove(m);
                return m;
            }
        }
        return null;
    }

    public void publish(Message m) {
        distributor.send(m);
    }

    private void updateQueue(){
        new Thread(() -> {
            while(true){
                Message m = distributor.nextMessage();
                if(subscriptions.contains(m.getTopics())) {
                    queue.add(m);
                }
            }

        }).start();
    }

}
