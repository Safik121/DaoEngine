package org.example;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

/**
 * A static utility class for tracking keyboard input.
 * Maintains a set of currently pressed keys to be queried by game entities.
 */
public class Input {
    /** A set containing all currently active (pressed) key codes. */
    private static Set<KeyCode> activeKeys = new HashSet<>();

    /**
     * Adds a key to the set of active keys.
     * 
     * @param code The KeyCode of the pressed key.
     */
    public static void addKey(KeyCode code) {
        activeKeys.add(code);
    }

    /**
     * Removes a key from the set of active keys.
     * 
     * @param code The KeyCode of the released key.
     */
    public static void removeKey(KeyCode code) {
        activeKeys.remove(code);
    }

    /**
     * Checks if a specific key is currently being pressed.
     * 
     * @param code The KeyCode to check.
     * @return true if the key is pressed, false otherwise.
     */
    public static boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }
}