package org.example.logic.event;

/**
 * Types of events that can occur in the game.
 */
public enum GameEvent {
    /** Triggered when a living entity (Player or Enemy) dies. */
    ENTITY_DEATH,
    /** Triggered when an item is added to the player's inventory from the world. */
    ITEM_PICKUP,
    /** Triggered when a quest objective is met and the quest is finalized. */
    QUEST_COMPLETE,
    /** Triggered when the player successfully reaches a new cultivation rank. */
    CULTIVATION_BREAKTHROUGH,
    /** Triggered when a new map is loaded and the player enters the realm. */
    LEVEL_START,
    /** Triggered when the current level's victory conditions are achieved. */
    LEVEL_WIN
}
