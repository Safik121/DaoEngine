package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.level.GameMap;
import org.example.level.Pathfinder;
import org.example.AssetRegistry;
import java.util.List;
import java.util.Random;

/**
 * Represents an enemy entity (monster) in the game.
 * Enemies can be regular or part of a "Tribulation" (Heavenly Punishment).
 * They use the A* algorithm for navigation towards the player.
 */
public class Enemy extends LivingEntity {

    /** Whether the enemy is part of a Tribulation (stronger and more aggressive). */
    private boolean isTribulation;

    /** Damage dealt to the player on contact. */
    private double damage;

    /** Name of the enemy type. */
    private String name;
    /** ID of the enemy type for registry lookups. */
    private String id;
    /** The scaling factor applied to stats at spawn. */
    private double scaling = 1.0;
    /** Current hex color code for rendering. */
    private String colorHex = "#0000FF";
    /** Timer (seconds) for cycling through animation frames. */
    private double animationTimer = 0;

    /** The range in pixels within which a regular enemy detects the player. */
    private double detectionRange = 150.0;

    /** Currently calculated path to the target (list of tile coordinates). */
    private List<int[]> currentPath;
    /** Timer for periodic path recalculation. */
    private int pathRecalculateTimer = 0;

    /** Cooldown timer between attacks on the player. */
    private double attackCooldown = 0;
    
    /** Range at which the enemy prefers to stay and shoot (if ranged). */
    private double attackRange = 30.0;
    /** If true, the enemy fires projectiles instead of melee. */
    private boolean isRanged = false;
    /** Config for projectiles if ranged. */
    private org.example.item.WeaponConfig projectileConfig;

    /**
     * Constructs a new Enemy at the specified position.
     * Use {@link #setStats} to initialize stats from a registry.
     * 
     * @param startX Initial X pixel coordinate.
     * @param startY Initial Y pixel coordinate.
     * @param isTribulation Whether this is an elite Tribulation enemy.
     */
    public Enemy(double startX, double startY, boolean isTribulation) {
        super(startX, startY, 24, 100, 1.0); // Defaults, instantly overridden by setStats
        this.isTribulation = isTribulation;
    }

    /**
     * Initializes the enemy statistics and appearance from a configuration.
     * 
     * @param id Unique identifier.
     * @param name Name of the monster.
     * @param hp Initial and max health.
     * @param damage Attack damage.
     * @param speed Movement speed.
     * @param size Hitbox size.
     * @param colorHex Rendering color.
     * @param scaling Stats multiplier.
     */
    public void setStats(String id, String name, double hp, double damage, double speed, double size, String colorHex, double scaling) {
        this.id = id;
        this.name = name;
        super.setMaxHp(hp);
        super.setHp(hp);
        this.damage = damage;
        super.setSpeed(speed);
        super.setSize(size);
        this.colorHex = colorHex;
        this.scaling = scaling;
    }

    /**
     * Initializes ranged attack parameters.
     */
    public void setRanged(double range, org.example.item.WeaponConfig config) {
        this.isRanged = true;
        this.attackRange = range;
        this.projectileConfig = config;
    }

    /**
     * Scales the enemy stats based on a factor.
     */
    public void scaleStats(double factor) {
        this.setMaxHp(this.getMaxHp() * factor);
        this.setHp(this.getMaxHp());
        this.damage *= factor;
        // Optionally scale size slightly
        this.size *= (1.0 + (factor - 1.0) * 0.1);
    }

