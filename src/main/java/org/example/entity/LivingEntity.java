package org.example.entity;

import org.example.logic.AttributeSet;

/**
 * Abstract class representing entities with health and combat capabilities
 * (e.g., Player, Enemies).
 */
public abstract class LivingEntity extends BaseEntity {
    protected AttributeSet stats;
    protected boolean facingLeft = false;
    protected org.example.logic.StatusEffectManager statusEffectManager;

    /**
     * @param x Initial X pixel coordinate.
     * @param y Initial Y pixel coordinate.
     * @param size Hitbox size.
     * @param maxHp Health capacity.
     * @param speed Movement speed.
     */
    public LivingEntity(double x, double y, double size, double maxHp, double speed) {
        super(x, y, size);
        // Default base stats, could be overridden by subclasses or configs
        this.stats = new AttributeSet(maxHp, speed, 10, 0, 10);
        this.statusEffectManager = new org.example.logic.StatusEffectManager(this);
    }

    /** @return Manager for active status effects (debuffs/buffs). */
    public org.example.logic.StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    /** @return The internal attribute set (HP, Speed, Defense). */
    public AttributeSet getStats() { return stats; }

    /** @return Current health points. */
    public double getHp() { return stats.getHp(); }
    /** @param hp New health value. */
    public void setHp(double hp) { stats.setHp(hp); }

    /** @return Maximum health capacity. */
    public double getMaxHp() { return stats.getMaxHp(); }
    /** @param maxHp New capacity. */
    public void setMaxHp(double maxHp) { stats.setMaxHp(maxHp); }

    /** @return Base movement speed. */
    public double getSpeed() { return stats.getSpeed(); }
    /** @param speed New base speed. */
    public void setSpeed(double speed) { stats.setSpeed(speed); }

    /** @return true if sprite should be flipped horizontally. */
    public boolean isFacingLeft() { return facingLeft; }
    /** @param facingLeft true if looking left. */
    public void setFacingLeft(boolean facingLeft) { this.facingLeft = facingLeft; }

    /**
     * Applies damage to the entity, mitigating it through defense.
     * @param amount Damage amount
     */
    public void takeDamage(double amount) {
        stats.takeDamage(amount);
    }

    /**
     * Restores health up to the maximum limit.
     * @param amount Healing amount
     */
    public void heal(double amount) {
        stats.heal(amount);
    }

    /**
     * Standard check for entity death.
     * @return true if hp <= 0
     */
    public boolean isDead() {
        return stats.getHp() <= 0;
    }
}
