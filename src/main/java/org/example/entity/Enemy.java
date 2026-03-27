package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.level.GameMap;
import org.example.level.Pathfinder;

import java.util.List;
import java.util.Random;

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
     * @param allEnemies List of all enemies for separation behavior.
     */
    public void update(GameMap gameMap, Player player, List<Enemy> allEnemies) {
        // 1. Calculate distance to player
        double distX = player.getX() + 6 - (this.x + size / 2);
        double distY = player.getY() + 6 - (this.y + size / 2);
        double distance = Math.sqrt(distX * distX + distY * distY);

        double moveDirX = 0;
        double moveDirY = 0;

        // 2. Decision: Direct Line-of-Sight vs Pathfinding
        if (isTribulation || distance <= detectionRange) {
            boolean hasLoS = gameMap.hasLineOfSight(x + size / 2, y + size / 2, player.getX() + 6, player.getY() + 6);

            if (hasLoS) {
                // Move directly towards player
                moveDirX = distX / distance;
                moveDirY = distY / distance;
                currentPath = null; // Clear path if we have direct view
            } else {
                // Use Pathfinding
                pathRecalculateTimer--;
                if (pathRecalculateTimer <= 0) {
                    int tileSize = gameMap.getTileSize();
                    int startX = (int) ((x + size / 2) / tileSize);
                    int startY = (int) ((y + size / 2) / tileSize);
                    int targetX = (int) ((player.getX() + 6) / tileSize);
                    int targetY = (int) ((player.getY() + 6) / tileSize);

                    currentPath = Pathfinder.findPath(gameMap, startX, startY, targetX, targetY);
                    pathRecalculateTimer = 20 + new Random().nextInt(20); // Jitter recalculation
                }

                if (currentPath != null && !currentPath.isEmpty()) {
                    int[] nextTile = currentPath.get(0);
                    int tileSize = gameMap.getTileSize();
                    double targetPX = nextTile[0] * tileSize + tileSize / 2.0;
                    double targetPY = nextTile[1] * tileSize + tileSize / 2.0;

                    double diffX = targetPX - (x + size / 2);
                    double diffY = targetPY - (y + size / 2);
                    double diffDist = Math.sqrt(diffX * diffX + diffY * diffY);

                    if (diffDist > 1.0) {
                        moveDirX = diffX / diffDist;
                        moveDirY = diffY / diffDist;
                    }

                    // Remove tile from path if reached or close enough
                    if (diffDist < 5.0) {
                        currentPath.remove(0);
                    }
                }
            }

            // 3. Separation behavior (don't clump together)
            double sepX = 0;
            double sepY = 0;
            for (Enemy other : allEnemies) {
                if (other == this) continue;
                double dx = (this.x + size / 2) - (other.x + other.size / 2);
                double dy = (this.y + size / 2) - (other.y + other.size / 2);
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d < size && d > 0) {
                    sepX += dx / d;
                    sepY += dy / d;
                }
            }
            moveDirX += sepX * 0.5;
            moveDirY += sepY * 0.5;

            // Normalize final movement vector if needed
            double mag = Math.sqrt(moveDirX * moveDirX + moveDirY * moveDirY);
            if (mag > 1.0) {
                moveDirX /= mag;
                moveDirY /= mag;
            }

            // --- 4. Attack Logic ---
            if (attackCooldown > 0) {
                attackCooldown--;
            } else if (distance < size + 5) {
                player.takeDamage(damage);
                attackCooldown = 60;
            }
        }

        // --- 5. Apply movement with collision checks ---
        double nextX = x + moveDirX * speed;
        double nextY = y + moveDirY * speed;

        if (moveDirX != 0 && !isSolid(nextX, y, gameMap)) {
            x = nextX;
        }
        if (moveDirY != 0 && !isSolid(x, nextY, gameMap)) {
            y = nextY;
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
     * @param camX Camera X offset.
     * @param camY Camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        if (isTribulation) {
            gc.setFill(Color.CRIMSON);
        } else {
            gc.setFill(Color.PURPLE);
        }
        gc.fillRect(x - camX, y - camY, size, size);
    }

    public double getX() { return x; }
    public double getY() { return y; }
}