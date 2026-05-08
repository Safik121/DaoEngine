package org.example.logic;

import org.example.state.PlayState;

/**
 * Interface for everything in the game world that a player can interact with.
 * Unifies NPCs, Items on ground, and special objects like Gates.
 */
public interface Interactable {
    /**
     * Called when the player interacts with this object (e.g., presses 'E').
     */
    void onInteract(PlayState state);

    /** @return The prompt to show in the UI (e.g., "[E] Talk"). */
    String getPrompt();

    /** @return Maximum distance for interaction. */
    double getInteractionRange();

    /** @return World X coordinate. */
    double getX();
    /** @return World Y coordinate. */
    double getY();
}
