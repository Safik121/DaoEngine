package org.example.logic;

import org.example.entity.Player;
import org.example.item.ItemRegistry;
import org.example.logic.event.GameEvent;
import org.example.logic.event.GameEventListener;
import org.example.state.PlayState;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all active and completed quests for the player.
 * Now implements GameEventListener to automatically react to world events.
 */
public class QuestManager implements GameEventListener {
    private List<Quest> activeQuests = new ArrayList<>();
    private List<Quest> completedQuests = new ArrayList<>();

    public void addQuest(Quest q, PlayState state) {
        if (!hasQuest(q.getId())) {
            activeQuests.add(q);
            System.out.println("[Quest] Added: " + q.getName());
            if (state != null) {
                state.addNotification("New Quest: " + q.getName());
            }
        }
    }

    public void addCompletedQuest(Quest q) {
        if (!isQuestCompleted(q.getId())) {
            completedQuests.add(q);
        }
    }

    public boolean hasQuest(String id) {
        for (Quest q : activeQuests) if (q.getId().equals(id)) return true;
        for (Quest q : completedQuests) if (q.getId().equals(id)) return true;
        return false;
    }

    public boolean isQuestActive(String id) {
        for (Quest q : activeQuests) if (q.getId().equals(id)) return true;
        return false;
    }

    public boolean isQuestCompleted(String id) {
        for (Quest q : completedQuests) if (q.getId().equals(id)) return true;
        return false;
    }

    @Override
    public void onGameEvent(GameEvent event, String targetId, int amount, PlayState state) {
        Quest.ObjectiveType mappedType = null;
        if (event == GameEvent.ENTITY_DEATH) mappedType = Quest.ObjectiveType.KILL;
        else if (event == GameEvent.ITEM_PICKUP) mappedType = Quest.ObjectiveType.COLLECT;

        if (mappedType != null) {
            registerProgress(mappedType, targetId, amount, state);
        }
    }

    /**
     * Internal method to process progress.
     */
    private void registerProgress(Quest.ObjectiveType type, String targetId, int amount, PlayState state) {
        List<Quest> newlyCompleted = new ArrayList<>();
        Player player = state.getPlayer();

        for (Quest q : activeQuests) {
            if (q.getObjectiveType() == type && q.getTargetId().equals(targetId)) {
                if (q.addProgress(amount)) {
                    newlyCompleted.add(q);
                } else {
                    state.addNotification(q.getName() + ": " + q.getCurrentAmount() + "/" + q.getRequiredAmount());
                }
            }
        }

        for (Quest q : newlyCompleted) {
            completeQuest(q, state);
        }
    }

    private void completeQuest(Quest q, PlayState state) {
        activeQuests.remove(q);
        completedQuests.add(q);
        System.out.println("[Quest] Completed: " + q.getName() + "!");
        
        if (state != null) {
            state.addNotification("QUEST COMPLETED: " + q.getName());
            state.getSoundManager().playSfx("quest_complete");
        }

        Player player = state.getPlayer();

        // Give rewards
        if (q.getRewardQi() > 0) {
            player.restoreQi(q.getRewardQi());
        }

        for (String itemId : q.getRewardItemIds()) {
            player.getInventory().addItem(ItemRegistry.createItem(itemId));
        }

        if (q.getRewardSkillId() != null && !q.getRewardSkillId().isEmpty()) {
            Skill skill = SkillRegistry.getSkill(q.getRewardSkillId());
            if (skill != null) {
                player.setActiveSkill(skill);
                if (state != null) {
                    state.addNotification("TECHNIQUE LEARNED: " + skill.getName());
                }
            }
        }
    }

    public List<Quest> getActiveQuests() {
        return activeQuests;
    }

    public List<Quest> getCompletedQuests() {
        return completedQuests;
    }
}
