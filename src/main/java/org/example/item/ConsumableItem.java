package org.example.item;

import org.example.entity.Player;
import org.example.state.PlayState;

/**
 * Specialized class for consumable items that restore player stats or grant buffs.
 */
public class ConsumableItem extends Item {
    public ConsumableItem(String id, String name, String description) {
        super(id, name, description, Type.CONSUMABLE);
    }

    @Override
    public void use(Player player, PlayState state) {
        if (getHpRestore() > 0) {
            player.heal(getHpRestore());
            System.out.println("Restored " + getHpRestore() + " HP.");
        }
        if (getQiRestore() > 0) {
            player.restoreQi(getQiRestore());
            System.out.println("Restored " + getQiRestore() + " Qi.");
        }
        if (getMaxHpBoost() > 0) {
            player.setMaxHp(player.getMaxHp() + getMaxHpBoost());
            System.out.println("Increased max HP by " + getMaxHpBoost());
        }
        if (getMaxQiBoost() > 0) {
            player.setMaxQi(player.getMaxQi() + getMaxQiBoost());
            System.out.println("Increased max Qi by " + getMaxQiBoost());
        }
    }
}
