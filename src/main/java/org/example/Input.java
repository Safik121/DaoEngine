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

    public static void addKey(KeyCode code) {
        activeKeys.add(code);
    }

    public static void removeKey(KeyCode code) {
        activeKeys.remove(code);
    }

    public static boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    // --- MOUSE ---
    private static double mouseX = 0;
    private static double mouseY = 0;
    private static boolean lmbPressed = false;
    private static boolean rmbPressed = false;

    public static void setMousePosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    public static void setMouseButton(MouseButton button, boolean pressed) {
        if (button == MouseButton.PRIMARY) {
            lmbPressed = pressed;
        } else if (button == MouseButton.SECONDARY) {
            rmbPressed = pressed;
        }
    }

    public static double getMouseX() { return mouseX; }
    public static double getMouseY() { return mouseY; }
    public static boolean isLmbPressed() { return lmbPressed; }
    public static boolean isRmbPressed() { return rmbPressed; }
}