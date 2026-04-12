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

    public void addQuest(Quest q) {
        if (!hasQuest(q.getId())) {
            activeQuests.add(q);
            System.out.println("[Quest] Added: " + q.getName());
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
            registerProgress(mappedType, targetId, amount, state.getPlayer());
        }
    }

    /**
     * Internal method to process progress.
     */
    private void registerProgress(Quest.ObjectiveType type, String targetId, int amount, Player player) {
        List<Quest> newlyCompleted = new ArrayList<>();

        for (Quest q : activeQuests) {
            if (q.getObjectiveType() == type && q.getTargetId().equals(targetId)) {
                if (q.addProgress(amount)) {
                    newlyCompleted.add(q);
                }
            }
        }

        for (Quest q : newlyCompleted) {
            completeQuest(q, player);
        }
    }

    private void completeQuest(Quest q, Player player) {
        activeQuests.remove(q);
        completedQuests.add(q);
        System.out.println("[Quest] Completed: " + q.getName() + "!");

        // Give rewards
        if (q.getRewardQi() > 0) {
            player.restoreQi(q.getRewardQi());
        }

        for (String itemId : q.getRewardItemIds()) {
            player.getInventory().addItem(ItemRegistry.createItem(itemId));
        }
    }

    public List<Quest> getActiveQuests() {
        return activeQuests;
    }
}
