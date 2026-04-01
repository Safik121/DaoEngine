package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.item.WeaponConfig;
import org.example.level.GameMap;

/**
 * Represents an active projectile fired from a weapon.
 * This class handles the physical movement, rendering, and collision logic 
 * for various projectile types (Fireballs, Flying Swords, Beams).
 */
public class Projectile {
    /** Current X coordinate in world pixels. */
    protected double x;
    /** Current Y coordinate in world pixels. */
    protected double y;
    /** Horizontal velocity component. */
    protected double vx;
    /** Vertical velocity component. */
    protected double vy;
    /** Rotation or aiming angle in radians. */
    protected double angle;
    /** Fixed length for segment-based projectiles like Beams. */
    protected double length;
    /** Hitbox diameter or beam width in pixels. */
    protected double size;
    /** Damage power to apply to targets on impact. */
    protected double damage;
    /** Remaining time (in seconds) before the projectile expires. */
    protected double lifeSpan;
    /** The specific behavior type of this projectile. */
    protected WeaponConfig.ProjectileType type;
    /** Whether the projectile is currently active and should be updated/rendered. */
    protected boolean active = true;
    /** The player instance that fired this projectile, used for following in AOE. */
    protected Player owner;

    /**
     * Constructs a new Projectile based on a weapon configuration.
     * 
     * @param x      The starting X coordinate.
     * @param y      The starting Y coordinate.
     * @param angle  The direction of travel or orientation in radians.
     * @param config The weapon configuration defining speed, damage, and type.
     * @param owner  The player entity (used for following in AOE_ZONE).
     */
    public Projectile(double x, double y, double angle, WeaponConfig config, Player owner) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.owner = owner;
        this.size = config.size;
        this.damage = config.damage;
        this.lifeSpan = config.lifeSpan;
        this.type = config.projectileType;
        this.length = config.length;

        this.vx = Math.cos(angle) * config.speed;
        this.vy = Math.sin(angle) * config.speed;
    }

    /**
     * Updates the projectile's state, including movement and expiration.
     * Standard projectiles move by velocity, while Beams remain fixed in duration.
     * 
     * @param gameMap Used to check for collisions with solid terrain.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(GameMap gameMap, double deltaTime) {
        double dtFactor = deltaTime * 60.0;
        
        if (type == WeaponConfig.ProjectileType.AOE_ZONE && owner != null) {
            // Follow player and keep it centered
            this.x = owner.getX() + 6;
            this.y = owner.getY() + 6;
        } else if (type != WeaponConfig.ProjectileType.BEAM) {
            x += vx * dtFactor;
            y += vy * dtFactor;
        }
        
        lifeSpan -= deltaTime;
        if (lifeSpan <= 0) active = false;

        // Wall collision (beams and AOE zones ignore walls)
        if (active && type != WeaponConfig.ProjectileType.BEAM && type != WeaponConfig.ProjectileType.AOE_ZONE &&
            gameMap.isSolid((int)(x / gameMap.getTileSize()), (int)(y / gameMap.getTileSize()))) {
            active = false;
        }
    }

    /**
     * Renders the projectile to the screen using the provided GraphicsContext.
     * Handles specific rendering logic for FIREBALL, FLYING_SWORD, and BEAM types.
     * 
     * @param gc The JavaFX GraphicsContext for drawing.
     * @param camX The current camera X offset.
     * @param camY The current camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        if (!active) return;
        
        switch (type) {
            case FIREBALL:
                gc.setFill(Color.ORANGERED);
                gc.fillOval(x - camX - size/2, y - camY - size/2, size, size);
                break;
            case FLYING_SWORD:
                gc.setFill(Color.SILVER);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                // Draw a simple "sword" line pointing in movement direction
                double endX = x - vx * 2;
                double endY = y - vy * 2;
                gc.strokeLine(x - camX, y - camY, endX - camX, endY - camY);
                break;
            case BEAM:
                gc.setStroke(Color.CYAN);
                gc.setLineWidth(size);
                gc.setGlobalAlpha(Math.min(1.0, lifeSpan * 5)); // Gradual fade-out
                double bx2 = x + Math.cos(angle) * length;
                double by2 = y + Math.sin(angle) * length;
                gc.strokeLine(x - camX, y - camY, bx2 - camX, by2 - camY);
                gc.setGlobalAlpha(1.0);
                break;
            case AOE_ZONE:
                gc.setGlobalAlpha(0.3);
                gc.setFill(Color.MEDIUMPURPLE);
                gc.setStroke(Color.PURPLE);
                gc.setLineWidth(3);
                gc.fillOval(x - camX - size / 2, y - camY - size / 2, size, size);
                gc.strokeOval(x - camX - size / 2, y - camY - size / 2, size, size);
                gc.setGlobalAlpha(1.0);
                break;
        }
    }

    /**
     * Performs collision detection between this projectile and a given Enemy.
     * Uses point-hitbox checks for standard projectiles and line-segment-to-circle 
     * checks for Beams.
     * 
     * @param enemy The enemy entity to check against.
     * @return true if the projectile overlaps with the enemy's hitbox.
     */
    public boolean checkCollision(Enemy enemy) {
        double ex = enemy.getX() + enemy.getSize() / 2;
        double ey = enemy.getY() + enemy.getSize() / 2;
        double eRadius = enemy.getSize() / 2;

        if (type == WeaponConfig.ProjectileType.BEAM) {
            double x2 = x + Math.cos(angle) * length;
            double y2 = y + Math.sin(angle) * length;
            return distToSegment(ex, ey, x, y, x2, y2) < eRadius + size/2;
        } else if (type == WeaponConfig.ProjectileType.AOE_ZONE) {
            // Circle-to-circle collision
            double distSq = (ex - x) * (ex - x) + (ey - y) * (ey - y);
            double radSum = eRadius + size / 2;
            return distSq < radSum * radSum;
        } else {
            return x > enemy.getX() && x < enemy.getX() + enemy.getSize() && 
                   y > enemy.getY() && y < enemy.getY() + enemy.getSize();
        }
    }

    /**
     * Helper method to calculate the minimum distance from a point to a line segment.
     * Essential for accurate beam-to-enemy collision detection.
     */
    private double distToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        if (l2 == 0.0) return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        double dx = px - (x1 + t * (x2 - x1));
        double dy = py - (y1 + t * (y2 - y1));
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** @return true if the projectile is still active in the world. */
    public boolean isActive() { return active; }
    /** Disables the projectile, causing it to be removed on the next update. */
    public void deactivate() { this.active = false; }
    /** @return The damage value this projectile carries. */
    public double getDamage() { return damage; }
    /** @return The behavior type of this projectile. */
    public WeaponConfig.ProjectileType getType() { return type; }
    /** @return The current X coordinate. */
    public double getX() { return x; }
    /** @return The current Y coordinate. */
    public double getY() { return y; }
}
