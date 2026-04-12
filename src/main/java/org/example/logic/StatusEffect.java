package org.example.logic;

import org.example.entity.LivingEntity;

/**
 * Interface for all temporary effects that can be applied to a LivingEntity.
 * Examples: Burn (DoT), Slow (Speed reduction), Regen (Healing over time).
 */
public interface StatusEffect {
    /**
     * Called when the effect is first applied to the entity.
     */
    void onApply(LivingEntity target);

    /**
     * Called every frame to update the effect's logic.
     * @param target The entity being affected.
     * @param dt Time elapsed in seconds.
     * @return true if the effect should persist, false if it has expired.
     */
    boolean onTick(LivingEntity target, double dt);

    /**
     * Called when the effect expires or is manually removed.
     */
    void onRemove(LivingEntity target);

    /** @return Descriptive name of the effect for UI. */
    String getName();
}
