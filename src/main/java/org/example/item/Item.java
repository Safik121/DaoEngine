package org.example.item;

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
     */
    public void use() {
        System.out.println("Used item: " + name);
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
}
