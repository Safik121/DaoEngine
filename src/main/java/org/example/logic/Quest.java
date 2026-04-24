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

    public void addRewardItem(String itemId) {
        rewardItemIds.add(itemId);
    }

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

    public double getProgressPercentage() {
        if (requiredAmount <= 0) return 1.0;
        return (double) currentAmount / requiredAmount;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ObjectiveType getObjectiveType() { return objectiveType; }
    public String getTargetId() { return targetId; }
    public int getRequiredAmount() { return requiredAmount; }
    public int getCurrentAmount() { return currentAmount; }
    public boolean isCompleted() { return isCompleted; }
    public List<String> getRewardItemIds() { return rewardItemIds; }
    public double getRewardQi() { return rewardQi; }
    public String getRewardSkillId() { return rewardSkillId; }
    public void setRewardSkillId(String skillId) { this.rewardSkillId = skillId; }
    
    public String getWorldFlagOnComplete() { return worldFlagOnComplete; }
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
