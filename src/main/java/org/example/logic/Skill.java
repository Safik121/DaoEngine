package org.example.logic;

import org.example.item.WeaponConfig;

/**
 * Represents an active technique a cultivator can use.
 */
public class Skill {
    private String id;
    private String name;
    private double qiCost;
    private double cooldown;
    private WeaponConfig weaponConfig; // Re-use the projectile logic
    
    public Skill() {} // For Jackson

    public Skill(String id, String name, double qiCost, double cooldown, WeaponConfig weaponConfig) {
        this.id = id;
        this.name = name;
        this.qiCost = qiCost;
        this.cooldown = cooldown;
        this.weaponConfig = weaponConfig;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getQiCost() { return qiCost; }
    public void setQiCost(double qiCost) { this.qiCost = qiCost; }
    
    public double getCooldown() { return cooldown; }
    public void setCooldown(double cooldown) { this.cooldown = cooldown; }
    
    public WeaponConfig getWeaponConfig() { return weaponConfig; }
    public void setWeaponConfig(WeaponConfig weaponConfig) { this.weaponConfig = weaponConfig; }
}
