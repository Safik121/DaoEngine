package org.example.logic;

import org.example.GameLogger;
import org.example.entity.Player;
import java.util.List;

/**
 * Manages the player's progression through cultivation ranks.
 * Data is loaded from CultivationRegistry (JSON).
 */
public class CultivationManager {
    private List<CultivationRank> progressionPath;
    private int currentRankIndex = 0;

    /**
     * Initializes the manager and loads the progression path from the registry.
     */
    public CultivationManager() {
        // Load the full path from the registry
        this.progressionPath = CultivationRegistry.getFullProgressionPath();
        
        // Safety check if registry is empty
        if (this.progressionPath.isEmpty()) {
            this.progressionPath.add(new CultivationRank("Mortal", 0, 0, 0, 0, 0, 0, "A mere mortal with no cultivation.", null, 0));
        }
    }

    /** @return The current cultivation rank of the player. */
    public CultivationRank getCurrentRank() {
        if (currentRankIndex < progressionPath.size()) {
            return progressionPath.get(currentRankIndex);
        }
        return progressionPath.get(progressionPath.size() - 1);
    }
    
    /** @return The next achievable cultivation rank, or null if at max rank. */
    public CultivationRank getNextRank() {
        if (currentRankIndex + 1 < progressionPath.size()) {
            return progressionPath.get(currentRankIndex + 1);
        }
        return null;
    }

    /**
     * Tries to perform a breakthrough. 
     * If the player has enough Qi, they ascend to the next tier and gain specific stat bonuses from that rank.
     */
    public boolean attemptBreakthrough(Player player) {
        CultivationRank next = getNextRank();
        if (next == null) return false; // Max rank achieved

        // Consume Qi for the breakthrough
        double requiredQi = next.getRequiredQiToBreakthrough();
        String requiredItem = next.getRequiredItemId();
        int requiredCount = next.getRequiredItemCount();

        // Check for item requirements
        if (requiredItem != null && !requiredItem.isEmpty() && requiredCount > 0) {
            if (!player.getInventory().hasItem(requiredItem, requiredCount)) {
                // Should ideally send a notification here but manager doesn't have reference to PlayState
                org.example.GameLogger.info("[Cultivation] Missing required item: " + requiredItem + " x" + requiredCount);
                return false;
            }
        }

        if (player.getQi() >= requiredQi) {
            // Consume Qi and Items
            player.setQi(player.getQi() - requiredQi);
            if (requiredItem != null && !requiredItem.isEmpty() && requiredCount > 0) {
                player.getInventory().removeItem(requiredItem, requiredCount);
            }
            
            // Advance rank
            currentRankIndex++;
            
            // Apply dynamic stat growths from the new rank
            AttributeSet stats = player.getStats();
            stats.addMaxHp(next.getHpBonus());
            stats.addStrength(next.getStrengthBonus());
            stats.addDefense(next.getDefenseBonus());
            stats.addSpirit(next.getSpiritBonus());
            
            // Breakthrough fully heals
            stats.heal(stats.getMaxHp());
            
            org.example.logic.SoundManager.playSound("breakthrough");
            return true;
        }
        return false;
    }

    /**
     * Used for saving/loading state.
     */
    /** @return Current position in the progression list. */
    public int getCurrentRankIndex() { return currentRankIndex; }
    /** @param index New progression index (used during load). */
    public void setCurrentRankIndex(int index) { this.currentRankIndex = index; }
}
