package org.example.logic;

import org.example.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages active status effects on a LivingEntity.
 * Handles update loops and removal of expired effects.
 */
public class StatusEffectManager {
    private final LivingEntity owner;
    private final List<StatusEffect> activeEffects;

    public StatusEffectManager(LivingEntity owner) {
        this.owner = owner;
        this.activeEffects = new ArrayList<>();
    }

    /**
     * Adds an effect to the entity and triggers onApply.
     */
    public void addEffect(StatusEffect effect) {
        // Prevent duplicate types if needed, but for now we allow stacks
        activeEffects.add(effect);
        effect.onApply(owner);
    }

    /**
     * Updates all active effects.
     * @param dt Delta time in seconds.
     */
    public void update(double dt) {
        List<StatusEffect> toRemove = new ArrayList<>();
        
        for (StatusEffect effect : activeEffects) {
            boolean keep = effect.onTick(owner, dt);
            if (!keep) {
                toRemove.add(effect);
            }
        }

        for (StatusEffect effect : toRemove) {
            effect.onRemove(owner);
            activeEffects.remove(effect);
        }
    }

    public List<StatusEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }
    
    public void clear() {
        for (StatusEffect effect : activeEffects) {
            effect.onRemove(owner);
        }
        activeEffects.clear();
    }
}
