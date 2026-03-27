package org.example;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.HashSet;
import java.util.Set;

/**
 * A static utility class for tracking keyboard and mouse input.
 * Maintains a set of currently pressed keys and mouse state to be queried by game entities.
 */
public class Input {
    // --- KEYBOARD ---
    /** A set containing all currently active (pressed) key codes. */
    private static Set<KeyCode> activeKeys = new HashSet<>();

    /**
     * Registers a key as being currently pressed.
     * @param code The key code to register.
     */
    public static void addKey(KeyCode code) {
        activeKeys.add(code);
    }

    /**
     * Registers a key as being currently released.
     * @param code The key code to remove.
     */
    public static void removeKey(KeyCode code) {
        activeKeys.remove(code);
    }

    /**
     * Checks if a specific key is currently held down.
     * @param code The key code to check.
     * @return true if the key is pressed.
     */
    public static boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    // --- MOUSE ---
    /** Current X coordinate of the mouse in pixels. */
    private static double mouseX = 0;
    /** Current Y coordinate of the mouse in pixels. */
    private static double mouseY = 0;
    /** Whether the Left Mouse Button is currently pressed. */
    private static boolean lmbPressed = false;
    /** Whether the Right Mouse Button is currently pressed. */
    private static boolean rmbPressed = false;

    /**
     * Updates the current mouse cursor position.
     * @param x The mouse X coordinate.
     * @param y The mouse Y coordinate.
     */
    public static void setMousePosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    /**
     * Updates the state of a specific mouse button.
     * @param button The mouse button (PRIMARY/SECONDARY).
     * @param pressed Whether it is currently pressed.
     */
    public static void setMouseButton(MouseButton button, boolean pressed) {
        if (button == MouseButton.PRIMARY) {
            lmbPressed = pressed;
        } else if (button == MouseButton.SECONDARY) {
            rmbPressed = pressed;
        }
    }

    /** @return Mouse X coordinate. */
    public static double getMouseX() { return mouseX; }
    /** @return Mouse Y coordinate. */
    public static double getMouseY() { return mouseY; }
    /** @return true if the Left Mouse Button is pressed. */
    public static boolean isLmbPressed() { return lmbPressed; }
    /** @return true if the Right Mouse Button is pressed. */
    public static boolean isRmbPressed() { return rmbPressed; }
}