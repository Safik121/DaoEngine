package org.example.item;

import org.example.entity.Player;

/**
 * Base class for all items in the game.
 * Items can be held in the inventory and some can be used.
 */
public class Item {
    /**
     * Enum for different item types.
     */
    public enum Type {
        /** Can be consumed for effects (e.g., healing). */
        CONSUMABLE,
        /** Can be used to attack. */
        WEAPON,
        /** Used in the crafting system. */
        CRAFTING,
        /** Miscellaneous items. */
        MISC
    }

    /** Unique identifier for the item (used for internal logic). */
    private String id;
    /** Display name of the item shown to the player. */
    private String name;
    /** Descriptive text explaining the item's purpose. */
    private String description;
    /** The general category of the item. */
    private Type type;

    // --- Dynamic Effects (Loaded from JSON) ---
    private double hpRestore = 0;
    private double qiRestore = 0;
    private double maxHpBoost = 0;
    private double maxQiBoost = 0;
    private String weaponConfigId;
    /** Unique ID of the sprite for this item from assets.json. */
    private String spriteId;

    /**
     * Constructs a new Item.
     * 
     * @param id Unique identifier (for recipes/logic).
     * @param name Display name.
     * @param description Brief text for the player.
     * @param type Category of the item.
     */
    public Item(String id, String name, String description, Type type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    /**
     * Called when the item is used from the inventory or hotbar.
     * Applies any configured effects to the target player.
     * 
     * @param player The player instance to apply effects to.
     */
    public void use(Player player) {
        if (hpRestore > 0) {
            player.heal(hpRestore);
            System.out.println("Restored " + hpRestore + " HP.");
        }
        if (qiRestore > 0) {
            player.restoreQi(qiRestore);
            System.out.println("Restored " + qiRestore + " Qi.");
        }
        if (maxHpBoost > 0) {
            player.setMaxHp(player.getMaxHp() + maxHpBoost);
            System.out.println("Increased max HP by " + maxHpBoost);
        }
        if (maxQiBoost > 0) {
            player.setMaxQi(player.getMaxQi() + maxQiBoost);
            System.out.println("Increased max Qi by " + maxQiBoost);
        }
    }

    // Getters and Setters
    /** @return Unique item ID. */
    public String getId() { return id; }
    /** @return Player-facing name. */
    public String getName() { return name; }
    /** @return Multi-line description. */
    public String getDescription() { return description; }
    /** @return Item classification. */
    public Type getType() { return type; }

    /**
     * Retrieves the weapon configuration for this item if it is a weapon.
     * 
     * @return The WeaponConfig associated with this item ID, or null if not a weapon.
     */
    public WeaponConfig getWeaponConfig() {
        if (type == Type.WEAPON) {
            String configId = (weaponConfigId != null) ? weaponConfigId : id;
            return WeaponRegistry.getWeaponConfig(configId);
        }
        return null;
    }

    // --- Getters and Setters for Effects ---
    public double getHpRestore() { return hpRestore; }
    public void setHpRestore(double val) { this.hpRestore = val; }
    
    public double getQiRestore() { return qiRestore; }
    public void setQiRestore(double val) { this.qiRestore = val; }
    
    public double getMaxHpBoost() { return maxHpBoost; }
    public void setMaxHpBoost(double val) { this.maxHpBoost = val; }
    
    public double getMaxQiBoost() { return maxQiBoost; }
    public void setMaxQiBoost(double val) { this.maxQiBoost = val; }

    public String getWeaponConfigId() { return weaponConfigId; }
    public void setWeaponConfigId(String id) { this.weaponConfigId = id; }
    
    public String getSpriteId() { return spriteId; }
    public void setSpriteId(String spriteId) { this.spriteId = spriteId; }
}
