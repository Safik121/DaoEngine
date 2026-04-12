package org.example.item;

import org.example.entity.Player;

/**
 * Specialized class for items designed to deal damage.
 * Typically do not have any on-use effects beyond triggering combat logic.
 */
public class WeaponItem extends Item {
    public WeaponItem(String id, String name, String description) {
        super(id, name, description, Type.WEAPON);
    }

    @Override
    public void use(Player player) {
        // Weapons are typically used via the CombatManager or active combat loop.
        // Direct 'use' from inventory could optionally equip it.
        System.out.println("Equipped or readied weapon: " + getName());
    }
}
