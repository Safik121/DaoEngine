package org.example.item;

import org.example.entity.Player;
import org.example.state.PlayState;

/**
 * Represents raw materials or miscellaneous items that cannot be "used" directly
 * but are essential for crafting and commerce.
 */
public class MaterialItem extends Item {
    public MaterialItem(String id, String name, String description, Type type) {
        super(id, name, description, type);
    }

    @Override
    public void use(Player player, PlayState state) {
        // Materials cannot be used directly.
        System.out.println(getName() + " cannot be used directly. Try crafting with it!");
    }
}
