package org.example.logic.event;

import org.example.state.PlayState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centrally manages game events and distributes them to registered listeners.
 * Decouples systems like Questing and Looting from the core update cycle.
 */
public class EventManager {
    private final Map<GameEvent, List<GameEventListener>> listeners = new HashMap<>();

    /**
     * Initializes the manager with empty listener lists for all event types.
     */
    public EventManager() {
        for (GameEvent event : GameEvent.values()) {
            listeners.put(event, new ArrayList<>());
        }
    }

    /**
     * Registers a listener for a specific game event type.
     * @param event The event type to listen for.
     * @param listener The listener instance.
     */
    public void subscribe(GameEvent event, GameEventListener listener) {
        listeners.get(event).add(listener);
    }

    /**
     * Dispatches an event to all registered listeners.
     * @param event The event type.
     * @param targetId ID of the entity or item involved.
     * @param amount Magnitude of the event (e.g. quantity of items).
     * @param state Reference to PlayState.
     */
    public void triggerEvent(GameEvent event, String targetId, int amount, PlayState state) {
        for (GameEventListener listener : listeners.get(event)) {
            listener.onGameEvent(event, targetId, amount, state);
        }
    }
}
