package org.example.item;

import org.example.GameLogger;
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
            GameLogger.info("Restored " + getHpRestore() + " HP.");
        }
        if (getQiRestore() > 0) {
            player.restoreQi(getQiRestore());
            GameLogger.info("Restored " + getQiRestore() + " Qi.");
        }
        if (getMaxHpBoost() > 0) {
            player.setMaxHp(player.getMaxHp() + getMaxHpBoost());
            GameLogger.info("Increased max HP by " + getMaxHpBoost());
        }
        if (getMaxQiBoost() > 0) {
            player.setMaxQi(player.getMaxQi() + getMaxQiBoost());
            GameLogger.info("Increased max Qi by " + getMaxQiBoost());
        }
    }
}
