package org.example.item;

import org.example.entity.Player;

/**
 * Specialized class for items used in crafting or as miscellaneous resources.
 * These items generally do not have active use effects.
 */
public class MaterialItem extends Item {
    public MaterialItem(String id, String name, String description, Type type) {
        super(id, name, description, type);
    }

    @Override
    public void use(Player player) {
        System.out.println("Cannot use " + getName() + " directly. Try crafting with it.");
    }
}
