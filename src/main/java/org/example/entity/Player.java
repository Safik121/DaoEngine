package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.level.Level;

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
     * Updates the player's position and handles collisions based on the current level.
     * 
     * @param level The current game level used for collision checks.
     */
    public void update(Level level) {
        double speed = 3.0;
        double dx = 0; // Planned movement on the X axis
        double dy = 0; // Planned movement on the Y axis

        // Determine intended movement direction
        if (Input.isKeyPressed(KeyCode.W)) dy -= speed;
        if (Input.isKeyPressed(KeyCode.S)) dy += speed;
        if (Input.isKeyPressed(KeyCode.A)) dx -= speed;
        if (Input.isKeyPressed(KeyCode.D)) dx += speed;

        // Apply movement on the X axis if no wall is present
        if (!isSolid(x + dx, y, level)) {
            x += dx;
        }

        // Apply movement on the Y axis if no wall is present
        if (!isSolid(x, y + dy, level)) {
            y += dy;
        }
    }

    /**
     * Checks if a specific position is occupied by a solid tile or is out of bounds.
     * Tests all four corners of the player's bounding box against the level grid.
     * 
     * @param targetX The target X coordinate to check.
     * @param targetY The target Y coordinate to check.
     * @param level The current level data.
     * @return true if the position is solid/blocked, false if the path is clear.
     */
    private boolean isSolid(double targetX, double targetY, Level level) {
        // Calculate the columns and rows the player would occupy
        // Subtract a tiny amount to avoid getting stuck on walls we're just touching
        int leftCol = (int) (targetX / level.tileSize);
        int rightCol = (int) ((targetX + size - 0.1) / level.tileSize);
        int topRow = (int) (targetY / level.tileSize);
        int bottomRow = (int) ((targetY + size - 0.1) / level.tileSize);

        // 1. Boundary check: ensure the player stays within the map limits
        if (leftCol < 0 || rightCol >= level.width || topRow < 0 || bottomRow >= level.height) {
            return true;
        }

        // 2. Grid check: collision if any of the corners hit a solid tile (value 1)
        if (level.data.get(topRow).get(leftCol) == 1 ||
                level.data.get(topRow).get(rightCol) == 1 ||
                level.data.get(bottomRow).get(leftCol) == 1 ||
                level.data.get(bottomRow).get(rightCol) == 1) {
            return true;
        }

        return false; // The path is clear!
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
        gc.fillRect(x, y, size, size);
    }
}