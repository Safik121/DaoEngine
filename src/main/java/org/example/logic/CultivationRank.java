package org.example.logic;

/**
 * Represents a discrete stage of cultivation in the game world.
 * Now includes stat bonuses for breakthroughs.
 */
public class CultivationRank {
    private String title;
    private int tier;
    private double requiredQiToBreakthrough;
    
    // Stat bonuses granted upon reaching this rank
    private double hpBonus;
    private double strengthBonus;
    private double defenseBonus;
    private double spiritBonus;
    private String description;

    public CultivationRank() {} // For Jackson

    public CultivationRank(String title, int tier, double requiredQiToBreakthrough, 
                           double hpBonus, double strengthBonus, double defenseBonus, double spiritBonus, String description) {
        this.title = title;
        this.tier = tier;
        this.requiredQiToBreakthrough = requiredQiToBreakthrough;
        this.hpBonus = hpBonus;
        this.strengthBonus = strengthBonus;
        this.defenseBonus = defenseBonus;
        this.spiritBonus = spiritBonus;
        this.description = description;
    }

    public String getTitle() { return title; }
    public int getTier() { return tier; }
    public double getRequiredQiToBreakthrough() { return requiredQiToBreakthrough; }
    public double getHpBonus() { return hpBonus; }
    public double getStrengthBonus() { return strengthBonus; }
    public double getDefenseBonus() { return defenseBonus; }
    public double getSpiritBonus() { return spiritBonus; }
    public String getDescription() { return description; }

    public String getFullName() {
        return title + " " + tier;
    }
}
