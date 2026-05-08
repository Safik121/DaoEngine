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

    /**
     * @param id Unique ID.
     * @param name Name in UI.
     * @param qiCost Resource cost.
     * @param cooldown Time between uses.
     * @param weaponConfig Projectile/Attack definition.
     */
    public Skill(String id, String name, double qiCost, double cooldown, WeaponConfig weaponConfig) {
        this.id = id;
        this.name = name;
        this.qiCost = qiCost;
        this.cooldown = cooldown;
        this.weaponConfig = weaponConfig;
    }

    /** @return Unique ID. */
    public String getId() { return id; }
    /** @param id The ID. */
    public void setId(String id) { this.id = id; }
    
    /** @return Skill name. */
    public String getName() { return name; }
    /** @param name Display name. */
    public void setName(String name) { this.name = name; }
    
    /** @return Qi consumed per cast. */
    public double getQiCost() { return qiCost; }
    /** @param qiCost The cost. */
    public void setQiCost(double qiCost) { this.qiCost = qiCost; }
    
    /** @return Cooldown in seconds. */
    public double getCooldown() { return cooldown; }
    /** @param cooldown The cooldown. */
    public void setCooldown(double cooldown) { this.cooldown = cooldown; }
    
    /** @return Attack configuration. */
    public WeaponConfig getWeaponConfig() { return weaponConfig; }
    /** @param weaponConfig Attack data. */
    public void setWeaponConfig(WeaponConfig weaponConfig) { this.weaponConfig = weaponConfig; }
}