    /**
     * Updates the enemy's state: movement, pathfinding, and attacking.
     * 
     * @param gameMap Game map for collision checks and pathfinding.
     * @param player Reference to the player for tracking and attacking.
     * @param allEnemies List of all enemies for separation behavior.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(GameMap gameMap, Player player, List<Enemy> allEnemies, double deltaTime, List<Projectile> projectiles) {
        animationTimer += deltaTime;
        if (animationTimer > 10.0) animationTimer -= 10.0;
        
        // Update status effects
        statusEffectManager.update(deltaTime);
        
        // 1. Calculate distance to player
        double distX = player.getX() + 6 - (this.x + size / 2);
        double distY = player.getY() + 6 - (this.y + size / 2);
        double distance = Math.sqrt(distX * distX + distY * distY);

        double moveDirX = 0;
        double moveDirY = 0;

        // 2. Decision: Pathfinding vs Shooting
        if (isTribulation || distance <= detectionRange) {
            boolean hasLoS = gameMap.hasLineOfSight(x + size / 2, y + size / 2, player.getX() + 6, player.getY() + 6);

            // Ranged behavior: stay at range if we have LoS
            if (isRanged && hasLoS && distance < attackRange) {
                // Stay put or move slightly away to maintain distance
                if (distance < attackRange * 0.7) {
                    moveDirX = -distX / distance;
                    moveDirY = -distY / distance;
                }
            } else if (hasLoS) {
                // Move directly towards player
                moveDirX = distX / distance;
                moveDirY = distY / distance;
                currentPath = null;
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
                    pathRecalculateTimer = 20 + new Random().nextInt(20);
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

                    if (diffDist < 5.0) {
                        currentPath.remove(0);
                    }
                }
            }

            // 3. Separation behavior
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

            double mag = Math.sqrt(moveDirX * moveDirX + moveDirY * moveDirY);
            if (mag > 1.0) {
                moveDirX /= mag;
                moveDirY /= mag;
            }

            // --- 4. Attack Logic ---
            if (attackCooldown > 0) {
                attackCooldown -= (deltaTime * 60.0);
            } else {
                if (isRanged && hasLoS && distance <= attackRange) {
                    // Fire projectile
                    double angle = Math.atan2(distY, distX);
                    projectiles.add(new Projectile(x + size / 2, y + size / 2, angle, projectileConfig, this, false));
                    attackCooldown = 90.0; // Ranged takes longer
                } else if (distance < size + 5) {
                    player.takeDamage(damage);
                    attackCooldown = 60.0;
                }
            }
        }

        // --- 5. Apply movement with collision checks ---
        double dtFactor = deltaTime * 60.0;
        double nextX = x + moveDirX * stats.getSpeed() * dtFactor;
        double nextY = y + moveDirY * stats.getSpeed() * dtFactor;

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
     * @param cameraX Camera X offset.
     * @param cameraY Camera Y offset.
     */
    @Override
    public void render(GraphicsContext gc, double cameraX, double cameraY) {
        // --- 1. Draw Sprite ---
        int frames = AssetRegistry.getFrameCount(id);
        int frameIndex = (int) (animationTimer / 0.1) % frames;

        javafx.scene.image.Image sprite = AssetRegistry.getSprite(id, frameIndex);
        if (sprite != null) {
            gc.drawImage(sprite, x - cameraX, y - cameraY, size, size);
        } else {
            // Draw body using configured hex color
            gc.setFill(Color.web(colorHex));
            gc.fillRect(x - cameraX, y - cameraY, size, size);
        }

        // --- HP Bar ---
        double barW = size;
        double barH = 4;
        double barX = x - cameraX;
        double barY = y - cameraY - 8;

        // Background (Red)
        gc.setFill(Color.RED);
        gc.fillRect(barX, barY, barW, barH);

        // Foreground (Green)
        double healthPercent = (double) stats.getHp() / stats.getMaxHp();
        gc.setFill(Color.LIME);
        gc.fillRect(barX, barY, barW * healthPercent, barH);

        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barW, barH);
    }

    /** @return Unique identifier of the enemy type. */
    public String getId() { return id; }
    /** @return The scaling factor applied at spawn. */
    public double getScaling() { return scaling; }
    /** 
     * Checks if this enemy was spawned as part of a Tribulation phase. 
     * @return true if it is a Tribulation elite enemy. 
     */
    public boolean isTribulation() { return isTribulation; }
}