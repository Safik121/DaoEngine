package org.example.logic;

import org.example.entity.Player;
import java.util.List;

/**
 * Manages the player's progression through cultivation ranks.
 * Data is loaded from CultivationRegistry (JSON).
 */
public class CultivationManager {
    private List<CultivationRank> progressionPath;
    private int currentRankIndex = 0;

    public CultivationManager() {
        // Load the full path from the registry
        this.progressionPath = CultivationRegistry.getFullProgressionPath();
        
        // Safety check if registry is empty
        if (this.progressionPath.isEmpty()) {
            this.progressionPath.add(new CultivationRank("Mortal", 0, 0, 0, 0, 0, 0));
        }
    }

    public CultivationRank getCurrentRank() {
        if (currentRankIndex < progressionPath.size()) {
            return progressionPath.get(currentRankIndex);
        }
        return progressionPath.get(progressionPath.size() - 1);
    }
    
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

        double requiredQi = next.getRequiredQiToBreakthrough();
        if (player.getQi() >= requiredQi) {
            // Consume Qi for the breakthrough
            player.setQi(player.getQi() - requiredQi);
            
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
            
            return true;
        }
        return false;
    }

    /**
     * Used for saving/loading state.
     */
    public int getCurrentRankIndex() { return currentRankIndex; }
    public void setCurrentRankIndex(int index) { this.currentRankIndex = index; }
}
