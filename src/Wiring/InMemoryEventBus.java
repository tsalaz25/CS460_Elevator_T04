package Wiring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple, thread-safe in-process event bus.
 *
 * - Uses ConcurrentHashMap + CopyOnWriteArrayList so publish/subscribe
 *   are safe from multiple threads.
 * - Dispatch is synchronous: publish(...) calls each handler in the
 *   current thread. If a handler needs to touch JavaFX UI, it should
 *   wrap itself in Platform.runLater(...) when it subscribes.
 */
public class InMemoryEventBus implements EventBus {

    // Map from Topic -> list of subscribers
    private final Map<Topics, CopyOnWriteArrayList<Consumer<Event>>> subscribers =
            new ConcurrentHashMap<>();

    @Override
    public void publish(Topics topic, Object payload) {
        Event event = new Event(topic, payload);

        CopyOnWriteArrayList<Consumer<Event>> handlers = subscribers.get(topic);
        if (handlers == null) {
            return; // no subscribers for this topic yet
        }

        // synchronous dispatch to a thread-safe snapshot of handlers
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
