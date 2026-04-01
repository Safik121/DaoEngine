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
}
