package org.example.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that maintains global flags and persistent world state.
 * Allows decisions in one level to affect characters or events in another.
 */
public class WorldState {
    private static WorldState instance;
    private Map<String, Boolean> flags;
    private Map<String, Integer> counters;

    private WorldState() {
        flags = new HashMap<>();
        counters = new HashMap<>();
    }

    public static WorldState getInstance() {
        if (instance == null) {
            instance = new WorldState();
        }
        return instance;
    }

    /**
     * Sets a global flag by name.
     */
    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    /**
     * Checks if a flag is set. Returns false if the flag doesn't exist.
     */
    public boolean getFlag(String key) {
        return flags.getOrDefault(key, false);
    }

    /**
     * Increments a global counter. Useful for tracking "Total Kills" or "Villagers Saved".
     */
    public void incrementCounter(String key) {
        counters.put(key, counters.getOrDefault(key, 0) + 1);
    }

    public int getCounter(String key) {
        return counters.getOrDefault(key, 0);
    }

    // --- Persistency Helpers ---

    public Map<String, Boolean> getFlags() { return new HashMap<>(flags); }
    public void setFlags(Map<String, Boolean> flags) { this.flags = new HashMap<>(flags); }

    public Map<String, Integer> getCounters() { return new HashMap<>(counters); }
    public void setCounters(Map<String, Integer> counters) { this.counters = new HashMap<>(counters); }

    /**
     * Clears all state. Used when starting a new game.
     */
    public void reset() {
        flags.clear();
        counters.clear();
    }
}
