package org.example.item;

/**
 * Data structure representing an item configuration from JSON.
 * Contains basic properties and optional boost/effect values.
 */
public class ItemConfig {
    /** Unique identifier matching recipes and world logic. */
    public String id;
    /** Display name shown in the inventory and dialogs. */
    public String name;
    /** Lore or functional text describing the item. */
    public String description;
    /** Extended lore and usage details for the Book of Knowledge. */
    public String detailedDescription;
    /** Categorization (WEAPON, CONSUMABLE, MATERIAL, etc.). */
    public Item.Type type;

    // Optional effects
    /** Amount of HP to restore upon use. */
    public double hpRestore = 0;
    /** Amount of Qi to restore upon use. */
    public double qiRestore = 0;
    /** Permanent increase to max HP upon use. */
    public double maxHpBoost = 0;
    /** Permanent increase to max Qi capacity upon use. */
    public double maxQiBoost = 0;

    /** Optional ID pointing to a specialized entry in weapon_configs.json. */
    public String weaponConfigId;
    /** Unique mapping ID in assets.json for the item's texture. */
    public String spriteId;
}
