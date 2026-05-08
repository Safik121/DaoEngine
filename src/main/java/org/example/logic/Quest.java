package org.example.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a quest given to the player.
 */
public class Quest {
    public enum ObjectiveType {
        KILL,
        COLLECT,
        TALK
    }

    private String id;
    private String name;
    private String description;
    
    private ObjectiveType objectiveType;
    private String targetId;
    private int requiredAmount;
    private int currentAmount;

    private List<String> rewardItemIds;
    private double rewardQi;
    private String rewardSkillId;

    private boolean isCompleted;
    private String worldFlagOnComplete;

    /**
     * @param id Unique ID.
     * @param name Name shown in UI.
     * @param description Brief summary.
     * @param type KILL, COLLECT, etc.
     * @param targetId ID of entity/item.
     * @param requiredAmount Target quantity.
     */
    public Quest(String id, String name, String description, ObjectiveType type, String targetId, int requiredAmount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objectiveType = type;
        this.targetId = targetId;
        this.requiredAmount = requiredAmount;
        this.currentAmount = 0;
        this.isCompleted = false;
        this.rewardItemIds = new ArrayList<>();
    }

    /** @param itemId Item given on completion. */
    public void addRewardItem(String itemId) {
        rewardItemIds.add(itemId);
    }

    /** @param qi Qi restored on completion. */
    public void setRewardQi(double qi) {
        this.rewardQi = qi;
    }

    /**
     * Registers progress for this quest.
     * @return true if the quest just completed, false otherwise.
     */
    public boolean addProgress(int amount) {
        if (isCompleted) return false;

        currentAmount += amount;
        if (currentAmount >= requiredAmount) {
            currentAmount = requiredAmount;
            isCompleted = true;
            
            // Trigger world flag if defined
            if (worldFlagOnComplete != null && !worldFlagOnComplete.isEmpty()) {
                WorldState.getInstance().setFlag(worldFlagOnComplete, true);
            }
            
            return true;
        }
        return false;
    }

    /** @return 0.0 to 1.0 progress. */
    public double getProgressPercentage() {
        if (requiredAmount <= 0) return 1.0;
        return (double) currentAmount / requiredAmount;
    }

    // Getters
    /** @return Unique ID. */
    public String getId() { return id; }
    /** @return Name. */
    public String getName() { return name; }
    /** @return Text. */
    public String getDescription() { return description; }
    /** @return Objective category. */
    public ObjectiveType getObjectiveType() { return objectiveType; }
    /** @return Target ID (enemy/item). */
    public String getTargetId() { return targetId; }
    /** @return Target quantity. */
    public int getRequiredAmount() { return requiredAmount; }
    /** @return Current quantity. */
    public int getCurrentAmount() { return currentAmount; }
    /** @return Completion status. */
    public boolean isCompleted() { return isCompleted; }
    /** @return Rewards IDs. */
    public List<String> getRewardItemIds() { return rewardItemIds; }
    /** @return Reward Qi. */
    public double getRewardQi() { return rewardQi; }
    /** @return Reward Skill. */
    public String getRewardSkillId() { return rewardSkillId; }
    /** @param skillId Skill ID to grant. */
    public void setRewardSkillId(String skillId) { this.rewardSkillId = skillId; }
    
    /** @return Global flag to set on finish. */
    public String getWorldFlagOnComplete() { return worldFlagOnComplete; }
    /** @param flag Global flag ID. */
    public void setWorldFlagOnComplete(String flag) { this.worldFlagOnComplete = flag; }

    /**
     * Used for restoring saved state.
     */
    public void setCurrentAmount(int amount) {
        this.currentAmount = amount;
        if (this.currentAmount >= this.requiredAmount) {
            this.isCompleted = true;
        }
    }
}
