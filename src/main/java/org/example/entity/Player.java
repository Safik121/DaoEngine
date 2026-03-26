package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.level.Level;

/**
 * Represents the player entity in the game.
 * Handles player position, size, movement logic, statistics, and rendering.
 */
public class Player {
    /** The X coordinate of the player in pixels. */
    private double x;
    /** The Y coordinate of the player in pixels. */
    private double y;
    /** The size of the player entity. */
    private double size;

    /** Current Health Points (HP). */
    private double hp;
    /** Maximum Health Points. */
    private double maxHp = 100.0;
    /** Current Spiritual Energy (Qi). */
    private double qi;
    /** Maximum Qi capacity. */
    private double maxQi = 50.0;
    /** Whether the player is currently meditating. */
    private boolean isMeditating = false;

    /**
     * Constructs a new Player at the specified starting position.
     * 
     * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.size = 12;
        this.hp = maxHp;
        this.qi = maxQi;
    }

    /**
     * Updates the player's position and handles collisions based on the current level.
     * Also processes meditation logic.
     * 
     * @param level The current game level used for collision checks.
     */
    public void update(Level level) {
        // --- 1. Meditation Logic ---
        isMeditating = Input.isKeyPressed(KeyCode.SPACE);
        
        if (isMeditating) {
            // Regenerate stats during meditation (Section 3.2 of the vision doc)
            if (hp < maxHp) hp += 0.1; // Slow heal
            if (qi < maxQi) qi += 0.2; // Faster Qi regen
            return; // Cannot move while meditating
        }

        // --- 2. Movement Logic ---
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
     * Tests all four corners of the player's bounding box.
     * 
     * @param targetX The target X coordinate to check.
     * @param targetY The target Y coordinate to check.
     * @param level The current level data.
     * @return true if the position is solid/blocked, false otherwise.
     */
    private boolean isSolid(double targetX, double targetY, Level level) {
        int leftCol = (int) (targetX / level.tileSize);
        int rightCol = (int) ((targetX + size - 0.1) / level.tileSize);
        int topRow = (int) (targetY / level.tileSize);
        int bottomRow = (int) ((targetY + size - 0.1) / level.tileSize);

        // 1. Boundary check
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

        return false;
    }

    /**
     * Renders the player entity using the provided GraphicsContext.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    public void render(GraphicsContext gc) {
        // Change color and add aura when meditating
        if (isMeditating) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(x - 5, y - 5, size + 10, size + 10);
            gc.setGlobalAlpha(1.0);
        }

        gc.setFill(Color.BLUE);
        gc.fillRect(x, y, size, size);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
    public double getQi() { return qi; }
    public double getMaxQi() { return maxQi; }
    public boolean isMeditating() { return isMeditating; }

    /**
     * Applies damage to the player. HP will not drop below 0.
     * 
     * @param amount The amount of damage to take.
     */
    public void takeDamage(double amount) {
        this.hp -= amount;
        if (this.hp < 0) this.hp = 0;
    }
}