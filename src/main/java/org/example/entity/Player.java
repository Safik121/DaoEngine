package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;

/**
 * Represents the player entity in the game.
 * Handles player position, size, movement logic, and rendering.
 */
public class Player {
    /** The X coordinate of the player in pixels. */
    private double x;
    /** The Y coordinate of the player in pixels. */
    private double y;
    /** The size of the player entity. */
    private double size;

    /**
     * Constructs a new Player at the specified starting position.
     * 
     * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        // Make the player slightly smaller than the tile size (32) to fit well
        this.size = 24;
    }

    /**
     * Updates the player's internal logic, such as movement and collisions.
     */
    public void update() {
        double speed = 3.0; // Movement speed (pixels per frame)

        if (Input.isKeyPressed(KeyCode.W)) {
            y -= speed; // Move Up (Y decreases)
        }
        if (Input.isKeyPressed(KeyCode.S)) {
            y += speed; // Move Down (Y increases)
        }
        if (Input.isKeyPressed(KeyCode.A)) {
            x -= speed; // Move Left (X decreases)
        }
        if (Input.isKeyPressed(KeyCode.D)) {
            x += speed; // Move Right (X increases)
        }
    }

    /**
     * Renders the player entity using the provided GraphicsContext.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    public void render(GraphicsContext gc) {
        // The player is currently rendered as a blue square
        gc.setFill(Color.BLUE);

        // Draw the player at their current position with a small offset (+4)
        // to center them within a 32x32 tile.
        gc.fillRect(x + 4, y + 4, size, size);
    }
}