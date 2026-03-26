package org.example.item;

/**
 * Base class for all items in the game.
 * Items can be held in the inventory and some can be used.
 */
public class Item {
    public enum Type {
        CONSUMABLE,
        WEAPON,
        CRAFTING,
        MISC
    }

    private String id;
    private String name;
    private String description;
    private Type type;

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
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
}
