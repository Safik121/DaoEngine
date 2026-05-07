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

    public AttributeSet(double maxHp, double speed, double strength, double defense, double spirit) {
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
        this.strength = strength;
        this.defense = defense;
        this.spirit = spirit;
    }

    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    public void takeDamage(double amount) {
        // Simple mitigation formula: reduce damage by defense, minimum 1 damage if hit
        double actualDamage = Math.max(1, amount - defense);
        this.hp -= actualDamage;
        if (this.hp < 0) this.hp = 0;
    }
    public void heal(double amount) {
        if (amount <= 0) return;
        this.hp += amount;
        if (this.hp > this.maxHp) this.hp = this.maxHp;
    }

    public double getMaxHp() { return maxHp; }
    public void setMaxHp(double maxHp) { 
        this.maxHp = maxHp; 
        if (this.hp > maxHp) this.hp = maxHp;
    }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getStrength() { return strength; }
    public void setStrength(double strength) { this.strength = strength; }

    public double getDefense() { return defense; }
    public void setDefense(double defense) { this.defense = defense; }

    public double getSpirit() { return spirit; }
    public void setSpirit(double spirit) { this.spirit = spirit; }
    
    public void addStrength(double amount) { this.strength += amount; }
    public void addDefense(double amount) { this.defense += amount; }
    public void addSpirit(double amount) { this.spirit += amount; }
    public void addMaxHp(double amount) { this.setMaxHp(this.maxHp + amount); }
}
