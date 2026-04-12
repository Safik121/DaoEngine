package org.example.entity;

/**
 * Configuration for an enemy type.
 * Maps directly to JSON properties in enemy_configs.json.
 */
public class EnemyConfig {
    /** Unique identifier for the enemy type. */
    public String id;
    /** Display name of the enemy. */
    public String name;
    /** Descriptive text about behavior and lore for the Book of Knowledge. */
    public String behaviorDescription;
    
    /** Base health points. */
    public double hp = 1.0;
    /** Base damage dealt to player. */
    public double damage = 1.0;
    /** Base movement speed (pixels per frame at 60 FPS). */
    public double speed = 1.0;
    
    /** Radius/Hitbox size in pixels. */
    public double size = 24.0;
    /** Hex color code for rendering (e.g., "#FF0000"). */
    public String color = "#FF0000";
    /** Unique mapping ID in assets.json for the enemy's texture. */
    public String spriteId;

    // Ranged stats
    public boolean isRanged = false;
    public double attackRange = 0;
    public String projectileType = "FIREBALL";
}
