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

    public EventManager() {
        for (GameEvent event : GameEvent.values()) {
            listeners.put(event, new ArrayList<>());
        }
    }

    public void subscribe(GameEvent event, GameEventListener listener) {
        listeners.get(event).add(listener);
    }

    public void triggerEvent(GameEvent event, String targetId, int amount, PlayState state) {
        for (GameEventListener listener : listeners.get(event)) {
            listener.onGameEvent(event, targetId, amount, state);
        }
    }
}
