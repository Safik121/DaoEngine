package org.example.logic.event;

import org.example.state.PlayState;

/**
 * Interface for systems that want to listen for game events.
 */
public interface GameEventListener {
    /**
     * Called when a game event is triggered.
     * @param event The type of event.
     * @param targetId The ID of the entity/item involved.
     * @param amount Quality/Quantity of the event (e.g., amount of enemies killed or items picked up).
     * @param state Reference to the current game state.
     */
    void onGameEvent(GameEvent event, String targetId, int amount, PlayState state);
}
