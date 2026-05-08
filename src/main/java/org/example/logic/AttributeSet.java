package org.example.logic;

/**
 * Manages the core RPG attributes of a living entity.
 */
public class AttributeSet {
    private double hp;
    private double maxHp;
    private double speed;
    
    // Advanced Attributes
    private double strength; // Damage modifier
    private double defense;  // Damage reduction
    private double spirit;   // Qi capacity & regeneration modifier

    /**
     * @param maxHp Health capacity.
     * @param speed Movement speed.
     * @param strength Damage bonus.
     * @param defense Damage mitigation.
     * @param spirit Qi efficacy.
     */
    public AttributeSet(double maxHp, double speed, double strength, double defense, double spirit) {
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
        this.strength = strength;
        this.defense = defense;
        this.spirit = spirit;
    }

    /** @return Current health points. */
    public double getHp() { return hp; }
    /** @param hp New health value (clamped to max). */
    public void setHp(double hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    /**
     * Reduces health after mitigation check.
     * @param amount Raw damage.
     */
    public void takeDamage(double amount) {
        // Simple mitigation formula: reduce damage by defense, minimum 1 damage if hit
        double actualDamage = Math.max(1, amount - defense);
        this.hp -= actualDamage;
        if (this.hp < 0) this.hp = 0;
    }
    /**
     * Increases health without exceeding max.
     * @param amount Healing points.
     */
    public void heal(double amount) {
        if (amount <= 0) return;
        this.hp += amount;
        if (this.hp > this.maxHp) this.hp = this.maxHp;
    }

    /** @return Max health capacity. */
    public double getMaxHp() { return maxHp; }
    /** @param maxHp New capacity. Adjusts current HP if needed. */
    public void setMaxHp(double maxHp) { 
        this.maxHp = maxHp; 
        if (this.hp > maxHp) this.hp = maxHp;
    }

    /** @return Movement speed. */
    public double getSpeed() { return speed; }
    /** @param speed New speed. */
    public void setSpeed(double speed) { this.speed = speed; }

    /** @return Damage bonus. */
    public double getStrength() { return strength; }
    /** @param strength New strength. */
    public void setStrength(double strength) { this.strength = strength; }

    /** @return Damage reduction. */
    public double getDefense() { return defense; }
    /** @param defense New defense. */
    public void setDefense(double defense) { this.defense = defense; }

    /** @return Qi scaling attribute. */
    public double getSpirit() { return spirit; }
    /** @param spirit New spirit. */
    public void setSpirit(double spirit) { this.spirit = spirit; }
    
    /** @param amount Increment strength. */
    public void addStrength(double amount) { this.strength += amount; }
    /** @param amount Increment defense. */
    public void addDefense(double amount) { this.defense += amount; }
    /** @param amount Increment spirit. */
    public void addSpirit(double amount) { this.spirit += amount; }
    /** @param amount Increment max HP. */
    public void addMaxHp(double amount) { this.setMaxHp(this.maxHp + amount); }
}
