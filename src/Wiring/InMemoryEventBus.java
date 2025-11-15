package Wiring;
public class InMemoryEventBus {
    // TODO: ensure thread-safe lists (CopyOnWriteArrayList) and map (ConcurrentHashMap).
    // TODO: call handlers on JavaFX thread if they touch UI: Platform.runLater(...) { handler.accept(e); }
}
