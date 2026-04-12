package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.AssetRegistry;
import org.example.item.WeaponConfig;
import org.example.level.GameMap;

/**
 * Represents an active projectile fired from a weapon or skill.
 * Now agnostic to the owner (Player or Enemy).
 */
public class Projectile {
    protected double x;
    protected double y;
    protected double vx;
    protected double vy;
    protected double angle;
    protected double length;
    protected double size;
    protected double damage;
    protected double lifeSpan;
    protected WeaponConfig.ProjectileType type;
    protected boolean active = true;
    
    /** The entity that fired this projectile. */
    protected LivingEntity owner;
    
    /** If true, this projectile hits enemies. If false, it hits the player. */
    protected boolean friendly;
    
    protected double animationTimer = 0;

    /**
     * Constructs a new Projectile.
     * 
     * @param x        Starting X coordinate.
     * @param y        Starting Y coordinate.
     * @param angle    Directon in radians.
     * @param config   Config defining stats and type.
     * @param owner    Firing entity.
     * @param friendly Whether it targets enemies (true) or player (false).
     */
    public Projectile(double x, double y, double angle, WeaponConfig config, LivingEntity owner, boolean friendly) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.owner = owner;
        this.friendly = friendly;
        this.size = config.size;
        
        // Damage scaling logic
        double strModifier = (owner != null) ? owner.getStats().getStrength() : 0;
        this.damage = config.damage + strModifier;
        
        this.lifeSpan = config.lifeSpan;
        this.type = config.projectileType;
        this.length = config.length;

        this.vx = Math.cos(angle) * config.speed;
        this.vy = Math.sin(angle) * config.speed;
    }

    public void update(GameMap gameMap, double deltaTime) {
        animationTimer += deltaTime;
        double dtFactor = deltaTime * 60.0;
        
        if (type == WeaponConfig.ProjectileType.AOE_ZONE && owner != null) {
            this.x = owner.getX() + owner.getSize()/2;
            this.y = owner.getY() + owner.getSize()/2;
        } else if (type != WeaponConfig.ProjectileType.BEAM) {
            x += vx * dtFactor;
            y += vy * dtFactor;
        }
        
        lifeSpan -= deltaTime;
        if (lifeSpan <= 0) active = false;

        if (active && type != WeaponConfig.ProjectileType.BEAM && type != WeaponConfig.ProjectileType.AOE_ZONE &&
            gameMap.isSolid((int)(x / gameMap.getTileSize()), (int)(y / gameMap.getTileSize()))) {
            active = false;
        }
    }

    public void render(GraphicsContext gc, double camX, double camY) {
        if (!active) return;
        
        javafx.scene.image.Image sprite = null;
        int frameIndex = 0;

        // Custom color for enemy projectiles to make them stand out
        Color effectColor = friendly ? Color.ORANGERED : Color.PURPLE;

        switch (type) {
            case FIREBALL:
                frameIndex = (int) (animationTimer / 0.08) % 4;
                sprite = AssetRegistry.getSprite("fireball", frameIndex);
                if (sprite != null) {
                    gc.drawImage(sprite, x - camX - size/2, y - camY - size/2, size, size);
                } else {
                    gc.setFill(effectColor);
                    gc.fillOval(x - camX - size/2, y - camY - size/2, size, size);
                }
                break;
            case FLYING_SWORD:
                sprite = AssetRegistry.getSprite("flying_sword", 0);
                if (sprite != null) {
                    gc.drawImage(sprite, x - camX - size/2, y - camY - size/2, size, size);
                } else {
                    gc.setStroke(friendly ? Color.WHITE : Color.RED);
                    gc.setLineWidth(2);
                    double endX = x - vx * 2;
                    double endY = y - vy * 2;
                    gc.strokeLine(x - camX, y - camY, endX - camX, endY - camY);
                }
                break;
            case BEAM:
                gc.setStroke(friendly ? Color.CYAN : Color.MEDIUMVIOLETRED);
                gc.setLineWidth(size);
                gc.setGlobalAlpha(Math.min(1.0, lifeSpan * 5));
                double bx2 = x + Math.cos(angle) * length;
                double by2 = y + Math.sin(angle) * length;
                gc.strokeLine(x - camX, y - camY, bx2 - camX, by2 - camY);
                gc.setGlobalAlpha(1.0);
                break;
            case AOE_ZONE:
                gc.setGlobalAlpha(0.3);
                gc.setFill(friendly ? Color.MEDIUMPURPLE : Color.DARKRED);
                gc.fillOval(x - camX - size / 2, y - camY - size / 2, size, size);
                gc.setGlobalAlpha(1.0);
                break;
        }
    }

    public boolean checkCollision(LivingEntity target) {
        double ex = target.getX() + target.getSize() / 2;
        double ey = target.getY() + target.getSize() / 2;
        double eRadius = target.getSize() / 2;

        if (type == WeaponConfig.ProjectileType.BEAM) {
            double x2 = x + Math.cos(angle) * length;
            double y2 = y + Math.sin(angle) * length;
            return distToSegment(ex, ey, x, y, x2, y2) < eRadius + size/2;
        } else if (type == WeaponConfig.ProjectileType.AOE_ZONE) {
            double distSq = (ex - x) * (ex - x) + (ey - y) * (ey - y);
            double radSum = eRadius + size / 2;
            return distSq < radSum * radSum;
        } else {
            return x > target.getX() && x < target.getX() + target.getSize() && 
                   y > target.getY() && y < target.getY() + target.getSize();
        }
    }

    private double distToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        if (l2 == 0.0) return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        double dx = px - (x1 + t * (x2 - x1));
        double dy = py - (y1 + t * (y2 - y1));
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    public double getDamage() { return damage; }
    public boolean isFriendly() { return friendly; }
    public double getX() { return x; }
    public double getY() { return y; }
    public org.example.item.WeaponConfig.ProjectileType getType() { return type; }
}
