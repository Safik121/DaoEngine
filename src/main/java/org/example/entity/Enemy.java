package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.level.GameMap;
import org.example.level.Level;
import org.example.level.Pathfinder;

import java.util.List;

/**
 * Represents an enemy entity (monster) in the game.
 * Enemies can be regular or part of a "Tribulation" (Heavenly Punishment).
 * They use the A* algorithm for navigation towards the player.
 */
public class Enemy {
    /** The X coordinate of the enemy in pixels. */
    private double x;
    /** The Y coordinate of the enemy in pixels. */
    private double y;
    /** The size of the enemy entity. */
    private double size = 24;

    /** Whether the enemy is part of a Tribulation (stronger and more aggressive). */
    private boolean isTribulation;
    /** Movement speed of the enemy. */
    private double speed;
    /** Health points of the enemy. */
    private int hp;
    /** Damage dealt to the player on contact. */
    private int damage;

    /** The range in pixels within which a regular enemy detects the player. */
    private double detectionRange = 150.0;

    /** Currently calculated path to the target (list of tile coordinates). */
    private List<int[]> currentPath;
    /** Timer for periodic path recalculation. */
    private int pathRecalculateTimer = 0;

    /** Cooldown timer between attacks on the player. */
    private int attackCooldown = 0;

    /**
     * Constructs a new Enemy at the specified position.
     * 
     * @param startX Initial X pixel coordinate.
     * @param startY Initial Y pixel coordinate.
     * @param isTribulation Whether this is an elite Tribulation enemy.
     */
    public Enemy(double startX, double startY, boolean isTribulation) {
        this.x = startX;
        this.y = startY;
        this.isTribulation = isTribulation;

        if (isTribulation) {
            this.hp = 100;
            this.damage = 25;
            this.speed = 2.0;
        } else {
            this.hp = 40;
            this.damage = 10;
            this.speed = 1.0;
        }
    }

    /**
     * Updates the enemy's state: movement, pathfinding, and attacking.
     * 
     * @param gameMap Game map for collision checks and pathfinding.
     * @param player Reference to the player for tracking and attacking.
     */
    public void update(GameMap gameMap, Player player) {
        double dx = 0;
        double dy = 0;

        // 1. Calculate distance to player
        double distX = player.getX() - this.x;
        double distY = player.getY() - this.y;
        double distance = Math.sqrt(distX * distX + distY * distY);

        // 2. Movement decision (AI)
        if (isTribulation || distance <= detectionRange) {
            
            // Pathfinding logic
            pathRecalculateTimer--;
            if (pathRecalculateTimer <= 0) {
                int tileSize = gameMap.getTileSize();
                int startX = (int) ((x + size / 2) / tileSize);
                int startY = (int) ((y + size / 2) / tileSize);
                int targetX = (int) ((player.getX() + 6) / tileSize);
                int targetY = (int) ((player.getY() + 6) / tileSize);

                currentPath = Pathfinder.findPath(gameMap, startX, startY, targetX, targetY);
                pathRecalculateTimer = 30; // Recalculate twice a second (at 60fps)
            }

            if (currentPath != null && !currentPath.isEmpty()) {
                // Move according to the calculated path
                int[] nextTile = currentPath.get(0);
                int tileSize = gameMap.getTileSize();
                double targetPX = nextTile[0] * tileSize + (tileSize - size) / 2.0;
                double targetPY = nextTile[1] * tileSize + (tileSize - size) / 2.0;

                double diffX = targetPX - x;
                double diffY = targetPY - y;

                if (Math.abs(diffX) < speed) x = targetPX;
                else if (diffX > 0) dx += speed;
                else if (diffX < 0) dx -= speed;

                if (Math.abs(diffY) < speed) y = targetPY;
                else if (diffY > 0) dy += speed;
                else if (diffY < 0) dy -= speed;

                // Remove tile from path if reached
                if (Math.abs(x - targetPX) < 1.0 && Math.abs(y - targetPY) < 1.0) {
                    currentPath.remove(0);
                }
            } else {
                // Fallback: direct movement (if no path found or very close)
                if (distX > 0) dx += speed;
                if (distX < 0) dx -= speed;
                if (distY > 0) dy += speed;
                if (distY < 0) dy -= speed;
            }

            // --- 3. Attack Logic ---
            if (attackCooldown > 0) {
                attackCooldown--;
            } else if (distance < size + 5) { // Within attack range
                player.takeDamage(damage);
                attackCooldown = 60; // Attack once per second
            }
        }

        // --- 4. Apply movement with collision checks ---
        if (dx != 0 && !isSolid(x + dx, y, gameMap)) {
            x += dx;
        }
        if (dy != 0 && !isSolid(x, y + dy, gameMap)) {
            y += dy;
        }
    }

    /**
     * Checks for collisions with the environment.
     */
    private boolean isSolid(double targetX, double targetY, GameMap gameMap) {
        int tileSize = gameMap.getTileSize();
        int leftCol = (int) (targetX / tileSize);
        int rightCol = (int) ((targetX + size - 0.1) / tileSize);
        int topRow = (int) (targetY / tileSize);
        int bottomRow = (int) ((targetY + size - 0.1) / tileSize);

        return gameMap.isSolid(leftCol, topRow) || 
               gameMap.isSolid(rightCol, topRow) || 
               gameMap.isSolid(leftCol, bottomRow) || 
               gameMap.isSolid(rightCol, bottomRow);
    }

    /**
     * Renders the enemy. Color depends on whether it's a Tribulation enemy.
     * 
     * @param gc GraphicsContext for drawing.
     */
    public void render(GraphicsContext gc) {
        if (isTribulation) {
            gc.setFill(Color.CRIMSON); // Crimson for Tribulation
        } else {
            gc.setFill(Color.PURPLE); // Purple for regular enemies
        }
        gc.fillRect(x, y, size, size);
    }
}