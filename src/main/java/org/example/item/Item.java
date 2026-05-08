package org.example.item;

import org.example.entity.Player;
import org.example.state.PlayState;

/**
 * Base class for all items in the game.
 * Items can be held in the inventory and some can be used.
 */
public abstract class Item {
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
        /** Technical manual for learning skills. */
        SKILL_BOOK,
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
    /** The index of the frame within the sprite sheet (if applicable). */
    private int spriteFrame = 0;
    /** The skill ID this book teaches, if applicable. */
    private String skillId;

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
     * Applies any configured effects to the target player, implemented by subclasses.
     * 
     * @param player The player instance to apply effects to.
     * @param state  The current PlayState for world interaction.
     */
    public abstract void use(Player player, PlayState state);

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
    /** @return Amount of HP restored on use. */
    public double getHpRestore() { return hpRestore; }
    /** @param val HP restoration amount. */
    public void setHpRestore(double val) { this.hpRestore = val; }
    
    /** @return Amount of Qi restored on use. */
    public double getQiRestore() { return qiRestore; }
    /** @param val Qi restoration amount. */
    public void setQiRestore(double val) { this.qiRestore = val; }
    
    /** @return permanent Max HP bonus. */
    public double getMaxHpBoost() { return maxHpBoost; }
    /** @param val Max HP boost. */
    public void setMaxHpBoost(double val) { this.maxHpBoost = val; }
    
    /** @return permanent Max Qi bonus. */
    public double getMaxQiBoost() { return maxQiBoost; }
    /** @param val Max Qi boost. */
    public void setMaxQiBoost(double val) { this.maxQiBoost = val; }

    /** @return ID for weapon data lookup. */
    public String getWeaponConfigId() { return weaponConfigId; }
    /** @param id Weapon config ID. */
    public void setWeaponConfigId(String id) { this.weaponConfigId = id; }
    
    /** @return Sprite identifier in AssetRegistry. */
    public String getSpriteId() { return spriteId; }
    /** @param spriteId The sprite ID. */
    public void setSpriteId(String spriteId) { this.spriteId = spriteId; }
    
    /** @return Index of the frame in spritesheet. */
    public int getSpriteFrame() { return spriteFrame; }
    /** @param spriteFrame The frame index. */
    public void setSpriteFrame(int spriteFrame) { this.spriteFrame = spriteFrame; }

    /** @return The skill ID this item teaches. */
    public String getSkillId() { return skillId; }
    /** @param skillId The skill ID. */
    public void setSkillId(String skillId) { this.skillId = skillId; }
}
