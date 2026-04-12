package org.example.logic;

import java.util.HashMap;
import java.util.Map;
import org.example.GameLogger;

/**
 * Manages temporary status effects (buffs) applied to the player.
 * Tracks remaining durations and provides checks for active effects like
 * invulnerability.
 */
public class BuffManager {

    public enum BuffType {
        INVULNERABILITY,
        SPEED_BOOST,
        REGEN_BOOST,
        QI_EFFICIENCY
    }

    private final Map<BuffType, Double> activeBuffs = new HashMap<>();

    /**
     * Updates all active buffs, decreasing their remaining duration.
     * 
     * @param deltaTime Time elapsed since last frame.
     */
    public void update(double deltaTime) {
        activeBuffs.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() <= 0;
            if (!expired) {
                entry.setValue(entry.getValue() - deltaTime);
            }
            return expired;
        });
    }

    /**
     * Applies a buff or refreshes its duration.
     * 
     * @param type     The type of buff to apply.
     * @param duration Duration in seconds.
     */
    public void applyBuff(BuffType type, double duration) {
        double current = activeBuffs.getOrDefault(type, 0.0);
        activeBuffs.put(type, Math.max(current, duration));
        GameLogger.info("Applied buff: " + type + " for " + duration + "s");
    }

    /**
     * Checks if a specific buff is currently active and not expired.
     */
    public boolean hasBuff(BuffType type) {
        return activeBuffs.getOrDefault(type, 0.0) > 0;
    }

    public double getRemainingTime(BuffType type) {
        return activeBuffs.getOrDefault(type, 0.0);
    }

    public void clear() {
        activeBuffs.clear();
    }
}
