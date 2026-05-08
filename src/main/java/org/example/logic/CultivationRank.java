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
    
    // Breakthrough Requirements
    private String requiredItemId;
    private int requiredItemCount;

    public CultivationRank() {} // For Jackson

    /**
     * @param title Realm name.
     * @param tier Stage within the realm.
     * @param requiredQiToBreakthrough Qi cost.
     * @param hpBonus Max HP increase.
     * @param strengthBonus Strength increase.
     * @param defenseBonus Defense increase.
     * @param spiritBonus Spirit increase.
     * @param description Narrative text.
     * @param requiredItemId Item ID needed (optional).
     * @param requiredItemCount Item quantity needed.
     */
    public CultivationRank(String title, int tier, double requiredQiToBreakthrough, 
                           double hpBonus, double strengthBonus, double defenseBonus, double spiritBonus, String description,
                           String requiredItemId, int requiredItemCount) {
        this.title = title;
        this.tier = tier;
        this.requiredQiToBreakthrough = requiredQiToBreakthrough;
        this.hpBonus = hpBonus;
        this.strengthBonus = strengthBonus;
        this.defenseBonus = defenseBonus;
        this.spiritBonus = spiritBonus;
        this.description = description;
        this.requiredItemId = requiredItemId;
        this.requiredItemCount = requiredItemCount;
    }

    /** @return Name of the realm. */
    public String getTitle() { return title; }
    /** @return Numeric tier. */
    public int getTier() { return tier; }
    /** @return Qi cost for advancement. */
    public double getRequiredQiToBreakthrough() { return requiredQiToBreakthrough; }
    /** @return HP bonus on reaching. */
    public double getHpBonus() { return hpBonus; }
    /** @return Strength bonus on reaching. */
    public double getStrengthBonus() { return strengthBonus; }
    /** @return Defense bonus on reaching. */
    public double getDefenseBonus() { return defenseBonus; }
    /** @return Spirit bonus on reaching. */
    public double getSpiritBonus() { return spiritBonus; }
    /** @return Lore description. */
    public String getDescription() { return description; }
    /** @return Required item ID. */
    public String getRequiredItemId() { return requiredItemId; }
    /** @return Required item quantity. */
    public int getRequiredItemCount() { return requiredItemCount; }

    /** @return Formatted full title (e.g. "Foundation 1"). */
    public String getFullName() {
        return title + " " + tier;
    }
}
