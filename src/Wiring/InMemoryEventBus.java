package Wiring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventBus {

    private final Map<Topics, CopyOnWriteArrayList<Consumer<Event>>> subscribers =
            new ConcurrentHashMap<>();

    @Override
    public void publish(Topics topic, Object payload) {
        Event event = new Event(topic, payload);

        CopyOnWriteArrayList<Consumer<Event>> handlers = subscribers.get(topic);
        if (handlers == null) {
            return;
        }

        for (Consumer<Event> handler : handlers) {
            handler.accept(event);
        }
    }

    @Override
    public void subscribe(Topics topic, Consumer<Event> handler) {
        subscribers
                .computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
}
