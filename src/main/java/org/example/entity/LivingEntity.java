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

    public LivingEntity(double x, double y, double size, double maxHp, double speed) {
        super(x, y, size);
        // Default base stats, could be overridden by subclasses or configs
        this.stats = new AttributeSet(maxHp, speed, 10, 0, 10);
        this.statusEffectManager = new org.example.logic.StatusEffectManager(this);
    }

    public org.example.logic.StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    public AttributeSet getStats() { return stats; }

    public double getHp() { return stats.getHp(); }
    public void setHp(double hp) { stats.setHp(hp); }

    public double getMaxHp() { return stats.getMaxHp(); }
    public void setMaxHp(double maxHp) { stats.setMaxHp(maxHp); }

    public double getSpeed() { return stats.getSpeed(); }
    public void setSpeed(double speed) { stats.setSpeed(speed); }

    public boolean isFacingLeft() { return facingLeft; }
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
