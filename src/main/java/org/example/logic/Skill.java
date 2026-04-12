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

    public Skill(String id, String name, double qiCost, double cooldown, WeaponConfig weaponConfig) {
        this.id = id;
        this.name = name;
        this.qiCost = qiCost;
        this.cooldown = cooldown;
        this.weaponConfig = weaponConfig;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getQiCost() { return qiCost; }
    public double getCooldown() { return cooldown; }
    public WeaponConfig getWeaponConfig() { return weaponConfig; }
}
