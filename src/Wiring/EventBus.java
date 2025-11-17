package Wiring;

import java.util.function.Consumer;

/**
 * Minimal pub/sub facade used by the rest of the elevator system.
 *
 * Implementations (e.g., InMemoryEventBus, SoftwareBusAdapter) provide
 * the actual storage and dispatch behavior.
 */
public interface EventBus {

    /**
     * Canonical event object passed to subscribers.
     * topic  - which logical channel (see Wiring.Topics)
     * payload - arbitrary data (Integer floor, String direction, etc.)
     */
    record Event(Topics topic, Object payload) { }

    /**
     * Publish an event with the given topic and payload.
     */
    void publish(Topics topic, Object payload);

    /**
     * Subscribe a handler that will be called for every event of the given topic.
     */
    void subscribe(Topics topic, Consumer<Event> handler);
}
