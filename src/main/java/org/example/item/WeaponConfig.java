package org.example.item;

/**
 * Configuration data for a weapon's combat properties.
 * These properties are loaded from a JSON file (weapon_configs.json) 
 * to allow for data-driven weapon balancing and creation.
 */
public class WeaponConfig {
    /**
     * Defines the various visual and logical behaviors of projectiles 
     * fired by weapons.
     */
    public enum ProjectileType {
        /** A standard bullet-like projectile that moves in a straight line. */
        FIREBALL,
        /** A specialized projectile that moves toward its target (e.g., a flying sword). */
        FLYING_SWORD,
        /** A continuous segment or beam of energy connecting the source to the target. */
        BEAM
    }

    /** The behavior and rendering type of the projectile. */
    public ProjectileType projectileType = ProjectileType.FIREBALL;
    
    /** The amount of health reduced from an enemy upon a successful hit. */
    public int damage = 10;
    
    /** The mandatory delay (in seconds) between successive attacks. */
    public double cooldown = 0.5;
    
    /** The velocity at which the projectile travels (pixels per frame). */
    public double speed = 5.0;
    
    /** The physical diameter or width of the projectile in world pixels. */
    public double size = 8.0;
    
    /** The maximum length for beam-type projectiles. */
    public double length = 0.0;
    
    /** The amount of Spiritual Energy (Qi) consumed to fire this weapon. */
    public double qiCost = 5.0;
    
    /** How long (in seconds) the projectile remains active in the world. */
    public double lifeSpan = 3.0;
}
